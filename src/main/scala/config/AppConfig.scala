package config

import com.typesafe.config.{ConfigFactory, Config}
import java.io.File
import scala.util.Try

object AppConfig {
  private val config: Config = ConfigFactory.load() // parseFile(new java.io.File("src/main/resources/application.conf")).resolve()


  object Kafka {
    val bootstrapServers: String = config.getString("kafka.bootstrapServers")
    val temperatureHumidityTopic: String = config.getString("kafka.topics.temperature_humidity.name")
    val co2Topic: String = config.getString("kafka.topics.co2.name")
    val soilMoistureTopic: String = config.getString("kafka.topics.soil_moisture.name")
  }

  val zonesJsonPath: String = Try(new java.io.File("./src/main/resources/" + config.getString("app.zones.jsonPath")).getAbsolutePath).getOrElse("zones.json")
  val zonesDeltaPath: String = config.getString("app.zones.deltaPath")

  object Paths {
    val baseDir: String = getOrElse("app.basedir", "./tmp/")
    val checkpointSuffix: String = getOrElse("app.checkpointSuffix", "_chk")
  }

  object Tables {
    val rawTemperatureHumidity: String = getOrElse("app.tables.rawTemperatureHumidity", "raw_temperature_humidity_zone")
    val temperatureHumidityMerge: String = getOrElse("app.tables.temperatureHumidityMerge", "temperature_humidity_zone_merge")
  }

  private def getOrElse(path: String, default: String): String = {
    if (config.hasPath(path)) config.getString(path) else default
  }
}