package com.rockthejvm

import ujson.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

case class EnergyReading(source: String, startTime: ZonedDateTime, endTime: ZonedDateTime, valueMW: Double)

object FingridDatasets:
  val Wind  = 181
  val Solar = 248
  val Hydro = 191

object FingridClient:

  private val ApiKey = sys.env.getOrElse("FINGRID_API_KEY", "7d6b56142a1c467088c41bdb4e1c7af9")
  private val BaseUrl = "https://data.fingrid.fi/api"
  private val Fmt     = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  def fetch(datasetId: Int, source: String,
            startTime: ZonedDateTime, endTime: ZonedDateTime): Either[String, List[EnergyReading]] =
    fetchAllPages(datasetId, source, startTime, endTime)

  @annotation.tailrec
  private def fetchAllPages(datasetId: Int, source: String,
                            start: ZonedDateTime, end: ZonedDateTime,
                            page: Int = 1, acc: List[EnergyReading] = List.empty): Either[String, List[EnergyReading]] =
    val params = Map("startTime" -> start.format(Fmt), "endTime" -> end.format(Fmt),
                     "format" -> "json", "pageSize" -> "1000", "page" -> page.toString)
    try
      val res = requests.get(s"$BaseUrl/datasets/$datasetId/data",
                             params = params,
                             headers = Map("x-api-key" -> ApiKey, "Cache-Control" -> "no-cache"))
      if res.statusCode != 200 then Left(s"HTTP ${res.statusCode}")
      else
        val json     = ujson.read(res.text())
        val lastPage = json("pagination")("lastPage").num.toInt
        val readings = json("data").arr.toList.flatMap: item =>
          try Some(EnergyReading(source,
            ZonedDateTime.parse(item("startTime").str, Fmt),
            ZonedDateTime.parse(item("endTime").str, Fmt),
            item("value").num))
          catch case _: Exception => None
        if page >= lastPage then Right(acc ++ readings)
        else fetchAllPages(datasetId, source, start, end, page + 1, acc ++ readings)
    catch case e: Exception => Left(e.getMessage)