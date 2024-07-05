package io

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.sql.Timestamp

object KafkaDataGenerator {
  private val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](KafkaDataGeneratorConfig.createProducerProperties())

  def sendData(topic: String, sensorId: String, value: Double, timestamp: Timestamp): Unit = {
    val message = formatMessage(topic, sensorId, value, timestamp)
    val record = new ProducerRecord[String, String](topic, "key", message)
    println(s"Sending data to topic $topic")
    producer.send(record)
  }

  private def formatMessage(topic: String, sensorId: String, value: Double, timestamp: Timestamp): String = {
    topic match {
      case "temperature_humidity" => s"$sensorId,$value,$value,$timestamp"
      case KafkaDataGeneratorConfig.co2Topic => s"$sensorId,$value,$timestamp"
      case "soil_moisture" => s"$sensorId,$value,$timestamp"
      case _ => throw new IllegalArgumentException("Invalid topic")
    }
  }

  def closeProducer(): Unit = {
    producer.close()
  }
}