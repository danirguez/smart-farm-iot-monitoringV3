package services

import org.apache.spark.sql.SparkSession
import org.scalatest.funsuite.AnyFunSuite

class KafkaServiceTest extends AnyFunSuite {

  lazy val spark: SparkSession = SparkSession.builder()
    .master("local[2]")
    .config("spark.sql.shuffle.partitions", "2")
    .config("spark.default.parallelism", "2")
    .config("spark.ui.enabled", "false")
    .appName("KafkaServiceTest")
    .getOrCreate()

  test("KafkaService debe crear un DataFrame con las columnas correctas") {
    val kafkaService = new KafkaService()(spark)
    val df = kafkaService.readStream("test-topic")

    //assert(df.columns.contains("value"))
    //assert(df.columns.contains("timestamp"))

    assert(df.columns.contains("sensorId"))
    assert(df.columns.contains("temperature"))
    assert(df.columns.contains("humidity"))

  }
}