package services

import models.TemperatureHumidityReading
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.functions._

class DataProcessorTest extends AnyFunSuite {

  lazy val spark: SparkSession = SparkSession.builder()
    .master("local[*]")
    .appName("DataProcessorTest")
    .getOrCreate()

  test("DataProcessor debe agregar correctamente la columna zoneId") {
    val dataProcessor = new DataProcessor()(spark)
    import spark.implicits._

    val testData = Seq(
      TemperatureHumidityReading("sensor1", 25.0, 60.0, java.sql.Timestamp.valueOf("2023-07-05 12:00:00"))
    ).toDS()

    val result = dataProcessor.processTemperatureHumidity(testData)

    assert(result.columns.contains("zoneId"))
    assert(result.filter($"sensorId" === "sensor1").select("zoneId").first().getString(0) == "zona1")
  }

  test("DataProcessor debe agregar correctamente los datos de temperatura y humedad") {
    val dataProcessor = new DataProcessor()(spark)
    import spark.implicits._

    val testData = Seq(
      TemperatureHumidityReading("sensor1", 25.0, 60.0, java.sql.Timestamp.valueOf("2023-07-05 12:00:00")),
      TemperatureHumidityReading("sensor1", 26.0, 61.0, java.sql.Timestamp.valueOf("2023-07-05 12:01:00"))
    ).toDS()

    val result = dataProcessor.aggregateData(testData.toDF(), "1 minute", Seq("temperature", "humidity"))

    assert(result.columns.contains("avg_temperature"))
    assert(result.columns.contains("avg_humidity"))
    val avgTemp = result.select("avg_temperature").first().getDouble(0)
    assert(avgTemp > 25.0 && avgTemp < 26.0)
  }
}
