package com.rockthejvm

import java.time.{LocalDate, ZonedDateTime}
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.temporal.WeekFields
import java.util.Locale

sealed trait SortDirection
case object ASC extends SortDirection
case object DESC extends SortDirection

sealed trait TimeBasis
case object Hourly extends TimeBasis
case object Daily extends TimeBasis
case object Weekly extends TimeBasis
case object Monthly extends TimeBasis

object FilterUtils {
  def filterByEnergyType(data: List[DataRow], energyType: String): Either[String, List[DataRow]] = {
    val validTypes = Set("Wind", "Solar", "Hydro", "Nuclear", "Consumption")

    if (!validTypes.contains(energyType)) {
      Left(s"Invalid energy type. Valid types are: ${validTypes.mkString(", ")}")
    } else {
      val filtered = data.filter(_.energyType.equalsIgnoreCase(energyType))
      if (filtered.isEmpty) {
        Left(s"No available data for the selected energy type: $energyType.")
      } else {
        Right(filtered)
      }
    }
  }

  // It asks date as an anchor point.
  // If select monthly, it will give result for the whole month from anchored date.
  // If select weekly, it will give result for the entire week to which anchored date belongs to.
  private val inputDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  def filterByDate(data: List[DataRow], dateStr: String, basis: TimeBasis): Either[String, List[DataRow]] = {
    try {
      val targetDate = LocalDate.parse(dateStr, inputDateFormat)

      val filteredData = data.filter { row =>
        val rowDateTime = ZonedDateTime.parse(row.startDate)
        val rowDate = rowDateTime.toLocalDate

        basis match {
          case Hourly =>
            rowDate.isEqual(targetDate)
          case Daily =>
            rowDate.isEqual(targetDate)
          case Weekly =>
            val weekFields = WeekFields.of(Locale.getDefault)
            rowDate.get(weekFields.weekOfWeekBasedYear()) == targetDate.get(weekFields.weekOfWeekBasedYear()) && rowDate.getYear == targetDate.getYear
          case Monthly =>
            rowDate.getMonthValue == targetDate.getMonthValue && rowDate.getYear == targetDate.getYear
        }
      }

      if (filteredData.isEmpty) {
        Left("No available data for the selected date. Please choose another date.")
      } else {
        Right(filteredData)
      }

    } catch {
      case _: DateTimeParseException =>
        Left(
          "Invalid date format. Please enter the date in the format 'DD/MM/YYYY'.\n" +
            "For example, enter '12/04/2024' for April 12, 2024."
        )
    }
  }

  def sortByEnergyProduction(data: List[DataRow], direction: SortDirection): Either[String, List[DataRow]] = {
    val sorted = data.sortBy(_.energyProduction)
    direction match {
      case ASC => Right(sorted)
      case DESC => Right(sorted.reverse)
    }
  }

  def main(args: Array[String]): Unit = {
    DataCollectorUtils.fromFile("energy.csv") match {
      case Left(e) => printf(s"Error: $e")
      case Right(data) => test(data)
    }


    def test(data: List[DataRow]) : Unit = {
      println("TEST DAILY FILTER")
      filterByDate(data, "01/01/2024", Daily) match {
        case Right(filtered) => filtered.take(3).foreach(println)
        case Left(err) => println(s"$err")
      }

      println("\n TEST INVALID FORMAT")
      filterByDate(data, "April 12, 2024", Daily) match {
        case Right(filtered) => filtered.foreach(println)
        case Left(err) => println(s"$err")
      }

      println("\n TEST NO DATA AVAILABLE")
      filterByDate(data, "15/04/2024", Daily) match {
        case Right(filtered) => filtered.foreach(println)
        case Left(err) => println(s"$err")
      }

      println("\n TEST ENERGY TYPE FILTERING")
      filterByEnergyType(data, "Wind") match {
        case Right(filtered) => filtered.take(3).foreach(println)
        case Left(err) => println(s"$err")
      }

      filterByEnergyType(data, "Nuclear") match {
        case Right(filtered) => filtered.take(3).foreach(println)
        case Left(err) => println(s"$err")
      }

      println("\n TEST SORT DESC")
      val sortingTest = filterByEnergyType(data, "Wind")
        .flatMap(windData => sortByEnergyProduction(windData, DESC))

      sortingTest match {
        case Right(sorted) => sorted.take(10).foreach(println)
        case Left(err) => println(s"$err")
      }
    }
  }
}
