package com.rockthejvm

case class DataStatus(
    severity: String,
    message: String,
    data: List[DataRow]
)


object DataStatusUtils {
    def getStatus(data: List[DataRow], threshold: Double): DataStatus = {
        val malfunctions = getMalfunctions(data)
        val lowProduction = getLowProduction(data, threshold)
        val okay = getOkay(data, threshold)

        if (malfunctions.nonEmpty && lowProduction.nonEmpty){
            val message = s"Malfunctions and low production detected"
            DataStatus("Red", message, malfunctions ++ lowProduction)
        
        } else if (malfunctions.nonEmpty) {
            val message = s"Malfunctions detected"
            DataStatus("Red", message, malfunctions)
        
        } else if (lowProduction.nonEmpty){
            val message = s"Low Production: ${lowProduction.length}"
            DataStatus("Yellow", message, lowProduction)
        
        } else {
            val message = "Nothing requires attention"
            DataStatus("Green", message, okay)
        }
    }

    def getMalfunctions(data: List[DataRow]) : List[DataRow] = {
        data.filter(datarow => datarow.energyProduction == 0.0)
    }

    def getLowProduction(data: List[DataRow], threshold: Double) : List[DataRow] = {
        data.filter(datarow => datarow.energyProduction > 0.0 && datarow.energyProduction < threshold)
    }

    def getOkay(data: List[DataRow], threshold: Double) : List[DataRow] = {
        data.filter(datarow => datarow.energyProduction >= threshold)
    }

    def getMalfunctonsAndLowProduction(data: List[DataRow], threshold: Double) : List[DataRow] = {
        data.filter(datarow => datarow.energyProduction < threshold)
    }

    def main(args: Array[String]): Unit = {
        val threshold = 2000
        DataCollectorUtils.fromFile("energy.csv") match {
            case Right(data) =>
                println(DataStatusUtils.getStatus(data, threshold))
                println("\n\nUnderperforming")
                println(getMalfunctonsAndLowProduction(data, threshold))
            case Left(error) =>
                println(s"Failed to read file: $error")
        }
    }
}