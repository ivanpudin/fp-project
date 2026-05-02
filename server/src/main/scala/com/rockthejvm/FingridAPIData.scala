package com.rockthejvm

import scala.util.parsing.json.JSON
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.net.{HttpURLConnection, URL}

// represents a single energy measurement from a source over a 3-minute interval
case class EnergyReading(source: String, startTime: ZonedDateTime, endTime: ZonedDateTime, valueMW: Double)

// Fingrid dataset IDs for each energy source
object FingridDatasets {
  val Wind  = 181
  val Solar = 248
  val Hydro = 191
}

// shared interface — swap between test and real client via useTest flag in Main
trait EnergyClient {
  def fetch(datasetId: Int, source: String,
            startTime: ZonedDateTime, endTime: ZonedDateTime): Either[String, List[EnergyReading]]
}

// this is for the Main object, which is used for testing everything without going through the real API
object TestFingridClient extends EnergyClient {
  private val rng = new scala.util.Random(42)

  private val baselines: Map[Int, (Double, Double)] = Map(
    FingridDatasets.Wind  -> (500.0,  4000.0),
    FingridDatasets.Solar -> (0.0,    800.0),
    FingridDatasets.Hydro -> (1000.0, 3500.0)
  )

  def fetch(datasetId: Int, source: String,
            startTime: ZonedDateTime, endTime: ZonedDateTime): Either[String, List[EnergyReading]] = {
    val (lo, hi) = baselines.getOrElse(datasetId, (100.0, 1000.0))
    val readings = Iterator
      .iterate(startTime)(_.plusMinutes(3))
      .takeWhile(!_.isAfter(endTime))
      .map { t =>
        val value = if (rng.nextInt(20) == 0) rng.nextDouble() * 50 // random generation of some low output that's about 5% likely
                    else lo + rng.nextDouble() * (hi - lo)
        EnergyReading(source, t, t.plusMinutes(3), BigDecimal(value).setScale(1, BigDecimal.RoundingMode.HALF_UP).toDouble)
      }
      .toList
    Right(readings)
  }
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
  @annotation.tailrec
  private def fetchAllPages(datasetId: Int, source: String,
                            start: ZonedDateTime, end: ZonedDateTime,
                            page: Int = 1, acc: List[EnergyReading] = List.empty): Either[String, List[EnergyReading]] = {
    val result = try {
      val conn = new URL(s"$BaseUrl/datasets/$datasetId/data?startTime=${start.format(Fmt)}&endTime=${end.format(Fmt)}&format=json&pageSize=1000&page=$page")
        .openConnection().asInstanceOf[HttpURLConnection]
      conn.setRequestProperty("x-api-key", ApiKey)
      conn.setRequestProperty("Cache-Control", "no-cache")
      if (conn.getResponseCode != 200) Left(s"HTTP ${conn.getResponseCode}")
      else {
        val body = scala.io.Source.fromInputStream(conn.getInputStream).mkString
        JSON.parseFull(body) match {
          case Some(map: Map[String, Any]) =>
            val pages    = map("pagination").asInstanceOf[Map[String, Any]]
            val lastPage = pages("lastPage").asInstanceOf[Double].toInt
            val readings = map("data").asInstanceOf[List[Map[String, Any]]].flatMap { item =>
              try Some(EnergyReading(source,
                ZonedDateTime.parse(item("startTime").asInstanceOf[String], Fmt),
                ZonedDateTime.parse(item("endTime").asInstanceOf[String], Fmt),
                item("value").asInstanceOf[Double]))
              catch { case _: Exception => None }
            }
            Right((lastPage, readings))
          case _ => Left("Failed to parse response")
        }
      }
    } catch { case e: Exception => Left(e.getMessage) }

    result match {
      case Left(err)                   => Left(err)
      case Right((lastPage, readings)) =>
        if (page >= lastPage) Right(acc ++ readings)
        else fetchAllPages(datasetId, source, start, end, page + 1, acc ++ readings) // tail recursion implementation
    }
  }
}

// reading and writing a file in a .csv format
object FileIO {
  def write(readings: List[EnergyReading], path: String): Unit = {
    val file = new java.io.FileWriter(path, true)
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

// this is only for testing purposes, it won't actually be used
object Main {
  val useTest = false // can be switched to true in order to test everything
  val client: EnergyClient = if (useTest) TestFingridClient else FingridClient

  def main(args: Array[String]): Unit = {
    val start = ZonedDateTime.parse("2024-01-01T00:00:00+02:00")
    val end   = ZonedDateTime.parse("2024-01-01T06:00:00+02:00")

    val sources = List(
      (FingridDatasets.Wind,  "Wind"),
      (FingridDatasets.Solar, "Solar"),
      (FingridDatasets.Hydro, "Hydro")
    )

    // clear the file before writing new data using the real API
    new java.io.FileWriter("output.csv", false).close()

    sources.foreach { case (id, name) =>
      val result = client.fetch(id, name, start, end)
      report(name, result)(r => s"${r.startTime} -> ${r.valueMW} MW")
      result.foreach(readings => FileIO.write(readings, "output.csv"))
    }

    val loaded = FileIO.read("output.csv")
    println(s"Read back ${loaded.length} readings from the file")

    // this is just for testing and demonstration
    sources.foreach { case (_, name) =>
      val values = loaded.filter(_.source == name).map(_.valueMW)
      println(s"\n$name stats:")
      println(f"  mean      ${AnalyticsUtils.mean(values)}%.2f MW")
      println(f"  median    ${AnalyticsUtils.median(values)}%.2f MW")
      println(f"  mode      ${AnalyticsUtils.mode(values).map(v => f"$v%.2f MW").mkString(", ")}")
      println(f"  range     ${AnalyticsUtils.range(values)}%.2f MW")
      println(f"  midrange  ${AnalyticsUtils.midrange(values)}%.2f MW")
    }

    val lowWind  = (r: EnergyReading) => thresholdAlert(500.0,  "Wind")(r)
    val lowSolar = (r: EnergyReading) => thresholdAlert(50.0,   "Solar")(r)
    val lowHydro = (r: EnergyReading) => thresholdAlert(1000.0, "Hydro")(r)

    println("\nAlerts:")
    val alerts = loaded.flatMap(r => lowWind(r) orElse lowSolar(r) orElse lowHydro(r))
    if (alerts.isEmpty) println("  No alerts, all systems are normal.")
    else alerts.foreach(println)
  }
}