package config

import org.scalatest.funsuite.AnyFunSuite

class ConfigTest extends AnyFunSuite {

  test("AppConfig debe cargar correctamente los valores de configuración") {
    assert(AppConfig.Kafka.bootstrapServers.nonEmpty)
    assert(AppConfig.Kafka.temperatureHumidityTopic.nonEmpty)
    assert(AppConfig.Kafka.co2Topic.nonEmpty)
    assert(AppConfig.Kafka.soilMoistureTopic.nonEmpty)
  }

  test("AppConfig debe generar rutas de tablas correctamente") {
    val tableName = "test_table"
    assert(AppConfig.getPathForTable(tableName).endsWith(tableName))
    assert(AppConfig.getCheckpointPath(tableName).endsWith(s"${tableName}${AppConfig.Paths.checkpointSuffix}"))
  }
}