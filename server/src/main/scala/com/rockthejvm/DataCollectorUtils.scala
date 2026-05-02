package com.rockthejvm

import scala.io.Source

case class DataRow(
    energyType: String,
    startDate: String,
    endDate: String,
    energyProduction: Double
)

object DataCollectorUtils {
    def fromFile(filename: String) : List[DataRow] = {
        def lineToDataRow(line: String) : DataRow = {
            val values = line.split(",").map(_.trim)
            DataRow(
                values(0),
                values(1),
                values(2),
                values(3).toDouble
            )
        }


        val source = Source.fromFile(filename)
        val data = source.getLines().map(line => lineToDataRow(line)).toList
        source.close()
        return data
    }

    def main(args: Array[String]): Unit = {
        val data = DataCollectorUtils.fromFile("energy.csv")
        println(data)
    }
}