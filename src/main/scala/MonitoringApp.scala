import config.{AppConfig, SparkSessionWrapper}
import org.apache.spark.sql.streaming.Trigger
import services.{DataProcessor, KafkaService}
import utils.DataValidation
import org.apache.log4j.{Level, Logger}

object MonitoringApp extends App with SparkSessionWrapper {
  import spark.implicits._

  // Configurar logging
  Logger.getRootLogger.setLevel(Level.OFF)
  Logger.getLogger("MonitoringApp").setLevel(Level.INFO)

  // Inicializar servicios
  val kafkaService = new KafkaService()(spark)

  // Crear todos los tópicos necesarios
  kafkaService.createTopicIfNotExists(AppConfig.Kafka.temperatureHumidityTopic)
  kafkaService.createTopicIfNotExists(AppConfig.Kafka.co2Topic)
  kafkaService.createTopicIfNotExists(AppConfig.Kafka.soilMoistureTopic)

  // Procesar datos de temperatura y humedad
  val tempHumStream = kafkaService.readStream(AppConfig.Kafka.temperatureHumidityTopic)
    .as[(String, java.sql.Timestamp)]
    .map { case (value, timestamp) =>
      DataValidation.validateTemperatureHumidity(value, timestamp)
    }

  val dataProcessor = new DataProcessor()(spark)
  val tempHumDf = dataProcessor.processTemperatureHumidity(tempHumStream)

  // Escribir datos en bruto
  tempHumDf.writeStream
    .format("delta")
    .outputMode("append")
    .option("checkpointLocation", AppConfig.getCheckpointPath(AppConfig.Tables.rawTemperatureHumidity))
    .start(AppConfig.getPathForTable(AppConfig.Tables.rawTemperatureHumidity))

  // Agregar y mostrar promedios
  val avgTempHumDf = dataProcessor.aggregateData(tempHumDf, "1 minute", Seq("temperature", "humidity"))
  avgTempHumDf.writeStream
    .outputMode("complete")
    .format("console")
    .start()

  spark.streams.awaitAnyTermination()
}