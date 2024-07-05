package io

import java.sql.Timestamp

object KafkaDataGeneratorMain extends App {
  val topics = KafkaDataGeneratorConfig.topics

  try {
    for (j <- 1 to 30000) {
      for (i <- 1 to 9) {
        for (topic <- topics) {
          val sensorId = if (j % 50 == 0 && i == 3) s"sensor-defective-$i" else s"sensor$i"
          val value = Math.random() * 100
          val timestamp = new Timestamp(System.currentTimeMillis())
          KafkaDataGenerator.sendData(topic, sensorId, value, timestamp)
          if (!(j % 50 == 0 && i == 3)) {
            Thread.sleep(5)
          }
        }
      }
    }
  } catch {
    case e: Exception => println(s"Error during data generation and sending: ${e.getMessage}")
  } finally {
    KafkaDataGenerator.closeProducer()
  }
}