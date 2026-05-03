package com.rockthejvm

import cask._
import upickle.default._
import java.time.ZonedDateTime

object Server extends cask.MainRoutes {

  override def port: Int = 1234
  override def host: String = "localhost"

  // upickle conversion
  implicit val zonedDateTimeRW: ReadWriter[ZonedDateTime] = readwriter[String].bimap[ZonedDateTime](
    zdt => zdt.toString,
    str => ZonedDateTime.parse(str)
  )

  implicit val dataRowRW: ReadWriter[DataRow] = macroRW
  implicit val dataStatusRW: ReadWriter[DataStatus] = macroRW

  // format success json and add CORS headers
  def buildSuccessResponse(data: List[DataRow]): Response[String] = {
    val status = DataStatusUtils.getStatus(data)

    val rawStats = AnalyticsHelper.runAnalytics(data)

    val serializableStats = rawStats.map { case (k, v) =>
      k -> v.map { case (ik, iv) => ik -> iv.toString }
    }

    val jsonString = write(Map(
      "status" -> writeJs(status),
      "data" -> writeJs(data),
      "analytics" -> writeJs(serializableStats)
    ))

    cask.Response(
      jsonString,
      200,
      Seq(
        "Content-Type" -> "application/json",
        "Access-Control-Allow-Origin" -> "*"
      )
    )
  }

  def buildErrorResponse(error: String): Response[String] = {
    cask.Response(
      s"""{"error": "$error"}""",
      500,
      Seq(
        "Content-Type" -> "application/json",
        "Access-Control-Allow-Origin" -> "*"
      )
    )
  }

  @cask.get("/api/get-data")
  def getData(): Response[String] = {
    DataCollectorUtils.fromFile("energy.csv") match {
      case Right(data) => buildSuccessResponse(data)
      case Left(error) => buildErrorResponse(error)
    }
  }

  @cask.get("/api/update-data")
  def updateData(): Response[String] = {
    FingridClient.getData()
    getData()
  }

  @cask.get("/api/analytics")
  def getAnalytics(): Response[String] = {
    DataCollectorUtils.fromFile("energy.csv") match {
      case Right(data) =>
        val stats = AnalyticsHelper.runAnalytics(data)

        // uPickle cannot serialize some things so we convert them to string
        val serializableStats = stats.map { case (k, v) =>
          k -> v.map { case (ik, iv) => ik -> iv.toString }
        }

        val status = DataStatusUtils.getStatus(data)
        val jsonString = write(Map(
          "status" -> writeJs(status),
          "data" -> writeJs(serializableStats)
        ))

        cask.Response(
          jsonString,
          200,
          Seq(
            "Content-Type" -> "application/json",
            "Access-Control-Allow-Origin" -> "*"
          )
        )
      case Left(error) => buildErrorResponse(error)
    }
  }

  @cask.get("/api/filtering/energy-type")
  def filterEnergyType(energyType: String): Response[String] = {
    DataCollectorUtils.fromFile("energy.csv") match {
      case Right(data) =>
        FilterUtils.filterByEnergyType(data, energyType) match {
          case Right(filtered) =>
            DataWriterUtils.storeFile("energy.csv", filtered)
            buildSuccessResponse(filtered)
          case Left(error) => buildErrorResponse(error)
        }
      case Left(error) => buildErrorResponse(error)
    }
  }

  @cask.get("/api/filtering/date")
  def filterDate(date: String, basis: String): Response[String] = {
    val timeBasis = basis.toLowerCase match {
      case "hourly" => Some(Hourly)
      case "daily" => Some(Daily)
      case "weekly" => Some(Weekly)
      case "monthly" => Some(Monthly)
      case _ => None
    }

    timeBasis match {
      case Some(tb) =>
        DataCollectorUtils.fromFile("energy.csv") match {
          case Right(data) =>
            FilterUtils.filterByDate(data, date, tb) match {
              case Right(filtered) =>
                DataWriterUtils.storeFile("energy.csv", filtered)
                buildSuccessResponse(filtered)
              case Left(error) => buildErrorResponse(error)
            }
          case Left(error) => buildErrorResponse(error)
        }
      case None => buildErrorResponse("Invalid time basis provided.")
    }
  }



  initialize()
}
