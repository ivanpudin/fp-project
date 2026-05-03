package com.rockthejvm

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.net.{HttpURLConnection, URL}

// represents a single energy measurement from a source over a 3-minute interval
case class EnergyReading(source: String, startTime: ZonedDateTime, endTime: ZonedDateTime, valueMW: Double)

// Fingrid dataset IDs for each energy source
object FingridDatasets {
  val Wind        = 181
  val Solar       = 248
  val Hydro       = 191
  val Nuclear     = 188
  val Consumption = 124
}

// shared interface — allows swapping between different client implementations
trait EnergyClient {
  def fetch(datasetId: Int, source: String,
            startTime: ZonedDateTime, endTime: ZonedDateTime): Either[String, List[EnergyReading]]
}

// actually fetching data from the API
object FingridClient extends EnergyClient {
  private val ApiKey  = sys.env.getOrElse("FINGRID_API_KEY", "7d6b56142a1c467088c41bdb4e1c7af9")
  private val BaseUrl = "https://data.fingrid.fi/api"
  private val Fmt     = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  def fetch(datasetId: Int, source: String,
            startTime: ZonedDateTime, endTime: ZonedDateTime): Either[String, List[EnergyReading]] =
    fetchAllPages(datasetId, source, startTime, endTime)

  // fetches all the pages of results with the needed info, either grouping them into a list or throwing an exception if something goes wrong
  // try/catch wraps only the HTTP work so the recursive call stays in tail position
  @annotation.tailrec
  private def fetchAllPages(datasetId: Int, source: String,
                            start: ZonedDateTime, end: ZonedDateTime,
                            page: Int = 1, acc: List[EnergyReading] = List.empty): Either[String, List[EnergyReading]] = {
    if (page > 1) Thread.sleep(2050)

    val result = try {
      val conn = new URL(s"$BaseUrl/datasets/$datasetId/data?startTime=${start.format(Fmt)}&endTime=${end.format(Fmt)}&format=json&pageSize=20000&page=$page")
        .openConnection().asInstanceOf[HttpURLConnection]
      conn.setRequestProperty("x-api-key", ApiKey)
      conn.setRequestProperty("Cache-Control", "no-cache")
      if (conn.getResponseCode != 200) Left(s"HTTP ${conn.getResponseCode}")
      else {
        val body     = scala.io.Source.fromInputStream(conn.getInputStream).mkString
        val json     = ujson.read(body)
        val pages    = json("pagination")
        val lastPage = pages("lastPage").num.toInt
        val readings = json("data").arr.toList.flatMap { item =>
          try Some(EnergyReading(source,
            ZonedDateTime.parse(item("startTime").str, Fmt),
            ZonedDateTime.parse(item("endTime").str, Fmt),
            item("value").num))
          catch { case _: Exception => None }
        }
        Right((lastPage, readings))
      }
    } catch { case e: Exception => Left(e.getMessage) }

    result match {
      case Left(err)                   => Left(err)
      case Right((lastPage, readings)) =>
        if (page >= lastPage) Right(acc ++ readings)
        else fetchAllPages(datasetId, source, start, end, page + 1, acc ++ readings) // tail recursion implementation
    }
  }

  def aggregateByHour(readings: List[EnergyReading]): List[EnergyReading] =
    readings
      .groupBy(r => (r.source, r.startTime.withMinute(0).withSecond(0).withNano(0)))
      .map { case ((source, start), group) =>
        val avgValue = group.map(_.valueMW).sum / group.size
        EnergyReading(source, start, start.plusHours(1), avgValue)
      }
      .toList
      .sortBy(_.startTime.toEpochSecond)

  // Single method to write all data to a file
  def getData(): Unit = {
    val path  = "energy.csv"
    val end   = ZonedDateTime.now(java.time.ZoneOffset.UTC)
    val start = end.minusMonths(1)

    val datasets = List(
      (FingridDatasets.Wind, "Wind"),
      (FingridDatasets.Solar, "Solar"),
      (FingridDatasets.Hydro, "Hydro"),
      (FingridDatasets.Nuclear, "Nuclear"),
      (FingridDatasets.Consumption, "Consumption")
    )

    datasets.zipWithIndex.foreach { case ((id, name), index) =>
      println(s"Fetching $name...")

      fetch(id, name, start, end) match {
        case Left(err) =>
          println(s"  Error: $err")
        case Right(raw) =>
          val hourly = aggregateByHour(raw)

          // If it's the first dataset (index 0), overwrite the file (append = false)
          // For all subsequent datasets, append to the file (append = true)
          val isFirst = index == 0
          FileIO.write(hourly, path, append = !isFirst)

          println(s"  Fetched ${hourly.size} rows from Fingrid API")
      }

      // API rate limit safeguard
      Thread.sleep(2100)
    }
  }

  // curried function that checks if a reading is below a certain threshold and either returns None or an alert message for low output
  def thresholdAlert(threshold: Double, label: String)(r: EnergyReading): Option[String] =
    if (r.valueMW < threshold) Some(s"[ALERT] $label low output: ${r.valueMW} MW @ ${r.startTime}")
    else None

  // prints the first 3 readings for a source
  def report[A](label: String, result: Either[String, List[A]])(display: A => String): Unit =
    result match {
      case Left(err) => println(s"Error [$label]: $err")
      case Right(xs) => xs.take(3).foreach(r => println(s"  $label: ${display(r)}"))
    }
}

// reading and writing a file in a .csv format
object FileIO {
  def write(readings: List[EnergyReading], path: String, append: Boolean = true): Unit = {
    val file = new java.io.FileWriter(path, append)
    try {
      readings.foreach { r =>
        file.write(s"${r.source},${r.startTime},${r.endTime},${r.valueMW}\n")
      }
    } finally {
      file.close()
    }
  }

  def read(path: String): List[EnergyReading] = {
    val file = new java.io.BufferedReader(new java.io.FileReader(path))
    try {
      Iterator.continually(file.readLine())
        .takeWhile(_ != null)
        .flatMap { line =>
          line.split(",") match {
            case Array(src, start, end, value) =>
              try Some(EnergyReading(src,
                ZonedDateTime.parse(start),
                ZonedDateTime.parse(end),
                value.toDouble))
              catch { case _: Exception => None }
            case _ => None
          }
        }
        .toList
    } finally {
      file.close()
    }
  }
}
