package config

import com.typesafe.config.ConfigFactory

object AppConfig {
  private val config = ConfigFactory.load()

  object Kafka {
    val bootstrapServers: String = config.getString("kafka.bootstrapServers")
    val temperatureHumidityTopic: String = config.getString("kafka.topics.temperature_humidity.name")
    val co2Topic: String = config.getString("kafka.topics.co2.name")
    val soilMoistureTopic: String = config.getString("kafka.topics.soil_moisture.name")
  }

  object Paths {
    val baseDir: String = config.getString("app.basedir")
    val checkpointSuffix: String = config.getString("app.checkpointSuffix")
  }

  object Tables {
    val rawTemperatureHumidity: String = config.getString("app.tables.rawTemperatureHumidity")
    val temperatureHumidityMerge: String = config.getString("app.tables.temperatureHumidityMerge")
  }

  def getPathForTable(tableName: String): String = s"${Paths.baseDir}$tableName"
  def getCheckpointPath(tableName: String): String = s"${getPathForTable(tableName)}${Paths.checkpointSuffix}"
}