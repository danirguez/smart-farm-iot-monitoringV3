package config

import org.apache.spark.sql.SparkSession

trait SparkSessionWrapper {
  lazy val spark: SparkSession = {
    SparkSession.builder()
      .appName("IoT Farm Monitoring")
      .master("local[*]")
      .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
      .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
      .config("spark.sql.shuffle.partitions", "10")
      .config("spark.driver.extraJavaOptions", "-Dlog4j.configuration=file:src/main/resources/log4j2.properties")
      .config("spark.executor.extraJavaOptions", "-Dlog4j.configuration=file:src/main/resources/log4j2.properties")
      .config("spark.ui.showConsoleProgress", "false")
      .config("spark.sql.streaming.ui.enabled", "false")
      .config("spark.log.level", "OFF")
      .config("spark.sql.streaming.statefulOperator.checkCorrectness.enabled", "false")
      .config("spark.sql.shuffle.partitions", "2")
      .getOrCreate()
  }
}