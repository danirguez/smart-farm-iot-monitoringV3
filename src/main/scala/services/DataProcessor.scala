package services

import config.AppConfig
import models._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._

class DataProcessor(implicit spark: SparkSession) extends Serializable {
  import spark.implicits._

  @transient private lazy val sensorToZoneMap: Map[String, String] = Map(
    "sensor1" -> "zona1", "sensor2" -> "zona1", "sensor3" -> "zona1",
    "sensor4" -> "zone2", "sensor5" -> "zone2", "sensor6" -> "zone2",
    "sensor7" -> "zone3", "sensor8" -> "zone3", "sensor9" -> "zone3"
  )

  private val sensorIdToZoneId = udf((sensorId: String) => sensorToZoneMap.getOrElse(sensorId, "unknown"))

  def processTemperatureHumidity(data: Dataset[TemperatureHumidityReading]): DataFrame = {
    data.withColumn("zoneId", sensorIdToZoneId($"sensorId"))
  }

  def processCO2(data: Dataset[CO2Reading]): DataFrame = {
    data.withColumn("zoneId", sensorIdToZoneId($"sensorId"))
  }

  def processSoilMoisture(data: Dataset[SoilMoistureReading]): DataFrame = {
    data.withColumn("zoneId", sensorIdToZoneId($"sensorId"))
  }

  def aggregateData(df: DataFrame, windowDuration: String, columns: Seq[String]): DataFrame = {
    val aggExprs = columns.map(col => avg(col).alias(s"avg_$col"))
    df.withWatermark("timestamp", "1 minute")
      .groupBy(window($"timestamp", windowDuration), $"zoneId")
      .agg(aggExprs.head, aggExprs.tail: _*)
  }
}