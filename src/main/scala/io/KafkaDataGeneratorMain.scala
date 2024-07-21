package io

import java.sql.Timestamp
// Se importa la clase Future para que se pueda ejecutar el método generateData en un hilo separado
import scala.concurrent.Future
// Se importa el ExecutionContext para que se pueda ejecutar el método generateData en un hilo separado
import scala.concurrent.ExecutionContext.Implicits.global

object KafkaDataGeneratorHelper {
  // Para que no bloquee el hilo principal, se ejecuta en un hilo separado
  def generateData(topics: Seq[String], withPrintedMessages: Boolean = true): Future[Unit] = Future {
    try {
      for (j <- 1 to 30000) {
        for (i <- 1 to 9) {
          for (topic <- topics) {
            val sensorId = if (j % 50 == 0 && i == 3) s"sensor-defective-$i" else s"sensor$i"
            val value = Math.random() * 100
            val timestamp = new Timestamp(System.currentTimeMillis())
            KafkaDataGenerator.sendData(topic, sensorId, value, timestamp, withPrintedMessages)
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
}

object KafkaDataGeneratorMain extends App {
  val topics = KafkaDataGeneratorConfig.topics
  KafkaDataGeneratorHelper.generateData(topics)
}