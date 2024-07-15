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

  // 1) Uso de Watermark y Window:
  def calculateAverageTemperature(df: DataFrame): DataFrame = {
    df.withWatermark("event_time", "5 minutes")
      .groupBy(
        window($"event_time", "10 minutes"),
        $"sensorId"
      )
      .agg(
        avg($"temperature").as("avg_temperature")
      )
      .select(
        $"window.start".as("window_start"),
        $"window.end".as("window_end"),
        $"sensorId",
        $"avg_temperature"
      )
  }

  // 2) Uso de CUBE, GROUPING SET o ROLLUP:
  def calculateTemperatureAggregations(df: DataFrame): DataFrame = {
    // Primero, aplicamos el watermark y calculamos promedios por hora
    val hourlyAverages = df
      .withWatermark("event_time", "10 minutes")
      .groupBy(
        window($"event_time", "1 hour"),
        $"sensorId"
      )
      .agg(avg($"temperature").as("avg_temperature"))
      .select(
        $"window.start".cast("string").as("hour"),
        $"sensorId",
        $"avg_temperature"
      )

    // Luego, aplicamos CUBE a los resultados agregados
    hourlyAverages
      .groupBy($"hour", $"sensorId")
      .agg(
        avg($"avg_temperature").as("avg_temperature"),
        count("*").as("count")
      )
      .rollup($"hour", $"sensorId")
      .agg(
        avg($"avg_temperature").as("avg_temperature"),
        sum($"count").as("count")
      )
      .orderBy($"hour", $"sensorId")
  }

  // 3) Uso de BROADCAST JOIN:
  def enrichWithZones(sensorData: DataFrame, zonesData: DataFrame): DataFrame = {
    sensorData.join(
      broadcast(zonesData),
      sensorData("sensorId") === zonesData("sensor_id"),
      "left"
    ).select(
      sensorData("*"),
      zonesData("zona_nombre").as("zoneName"),
      zonesData("latitud"),
      zonesData("longitud")
    )
  }
}