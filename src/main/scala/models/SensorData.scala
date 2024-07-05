package models

import java.sql.Timestamp

sealed trait SensorData {
  def sensorId: String
  def timestamp: Timestamp
}

case class TemperatureHumidityData(
                                    reading: TemperatureHumidityReading,
                                    zoneId: Option[String] = None
                                  ) extends SensorData {
  def sensorId: String = reading.sensorId
  def timestamp: Timestamp = reading.timestamp
}

case class CO2Data(
                    reading: CO2Reading,
                    zoneId: Option[String] = None
                  ) extends SensorData {
  def sensorId: String = reading.sensorId
  def timestamp: Timestamp = reading.timestamp
}

case class SoilMoistureData(
                             reading: SoilMoistureReading,
                             zoneId: Option[String] = None
                           ) extends SensorData {
  def sensorId: String = reading.sensorId
  def timestamp: Timestamp = reading.timestamp
}