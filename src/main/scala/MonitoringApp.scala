import config.{AppConfig, SparkSessionWrapper}
import org.apache.spark.sql.{DataFrame, SparkSession}
import services.{DataProcessor, KafkaService, ZonesManager}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.functions._

object MonitoringApp extends App with SparkSessionWrapper {
  implicit val sparkSession: SparkSession = spark
  import sparkSession.implicits._

  val kafkaService = new KafkaService()
  val dataProcessor = new DataProcessor()
  val zonesManager = new ZonesManager()

  // Cargar los datos de las zonas desde el JSON
  val zonesData = zonesManager.loadZones(AppConfig.zonesJsonPath)

  val sensorDataStream = kafkaService.readStream(AppConfig.Kafka.temperatureHumidityTopic)

  // Enriquecer los datos del sensor con la información de las zonas usando broadcast join
  val enrichedSensorData = dataProcessor.enrichWithZones(sensorDataStream, zonesData)

  // Calcular promedios de temperatura por sensor cada 10 minutos
  val averageTemperatures = dataProcessor.calculateAverageTemperature(enrichedSensorData)

  // Calcular agregaciones de temperatura usando ROLLUP
  val temperatureAggregations = dataProcessor.calculateTemperatureAggregations(enrichedSensorData)

  // Escribir los resultados de promedios de temperatura en la consola
  val avgTempQuery = averageTemperatures
    .writeStream
    .outputMode("update")
    .format("console")
    .trigger(Trigger.ProcessingTime("10 seconds"))
    .start()

  // Escribir los resultados de agregaciones de temperatura en la consola
  val aggTempQuery = temperatureAggregations
    .writeStream
    .outputMode("complete")
    .format("console")
    .trigger(Trigger.ProcessingTime("1 minute"))
    .start()

  // 4. Uso de Accumulators
  val errorCounter = spark.sparkContext.longAccumulator("ErrorCounter")

  val query = sensorDataStream
    .writeStream
    .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
      // Filtrar sensores defectuosos
      val defectiveDF = batchDF.filter(col("sensorId").rlike("^sensor-defective-\\d+$"))

      // Contar errores en este batch
      val errorCount = defectiveDF.count()
      errorCounter.add(errorCount)

      // Mostrar resultados
      defectiveDF.groupBy("sensorId").count().show()

      println(s"Errores acumulados hasta el batch $batchId: ${errorCounter.value}")
    }
    .start()

  spark.streams.awaitAnyTermination()
}