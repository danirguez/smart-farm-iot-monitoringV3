package services

import org.apache.spark.sql.SparkSession
import org.scalatest.funsuite.AnyFunSuite

class KafkaServiceTest extends AnyFunSuite {

  lazy val spark: SparkSession = SparkSession.builder()
    .master("local[*]")
    .appName("KafkaServiceTest")
    .getOrCreate()

  test("KafkaService debe crear un DataFrame con las columnas correctas") {
    val kafkaService = new KafkaService()(spark)
    val df = kafkaService.readStream("test-topic")

    assert(df.columns.contains("value"))
    assert(df.columns.contains("timestamp"))
  }
}