package com.rockthejvm

import scala.math.Numeric.Implicits._

/* 
Use the --interactive flag to test either of the objects
Eg: scala --interactive .\AnalyticsUtils.scala
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

    // Tests
    def main(args: Array[String]): Unit = {
        val seq1 = Seq(10, 20, 20, 30, 50)
        println(s"Data Set: $seq1")
        println(s"1. Mean:     ${mean(seq1)} (Expected: 26.0)")
        println(s"2. Median:   ${median(seq1)} (Expected: 20.0)")
        println(s"3. Mode:     ${mode(seq1)} (Expected: List(20))")
        println(s"4. Range:    ${range(seq1)} (Expected: 40.0)")
        println(s"5. Midrange: ${midrange(seq1)} (Expected: 30.0)")

        val seq2 = Seq(10, 20, 30, 40)
        println(s"Data Set: $seq2")
        println(s"1. Mean:     ${mean(seq2)} (Expected: 25.0)")
        println(s"2. Median:   ${median(seq2)} (Expected: 25.0)")
        println(s"3. Mode:     ${mode(seq2)} (Expected: List(10, 20, 30, 40))")
        println(s"4. Range:    ${range(seq2)} (Expected: 30.0)")
        println(s"5. Midrange: ${midrange(seq2)} (Expected: 25.0)")

        val seq3 = Seq(1.5, 2.5, 2.5, 3.5, 5.5)
        println(s"Data Set: $seq3")
        println(s"1. Mean:     ${mean(seq3)} (Expected: 3.1)")
        println(s"2. Median:   ${median(seq3)} (Expected: 2.5)")
        println(s"3. Mode:     ${mode(seq3)} (Expected: List(2.5))")
        println(s"4. Range:    ${range(seq3)} (Expected: 4.0)")
        println(s"5. Midrange: ${midrange(seq3)} (Expected: 3.5)")

        val seq4 = Seq(10.5, 20.0, 30.5, 40.0)
        println(s"Data Set: $seq4")
        println(s"1. Mean:     ${mean(seq4)} (Expected: 25.25)")
        println(s"2. Median:   ${median(seq4)} (Expected: 25.25)")
        println(s"3. Mode:     ${mode(seq4)} (Expected: List(10.5, 20.0, 30.5, 40))")
        println(s"4. Range:    ${range(seq4)} (Expected: 29.5)")
        println(s"5. Midrange: ${midrange(seq4)} (Expected: 25.25)")
    }
}

object AnalyticsHelper {
    def getStats[A](data: Seq[Map[String, A]])(implicit num: Numeric[A]) : Map[String, Map[String, Any]] = {
        if (data.isEmpty) {
            return Map.empty
        }

        // get keys
        val keys = data.flatMap(x => x.keys).toSet

        // get sequences for each key
        val sequences = keys.map(key => key -> data.flatMap(line => line.get(key)))

        //get all stats for each sequence
        sequences.map(pair => pair._1 -> Map(
            "mean" -> AnalyticsUtils.mean(pair._2),
            "median" -> AnalyticsUtils.median(pair._2),
            "mode" -> AnalyticsUtils.mode(pair._2),
            "range" -> AnalyticsUtils.range(pair._2),
            "midrange" -> AnalyticsUtils.midrange(pair._2)
        )).toMap
    }

    def main(args: Array[String]): Unit = {
        val seq1 = Seq(
            Map(
                "price" -> 5,
                "random" -> 10
            ),
            Map(
                "price" -> 3,
                "random" -> 15
            )
        )

        AnalyticsHelper.getStats(seq1).foreach(println)

        // example to get 1 stat for 1 field
        println(AnalyticsHelper.getStats(seq1).get("price").get("mean"))
    }
}