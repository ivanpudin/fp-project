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

  private val ApiKey  = sys.env.getOrElse("FINGRID_API_KEY", "7d6b56142a1c467088c41bdb4e1c7af9")
  private val BaseUrl = "https://data.fingrid.fi/api"
  private val Fmt     = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  def fetch(datasetId: Int, source: String,
            startTime: ZonedDateTime, endTime: ZonedDateTime): Either[String, List[EnergyReading]] =
    fetchAllPages(datasetId, source, startTime, endTime)

  def save(readings: List[EnergyReading], path: String): Unit =
    val file = new java.io.FileWriter(path, true)
    try
      readings.foreach: r =>
        file.write(s"${r.source},${r.startTime},${r.endTime},${r.valueMW}\n")
    finally
      file.close()

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

object FileIO:
  def write(readings: List[EnergyReading], path: String): Unit =
    val file = new java.io.FileWriter(path, true)
    try
      readings.foreach: r =>
        file.write(s"${r.source},${r.startTime},${r.endTime},${r.valueMW}\n")
    finally
      file.close()

  def read(path: String): List[EnergyReading] =
    val file = new java.io.BufferedReader(new java.io.FileReader(path))
    try
      Iterator.continually(file.readLine())
        .takeWhile(_ != null)
        .flatMap: line =>
          line.split(",") match
            case Array(src, start, end, value) =>
              try Some(EnergyReading(src,
                ZonedDateTime.parse(start),
                ZonedDateTime.parse(end),
                value.toDouble))
              catch case _: Exception => None
            case _ => None
        .toList
    finally
      file.close()