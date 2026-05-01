package com.rockthejvm

import ujson.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// ── Model ─────────────────────────────────────────────────────────────────────

case class EnergyReading(source: String, startTime: ZonedDateTime, endTime: ZonedDateTime, valueMW: Double)

// ── Fetch ─────────────────────────────────────────────────────────────────────

val ApiKey = sys.env.getOrElse("FINGRID_API_KEY", "7d6b56142a1c467088c41bdb4e1c7af9")
val Fmt    = DateTimeFormatter.ISO_OFFSET_DATE_TIME

object Datasets:
  val Wind  = 181
  val Solar = 248
  val Hydro = 191

@annotation.tailrec
def fetchPages(apiKey: String, datasetId: Int, source: String,
               start: ZonedDateTime, end: ZonedDateTime,
               page: Int = 1, acc: List[EnergyReading] = List.empty): Either[String, List[EnergyReading]] =
  val params = Map("startTime" -> start.format(Fmt), "endTime" -> end.format(Fmt),
                   "format" -> "json", "pageSize" -> "1000", "page" -> page.toString)
  try
    val res = requests.get(s"https://data.fingrid.fi/api/datasets/$datasetId/data",
                           params = params,
                           headers = Map("x-api-key" -> apiKey, "Cache-Control" -> "no-cache"))
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
      else fetchPages(apiKey, datasetId, source, start, end, page + 1, acc ++ readings)
  catch case e: Exception => Left(e.getMessage)

// ── Menu ──────────────────────────────────────────────────────────────────────

@main def run(): Unit = loop()

@annotation.tailrec
def loop(): Unit =
  println("\n1. Fetch data\n2. View data\n3. Analyze data\n4. Alerts\n5. Exit")
  scala.io.StdIn.readLine().trim match
    case "1" => fetch(); loop()
    case "2" => println("TODO: teammate's view"); loop()
    case "3" => println("TODO: teammate's analysis"); loop()
    case "4" => println("TODO: teammate's alerts"); loop()
    case "5" => println("Bye.")
    case _   => println("Invalid."); loop()

def fetch(): Unit =
  println("Source? 1=Wind 2=Solar 3=Hydro")
  val (id, name) = scala.io.StdIn.readLine().trim match
    case "1" => (Datasets.Wind,  "wind")
    case "2" => (Datasets.Solar, "solar")
    case "3" => (Datasets.Hydro, "hydro")
    case _   => println("Invalid."); return

  val end   = ZonedDateTime.now()
  val start = end.minusHours(24)

  fetchPages(ApiKey, id, name, start, end) match
    case Right(data) => println(s"Fetched ${data.length} readings.")
    case Left(err)   => println(s"Error: $err")