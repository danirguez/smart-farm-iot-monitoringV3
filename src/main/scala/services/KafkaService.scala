package services

import config.AppConfig
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.common.errors.TopicExistsException
import scala.jdk.CollectionConverters._
import scala.util.{Try, Success, Failure}

class KafkaService(implicit spark: SparkSession) extends Serializable {
  @transient private lazy val adminProps = new java.util.Properties()
  adminProps.put("bootstrap.servers", AppConfig.Kafka.bootstrapServers)
  @transient private lazy val adminClient = AdminClient.create(adminProps)

  def createTopicIfNotExists(topic: String): Unit = {
    val newTopic = new NewTopic(topic, 1, 1.toShort)
    Try(adminClient.createTopics(java.util.Collections.singleton(newTopic)).all().get()) match {
      case Success(_) => println(s"Topic $topic created successfully")
      case Failure(e: TopicExistsException) => println(s"Topic $topic already exists")
      case Failure(e) => println(s"Error creating topic $topic: ${e.getMessage}")
    }
  }

  def readStream(topic: String): DataFrame = {
    createTopicIfNotExists(topic)

    spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", AppConfig.Kafka.bootstrapServers)
      .option("subscribe", topic)
      .option("startingOffsets", "earliest")
      .load()
      .selectExpr("CAST(value AS STRING)", "CAST(timestamp AS TIMESTAMP)")
  }
}