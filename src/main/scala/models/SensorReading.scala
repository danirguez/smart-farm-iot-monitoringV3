package models

import java.sql.Timestamp
import org.apache.spark.sql.types._

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

object SensorReading {
  val schema: StructType = StructType(Seq(
    StructField("sensorId", StringType),
    StructField("temperature", DoubleType),
    StructField("humidity", DoubleType),
    StructField("timestamp", TimestampType)
  ))
}