package com.rockthejvm

case class DataStatus(
    severity: String,
    message: String
)


object DataStatusUtils {
    def getStatus(data: List[DataRow]): DataStatus = {
        val malfunctions = getMalfunctions(data)
        
        val totalProduced  = getTotalProduction(data)
        val totalConsumed  = getTotalConsumption(data)
        val isLowProduction = totalProduced < totalConsumed

        if (malfunctions.nonEmpty && isLowProduction)
            DataStatus("Red", "Malfunctions and low production detected")
        else if (malfunctions.nonEmpty)
            DataStatus("Red", "Malfunctions detected")
        else if (isLowProduction)
            DataStatus("Yellow", "Low production")
        else
            DataStatus("Green", "Nothing requires attention")
    }

    def getMalfunctions(data: List[DataRow]) : List[DataRow] = {
        data.filter(datarow => datarow.energyProduction == 0.0)
    }

    def getTotalProduction(data: List[DataRow]) : Double = {
        val analytics = AnalyticsHelper.runAnalytics(data)
        val consumed = getTotalConsumption(data)
        val sums = analytics.values.map(x => x("sum").asInstanceOf[Double])
        val production = sums.sum - consumed
        production
    }

    def getTotalConsumption(data: List[DataRow]) : Double = {
        val consumed = AnalyticsHelper.runAnalytics(data)
        consumed.get("Consumption") match {
            case Some(statistics) => statistics("sum").asInstanceOf[Double]
            case None => 0.0
        }
    }

    def isProductionLow(data: List[DataRow]) : Boolean = {
        getTotalConsumption(data)>getTotalProduction(data)
    }

    def main(args: Array[String]): Unit = {
        DataCollectorUtils.fromFile("energy.csv") match {
            case Right(data) =>
                println(getStatus(data))
            case Left(error) =>
                println(s"Failed to read file: $error")
        }
    }
}