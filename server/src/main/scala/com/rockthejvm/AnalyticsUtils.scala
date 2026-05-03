package com.rockthejvm

import scala.math.Numeric.Implicits._

/* 
Use the --interactive flag to test either of the objects
Eg: scala --interactive .\AnalyticsUtils.scala .\DataCollectorUtils.scala
then select which one to run in the interactive menu
*/


object AnalyticsUtils {

    def mean[A](data: Seq[A])(implicit num: Numeric[A]) : Double = {
        if (data.isEmpty) 0.0
        else data.map(x => num.toDouble(x)).sum / data.length
    }
    
    def median[A](data: Seq[A])(implicit num: Numeric[A]) : Double = {
        if (data.isEmpty) {
            return 0.0
        }

        val sortedSeq = data.sorted
        val length = sortedSeq.length

        if (length % 2 == 0) {
            (num.toDouble(sortedSeq(length/2 - 1)) + num.toDouble(sortedSeq(length/2))) / 2.0
        } else num.toDouble(sortedSeq((length-1) / 2))
    }

    def mode[A](data: Seq[A])(implicit num: Numeric[A]) : Seq[A] = {
        if (data.isEmpty) {
            return Seq.empty
        }
        
        val frequency = data.foldLeft(Map.empty[A, Int]) { (acc, element) =>
            acc + (element -> (acc.getOrElse(element, 0) + 1))
        }

        val maxOccurance = frequency.values.max

        frequency.filter(_._2 == maxOccurance).keys.toSeq

    }

    def range[A](data: Seq[A])(implicit num: Numeric[A]) : Double = {
        if (data.isEmpty) 0.0
        else num.toDouble(data.max) - num.toDouble(data.min)
    }

    def midrange[A](data: Seq[A])(implicit num: Numeric[A]) : Double = {
        if (data.isEmpty) 0.0
        else (num.toDouble(data.max) + num.toDouble(data.min)) / 2.0
    }

    def sum[A](data: Seq[A])(implicit num: Numeric[A]) : Double = {
        if (data.isEmpty) 0.0
        else data.map(x => num.toDouble(x)).sum
    }

    // Tests
    def main(args: Array[String]): Unit = {
        val seq1 = Seq(10, 20, 20, 30, 50)
        println(s"Data Set: $seq1")
        println(s"1. Mean:     ${mean(seq1)} (Expected: 26.0)")
        println(s"2. Median:   ${median(seq1)} (Expected: 20.0)")
        println(s"3. Mode:     ${mode(seq1)} (Expected: List(20))")
        println(s"4. Range:    ${range(seq1)} (Expected: 40.0)")
        println(s"5. Midrange: ${midrange(seq1)} (Expected: 30.0)")
        println(s"6. Sum:      ${sum(seq1)} (Expected: 130.0)")

        val seq2 = Seq(10, 20, 30, 40)
        println(s"Data Set: $seq2")
        println(s"1. Mean:     ${mean(seq2)} (Expected: 25.0)")
        println(s"2. Median:   ${median(seq2)} (Expected: 25.0)")
        println(s"3. Mode:     ${mode(seq2)} (Expected: List(10, 20, 30, 40))")
        println(s"4. Range:    ${range(seq2)} (Expected: 30.0)")
        println(s"5. Midrange: ${midrange(seq2)} (Expected: 25.0)")
        println(s"6. Sum:      ${sum(seq2)} (Expected: 100.0)")

        val seq3 = Seq(1.5, 2.5, 2.5, 3.5, 5.5)
        println(s"Data Set: $seq3")
        println(s"1. Mean:     ${mean(seq3)} (Expected: 3.1)")
        println(s"2. Median:   ${median(seq3)} (Expected: 2.5)")
        println(s"3. Mode:     ${mode(seq3)} (Expected: List(2.5))")
        println(s"4. Range:    ${range(seq3)} (Expected: 4.0)")
        println(s"5. Midrange: ${midrange(seq3)} (Expected: 3.5)")
        println(s"6. Sum:      ${sum(seq3)} (Expected: 15.5)")

        val seq4 = Seq(10.5, 20.0, 30.5, 40.0)
        println(s"Data Set: $seq4")
        println(s"1. Mean:     ${mean(seq4)} (Expected: 25.25)")
        println(s"2. Median:   ${median(seq4)} (Expected: 25.25)")
        println(s"3. Mode:     ${mode(seq4)} (Expected: List(10.5, 20.0, 30.5, 40))")
        println(s"4. Range:    ${range(seq4)} (Expected: 29.5)")
        println(s"5. Midrange: ${midrange(seq4)} (Expected: 25.25)")
        println(s"6. Sum:      ${sum(seq4)} (Expected: 101.0)")
    }
}

object AnalyticsHelper {
    def getStats[A](data: Seq[A])(implicit num: Numeric[A]) : Map[String, Any] = {
        Map(
            "mean" -> AnalyticsUtils.mean(data),
            "median" -> AnalyticsUtils.median(data),
            "mode" -> AnalyticsUtils.mode(data),
            "range" -> AnalyticsUtils.range(data),
            "midrange" -> AnalyticsUtils.midrange(data),
            "sum" -> AnalyticsUtils.sum(data)
        )
    }

    def runAnalytics(data: List[DataRow]) : Map[String, Map[String, Any]] = {
        //get all energy type
        val types = data.map(datarow => datarow.energyType).toSet

        //have a sequence for each energy type
        val sequences = types.map(
            energyType => energyType -> data.filter(
                datarow => datarow.energyType == energyType
                )
                .map(datarow => datarow.energyProduction)).toMap

        sequences.map(pair => pair._1 -> getStats(pair._2))
        
    }

    def main(args: Array[String]): Unit = {
        val data = DataCollectorUtils.fromFile("energy.csv")

        data match {
            case Left(e) => println(s"Error $e")
            case Right(d) => AnalyticsHelper.runAnalytics(d).foreach(println)
        }
        
    }
}