package utils

import models._
import java.sql.Timestamp

object DataValidation {
  def validateTemperatureHumidity(value: String, timestamp: Timestamp): TemperatureHumidityReading = {
    val parts = value.split(",")
    TemperatureHumidityReading(parts(0), parts(1).toDouble, parts(2).toDouble, timestamp)
  }

  def validateCO2(value: String, timestamp: Timestamp): CO2Reading = {
    val parts = value.split(",")
    CO2Reading(parts(0), parts(1).toDouble, timestamp)
  }

  def validateSoilMoisture(value: String, timestamp: Timestamp): SoilMoistureReading = {
    val parts = value.split(",")
    SoilMoistureReading(parts(0), parts(1).toDouble, timestamp)
  }
}