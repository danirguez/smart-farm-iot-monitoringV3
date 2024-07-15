package services

import org.apache.spark.sql.{DataFrame, SparkSession, Row}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import io.delta.tables._
import java.io.File

class ZonesManager(implicit spark: SparkSession) {
  import spark.implicits._

  def loadZones(path: String): DataFrame = {
    val file = new File(path)

    if (!file.exists()) {
      return spark.createDataFrame(spark.sparkContext.emptyRDD[Row], zonesSchema)
    }

    try {
      val zonasDF = spark.read
        .option("multiline", "true")
        .json(path)

      println(s"Loaded zones DataFrame. Row count: ${zonasDF.count()}")
      processZonesDataFrame(zonasDF)
    } catch {
      case e: Exception =>
        println(s"Error loading zones file: ${e.getMessage}")
        e.printStackTrace()
        spark.createDataFrame(spark.sparkContext.emptyRDD[Row], zonesSchema)
    }
  }

  def updateZonesDelta(zonesDF: DataFrame, deltaPath: String): Unit = {
    if (zonesDF.isEmpty) {
      println("Warning: Zones DataFrame is empty. Skipping Delta table update.")
      return
    }

    if (DeltaTable.isDeltaTable(spark, deltaPath)) {
      val deltaTable = DeltaTable.forPath(spark, deltaPath)
      deltaTable.as("oldZones")
        .merge(zonesDF.as("newZones"), "oldZones.sensor_id = newZones.sensor_id")
        .whenMatched.updateAll()
        .whenNotMatched.insertAll()
        .execute()
    } else {
      zonesDF.write.format("delta").save(deltaPath)
    }
  }

  private val zonesSchema = StructType(Seq(
    StructField("zona_id", StringType),
    StructField("zona_nombre", StringType),
    StructField("sensor_id", StringType),
    StructField("sensor_nombre", StringType),
    StructField("latitud", DoubleType),
    StructField("longitud", DoubleType),
    StructField("tipo", StringType)
  ))

  private def processZonesDataFrame(zonasDF: DataFrame): DataFrame = {
    zonasDF
      .withColumn("zona", explode($"zonas"))
      .select(
        $"zona.id".as("zona_id"),
        $"zona.nombre".as("zona_nombre"),
        explode($"zona.sensores").as("sensor")
      )
      .select(
        $"zona_id",
        $"zona_nombre",
        $"sensor.id".as("sensor_id"),
        $"sensor.nombre".as("sensor_nombre"),
        $"sensor.latitud",
        $"sensor.longitud",
        $"sensor.tipo"
      )
  }
}