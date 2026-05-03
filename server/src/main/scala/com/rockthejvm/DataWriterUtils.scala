package com.rockthejvm

import java.io.{File, PrintWriter}
import scala.util.Using
import java.time.ZonedDateTime

object DataWriterUtils {
  def storeFile(filename: String, data: List[DataRow]): Either[String, Unit] = {
    try {
      Using.resource(new PrintWriter(new File(filename))) { writer =>
        val csvLines = data.map { row =>
          s"${row.energyType},${row.startDate},${row.endDate},${row.energyProduction}"
        }

        writer.print(csvLines.mkString("\n"))
      }

      Right(())
    } catch {
      case exception: Exception =>
        Left(s"Failed to write to $filename: ${exception.getMessage}")
    }
  }

  def main(args: Array[String]): Unit = {
    val sampleData = List(
      DataRow("Wind", ZonedDateTime.parse("2024-01-01T00:00Z"), ZonedDateTime.parse("2024-01-01T01:00Z"), 150.5),
      DataRow("Solar", ZonedDateTime.parse("2024-01-01T00:00Z"), ZonedDateTime.parse("2024-01-01T01:00Z"), 89.2),
      DataRow("Hydro", ZonedDateTime.parse("2024-01-01T00:00Z"), ZonedDateTime.parse("2024-01-01T01:00Z"), 300.0)
    )

    storeFile("energy_test_output.csv", sampleData) match {
      case Right(_) =>
        println("Successfully wrote data to energy_test_output.csv")
      case Left(error) =>
        println(s"$error")
    }
  }
}
