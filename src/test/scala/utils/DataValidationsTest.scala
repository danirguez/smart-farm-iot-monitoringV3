package utils

import org.scalatest.funsuite.AnyFunSuite
import java.sql.Timestamp

class DataValidationTest extends AnyFunSuite {

  test("validateTemperatureHumidity debe parsear correctamente los datos") {
    val testData = "sensor1,25.5,60.0"
    val timestamp = new Timestamp(System.currentTimeMillis())
    val result = DataValidation.validateTemperatureHumidity(testData, timestamp)

    assert(result.sensorId == "sensor1")
    assert(result.temperature == 25.5)
    assert(result.humidity == 60.0)
    assert(result.timestamp == timestamp)
  }

  test("validateCO2 debe parsear correctamente los datos") {
    val testData = "sensor1,400.0"
    val timestamp = new Timestamp(System.currentTimeMillis())
    val result = DataValidation.validateCO2(testData, timestamp)

    assert(result.sensorId == "sensor1")
    assert(result.co2Level == 400.0)
    assert(result.timestamp == timestamp)
  }
}