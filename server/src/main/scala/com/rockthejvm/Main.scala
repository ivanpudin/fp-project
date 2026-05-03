package com.rockthejvm

import scala.io.StdIn.readLine
import scala.annotation.tailrec

object Main {
  private val FILENAME = "energy.csv"

  private def loadData(): List[DataRow] = {
    DataCollectorUtils.fromFile(FILENAME)

    // For version of Collector Utils with error handling:
//    DataCollectorUtils.fromFile(FILENAME) match {
//      case Right(data) =>
//        data
//      case Left(error) =>
//        println(s"Failed to read file: $error")
//        Nil
//    }
  }

  private def saveData(data: List[DataRow]): Unit = {
    DataWriterUtils.storeFile(FILENAME, data) match {
      case Right(_) => ()
      case Left(error) => println(s"Failed to save file: $error")
    }
  }

  @tailrec
  def mainMenu(currentData: List[DataRow]): Unit = {
    println("\n1. Fetch data from API")
    println("2. Show data")
    println("3. Filter data")
    println("4. Analytics")
    println("5. Exit")
    print("Select an option: ")

    readLine() match {
      case "1" =>
        // Call API handler here,
        // assuming that it saves data to .csv on its own

        // After API saved new data to a file, fetch it here
        val newData = loadData()
        println(s"Loaded ${newData.length} rows from Fingrid API.")
        mainMenu(newData)

      case "2" =>
        currentData.foreach(println)
        println(s"Showing ${currentData.length} rows.")
        mainMenu(currentData)

      case "3" =>
        val filteredData = filterMenu(currentData)
        mainMenu(filteredData)

      case "4" =>
        AnalyticsHelper.runAnalytics(currentData).foreach(println)
        mainMenu(currentData)

      case "5" =>
        println("Exiting application...")

      case _ =>
        println("Invalid option. Please try again.")
        mainMenu(currentData)
    }
  }

  @tailrec
  def filterMenu(data: List[DataRow]): List[DataRow] = {
    println("\n\n1. Filter by Energy Type")
    println("2. Filter by Date")
    println("3. Sort by Energy Production")
    println("4. Back to Main Menu")
    print("Select an option: ")

    readLine() match {
      case "1" =>
        print("Enter Energy Type (Wind, Solar, Hydro, Nuclear, Consumption): ")
        val energyType = readLine()
        FilterUtils.filterByEnergyType(data, energyType) match {
          case Right(filtered) =>
            println(s"Data reduced from ${data.length} to ${filtered.length} rows.")
            saveData(filtered)
            filtered
          case Left(error) =>
            println(error)
            filterMenu(data)
        }

      case "2" =>
        print("Enter Date (DD/MM/YYYY): ")
        val date = readLine()

        println("\n\nSelect Time Basis:")
        println("1. Hourly")
        println("2. Daily")
        println("3. Weekly")
        println("4. Monthly")
        print("Select an option: ")

        val basisOption = readLine() match {
          case "1" => Some(Hourly)
          case "2" => Some(Daily)
          case "3" => Some(Weekly)
          case "4" => Some(Monthly)
          case _   => None
        }

        basisOption match {
          case Some(basis) =>
            FilterUtils.filterByDate(data, date, basis) match {
              case Right(filtered) =>
                println(s"Data reduced from ${data.length} to ${filtered.length} rows.")
                saveData(filtered)
                filtered
              case Left(error) =>
                println(error)
                filterMenu(data)
            }
          case None =>
            println("Invalid time basis.")
            filterMenu(data)
        }

      case "3" =>
        print("Sort Direction (1 for ASC, 2 for DESC): ")
        val direction = if (readLine() == "1") ASC else DESC

        FilterUtils.sortByEnergyProduction(data, direction) match {
          case Right(sorted) =>
            println("Data sorted successfully.")
            saveData(sorted)
            sorted
          case Left(error) =>
            println(error)
            filterMenu(data)
        }

      case "4" =>
        data

      case _ =>
        println("Invalid option. Please try again.")
        filterMenu(data)
    }
  }

  def main(args: Array[String]): Unit = {
    val data = loadData()
    println(s"Successfully loaded ${data.length} rows.")
    mainMenu(data)
  }
}
