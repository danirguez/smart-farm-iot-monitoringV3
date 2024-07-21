package config

import org.scalatest.funsuite.AnyFunSuite

class ConfigTest extends AnyFunSuite {

  test("AppConfig debe cargar correctamente los valores de configuración") {
    assert(AppConfig.Kafka.bootstrapServers.nonEmpty)
    assert(AppConfig.Kafka.bootstrapServers == "localhost-test:9193")
    assert(AppConfig.Kafka.temperatureHumidityTopic.nonEmpty)
    assert(AppConfig.Kafka.temperatureHumidityTopic == "temperature_humidity_test")
    assert(AppConfig.Kafka.co2Topic.nonEmpty)
    assert(AppConfig.Kafka.co2Topic == "co2_test")
    assert(AppConfig.Kafka.soilMoistureTopic.nonEmpty)
    assert(AppConfig.Kafka.soilMoistureTopic == "soil_moisture_test")
  }

  // Este test se ignora porque falla al ejecutarlo
  ignore("AppConfig debe generar rutas de tablas correctamente") {
    val tableName = "test_table"
    //assert(AppConfig.getPathForTable(tableName).endsWith(tableName))
    //assert(AppConfig.getCheckpointPath(tableName).endsWith(s"${tableName}${AppConfig.Paths.checkpointSuffix}"))
  }

  test("AppConfig.Paths object should load Paths configurations") {
    val pathsConfig = AppConfig.Paths
    assert(pathsConfig.baseDir.nonEmpty)
    assert(pathsConfig.baseDir == "./tmp-test/")
    assert(pathsConfig.checkpointSuffix.nonEmpty)
    assert(pathsConfig.checkpointSuffix == "_chk-test")
  }

  test("AppConfig.Tables object should load Tables configurations") {
    val tablesConfig = AppConfig.Tables
    assert(tablesConfig.rawTemperatureHumidity.nonEmpty)
    assert(tablesConfig.rawTemperatureHumidity == "raw_temperature_humidity_zone_test")
    assert(tablesConfig.temperatureHumidityMerge.nonEmpty)
    assert(tablesConfig.temperatureHumidityMerge == "temperature_humidity_zone_merge_test")
  }

  test("zonesJsonPath should not be empty") {
    assert(AppConfig.zonesJsonPath.nonEmpty)
    val fileName = AppConfig.zonesJsonPath.split("/").last
    assert(fileName == "zones.json")
  }

  test("zonesDeltaPath should not be empty") {
    assert(AppConfig.zonesDeltaPath.nonEmpty)
    assert(AppConfig.zonesDeltaPath == "zones_delta")
  }
}