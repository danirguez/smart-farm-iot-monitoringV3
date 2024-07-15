package services

import config.AppConfig
import models.SensorReading
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StringType, StructType, DoubleType, TimestampType}

class KafkaService(implicit spark: SparkSession) {
  import spark.implicits._

  def readStream(topic: String): DataFrame = {
    spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", AppConfig.Kafka.bootstrapServers)
      .option("subscribe", topic)
      .option("startingOffsets", "earliest")
      .load()
      .select(
        col("timestamp").as("processing_time"),
        from_json(col("value").cast("string"), sensorSchema).alias("data")
      )
      .select(
        $"processing_time",
        $"data.sensorId",
        $"data.temperature",
        $"data.humidity",
        $"data.timestamp".cast("timestamp").as("event_time")
      )
  }

  private val sensorSchema = new StructType()
    .add("sensorId", StringType)
    .add("temperature", DoubleType)
    .add("humidity", DoubleType)
    .add("timestamp", StringType)
}