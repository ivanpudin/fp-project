package com.rockthejvm

import scala.io.Source
import java.time.ZonedDateTime

case class DataRow(
    energyType: String,
    startDate: ZonedDateTime,
    endDate: ZonedDateTime,
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
                val energyType = values(0)
                val parsedStartDate = Try(ZonedDateTime.parse(values(1)))
                val parsedEndDate = Try(ZonedDateTime.parse(values(2)))
                val parsedProduction = Try(values(3).toDouble)

                (parsedStartDate, parsedEndDate, parsedProduction) match {
                    case (Right(start), Right(end), Right(prod)) => Right(DataRow(energyType, start, end, prod))
                    case (Left(error), _, _) => Left(s"Start date could not be parsed in line: $line")
                    case (_, Left(error), _) => Left(s"End date could not be parsed in line: $line")
                    case (_, _, Left(error)) => Left(s"Production could not be parsed in line: $line")
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