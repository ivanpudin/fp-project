package com.rockthejvm

import scala.io.Source

case class DataRow(
    energyType: String,
    startDate: String,
    endDate: String,
    energyProduction: Double
)

object DataCollectorUtils {
    def Try[A](a: => A): Either[Exception, A] =
        try Right(a)
        catch { case e: Exception => Left(e) }

    def fromFile(filename: String) : Either[String, List[DataRow]] = {
        def lineToDataRow(line: String) : Either[String, DataRow] = {
            val values = line.split(",").map(_.trim)
            
            if (values.length != 4) {
                Left(s"Invalid row format (expected columns:4): $line")
            } else {
            
                val production = Try(values(3).toDouble)

                production match {
                    case Right(energyProduction) =>
                        Right(DataRow(
                            values(0),
                            values(1),
                            values(2),
                            energyProduction
                        ))
                    case Left(_) => Left(s"Production could not have been parsed to double in line: $line")
                }
            }
            
        }


        Try(Source.fromFile(filename)) match {
            case Left(e) => Left(s"Could not open $filename. Error: $e")
            case Right(source) => try {
                val data = source.getLines().map(line => lineToDataRow(line)).toList
                
                val lefts = data.filter(_.isLeft)
                
                if (lefts.isEmpty) {
                    Right(
                        data.map {
                            case Right(row) => row
                            case Left(_) => throw new RuntimeException("Something went wrong")
                        }
                    )
                } else {
                    lefts.head match {
                        case Left(errorMessage) => Left(errorMessage)
                        case _ => Left("Unknown error")
                    }
                }

            } finally {
                source.close()
            }
        }
    }

    def main(args: Array[String]): Unit = {
        val data = DataCollectorUtils.fromFile("energy.csv")
        println(data)
    }
}