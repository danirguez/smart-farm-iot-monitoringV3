package app

import config.{AppConfig, SparkSessionWrapper}
import io.{KafkaDataGeneratorConfig, KafkaDataGeneratorHelper}
import org.apache.log4j.Logger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{DataFrame, Observation, SparkSession, functions}
import services.{DataProcessor, KafkaService, ZonesManager}
import spark.listeners.{InvalidDataAccumulatorListener, PerformanceMetricsListener}

import scala.Console.{BLUE, BOLD, RESET}

object MonitoringAppHelpers {
  def printAndLog(message: String)(implicit logger: org.apache.log4j.Logger): Unit = {
    println(BOLD + BLUE + message + RESET)
    logger.info(message)
  }

  def writeQueryToConsole(query: DataFrame, mode: String, interval: String, enabled: Boolean = true): Any = {
    if (enabled) {
      query
        .writeStream
        .outputMode(mode)
        .format("console")
        .trigger(Trigger.ProcessingTime(interval))
        .start()
    }
  }

}

object MonitoringApp extends App with SparkSessionWrapper {
  override val appName = "IoT Farm Monitoring"
  override val master = "local[*]"
  override val useDelta = true
  override val extraJavaOption = "-Dlog4j.configuration=file:src/main/resources/log4j2.properties"
  override val shufflePartitions = "10"

  implicit val sparkSession: SparkSession = spark

  // Registramos los listeners:
  sparkSession.streams.addListener(new InvalidDataAccumulatorListener())
  sparkSession.streams.addListener(new PerformanceMetricsListener())

  implicit val log: Logger = org.apache.log4j.LogManager.getLogger("MonitoringApp")
  // Para no tener que acordarme de lanza el KafkaDataGenerator lo llamo desde aquí:
  val topics = KafkaDataGeneratorConfig.topics
  // Paso false para que no imprima los mensajes en consola y así no se mezclen con los mensajes de la aplicación
  KafkaDataGeneratorHelper.generateData(topics, withPrintedMessages = false)

  // Lanzamos la lectura de los datos de Kafka:
  MonitoringAppHelpers.printAndLog("[KafkaService] Iniciamos el servicio para leer datos de Kafka")
  val kafkaService = new KafkaService()
  // Lanzamos el procesamiento de los datos:
  MonitoringAppHelpers.printAndLog("[DataProcessor] Iniciamos el procesamiento de los datos")
  val dataProcessor = new DataProcessor()
  // Lanzamos el servicio para gestionar las zonas:
  MonitoringAppHelpers.printAndLog("[ZonesManager] Iniciamos el servicio para gestionar las zonas")
  val zonesManager = new ZonesManager()

  // Cargar los datos de las zonas desde el JSON
  val zonesData = zonesManager.loadZones(AppConfig.zonesJsonPath)

  val sensorDataStream = kafkaService.readStream(AppConfig.Kafka.temperatureHumidityTopic)

  // Enriquecer los datos del sensor con la información de las zonas usando broadcast join
  MonitoringAppHelpers.printAndLog("Enriqueciendo los datos de los sensores con la información de las zonas...")
  val enrichedSensorData = dataProcessor.enrichWithZones(sensorDataStream, zonesData)

  // Calcular promedios de temperatura por sensor cada 10 minutos
  MonitoringAppHelpers.printAndLog("Calculando promedios de temperatura por sensor cada 10 minutos...")
  val averageTemperatures = dataProcessor.calculateAverageTemperature(enrichedSensorData)

  // Calcular agregaciones de temperatura usando ROLLUP
  MonitoringAppHelpers.printAndLog("Calculando agregaciones de temperatura usando ROLLUP...")
  val temperatureAggregations = dataProcessor.calculateTemperatureAggregations(enrichedSensorData)

  // Escribir los resultados de promedios de temperatura en la consola
  private val activarConsolas = false
  val avgTempQuery = MonitoringAppHelpers.writeQueryToConsole(averageTemperatures, "update", "10 seconds", enabled = activarConsolas)

  // Escribir los resultados de agregaciones de temperatura en la consola
  val aggTempQuery = MonitoringAppHelpers.writeQueryToConsole(temperatureAggregations, "complete", "1 minute", enabled = activarConsolas)

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
      // Mario - Intenta no hacer acciones en el driver como count o show ya que puede ralentizar el proceso
      // defectiveDF.groupBy("sensorId").count().show()


      // Mario - Lo he metido con un Listemer
      //println(s"Errores acumulados hasta el batch $batchId: ${errorCounter.value}")
    }
    .start()

  spark.streams.awaitAnyTermination()
}