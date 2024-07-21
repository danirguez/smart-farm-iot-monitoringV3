package io

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.sql.Timestamp

object KafkaDataGenerator {
  private val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](KafkaDataGeneratorConfig.createProducerProperties())

  def sendData(topic: String, sensorId: String, value: Double, timestamp: Timestamp, withPrintedMessages: Boolean = true): Unit = {
    val message = formatMessage(topic, sensorId, value, timestamp)
    val record = new ProducerRecord[String, String](topic, "key", message)
    if (withPrintedMessages)
      println(s"Sending data to topic $topic")
    producer.send(record)
  }

  private def formatMessage(topic: String, sensorId: String, value: Double, timestamp: Timestamp): String = {
    topic match {
      case KafkaDataGeneratorConfig.temperatureHumidityTopic =>
        s"""{"sensorId":"$sensorId","temperature":$value,"humidity":$value,"timestamp":"$timestamp"}"""
      case KafkaDataGeneratorConfig.co2Topic =>
        s"""{"sensorId":"$sensorId","co2Level":$value,"timestamp":"$timestamp"}"""
      case KafkaDataGeneratorConfig.soilMoistureTopic =>
        s"""{"sensorId":"$sensorId","soilMoisture":$value,"timestamp":"$timestamp"}"""
      case _ => throw new IllegalArgumentException("Invalid topic")
    }
  }

  def closeProducer(): Unit = {
    producer.close()
  }
}