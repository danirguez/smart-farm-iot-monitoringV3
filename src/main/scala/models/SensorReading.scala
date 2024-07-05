package models

import java.sql.Timestamp

sealed trait SensorReading

case class TemperatureHumidityReading(
                                       sensorId: String,
                                       temperature: Double,
                                       humidity: Double,
                                       timestamp: Timestamp
                                     ) extends SensorReading

case class CO2Reading(
                       sensorId: String,
                       co2Level: Double,
                       timestamp: Timestamp
                     ) extends SensorReading

case class SoilMoistureReading(
                                sensorId: String,
                                soilMoisture: Double,
                                timestamp: Timestamp
                              ) extends SensorReading