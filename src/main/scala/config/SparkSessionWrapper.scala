package config
import org.apache.spark.sql.SparkSession

trait SparkSessionWrapper {

  def appName: String
  def master: String
  def useDelta: Boolean
  def extraJavaOption: String
  def shufflePartitions: String

  lazy val spark: SparkSession = {
    val builder = SparkSession.builder()
      .appName(appName)
      .master(master)
      .config("spark.sql.shuffle.partitions", shufflePartitions)
      .config("spark.driver.extraJavaOptions", extraJavaOption)
      .config("spark.executor.extraJavaOptions", extraJavaOption)
      .config("spark.ui.showConsoleProgress", "false")
      .config("spark.sql.streaming.ui.enabled", "false")
      .config("spark.log.level", "OFF")
      .config("spark.sql.streaming.statefulOperator.checkCorrectness.enabled", "false")

    if (useDelta) {
      builder.config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
        .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
    }

    builder.getOrCreate()
  }
}