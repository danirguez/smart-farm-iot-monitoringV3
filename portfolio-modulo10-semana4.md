# Portfolio: Spark Structured Streaming

## 1. Primera Aplicación de Spark Streaming

### Configuración básica de Spark Session
```scala
val spark = SparkSession.builder()
  .appName("PrimeraStructuredStreamingApp")
  .master(s"local[$numPartitions]")
  .config("spark.sql.adaptive.enabled", "false")
  .config("spark.sql.shuffle.partitions", numPartitions.toString)
  .config("spark.sql.streaming.checkpointLocation", "./checkpoint")
  .config("spark.sql.streaming.forceDeleteTempCheckpointLocation", "true")
  .getOrCreate()
```

### Lectura de datos en streaming
```scala
val readerSourceRate: DataStreamReader = spark.readStream
  .format("rate")
  .option("rampUpTime", 1)
  .option("numPartitions", numPartitions)
  .option("rowsPerSecond", 1000)

val dfSourceRate: DataFrame = readerSourceRate.load()
```

### Escritura de datos en streaming
```scala
val writerSinkConsole: DataStreamWriter[Row] = dfSourceRate.writeStream
  .format("console")
  .outputMode("append")
  .option("truncate", "false")
  .queryName("RateToConsole")

val queryStreamingToConsole: StreamingQuery = writerSinkConsole.start()
```

## 2. Observations y Listeners en Spark Streaming

### Definición de observaciones
```scala
val observation: Observation = Observation(ObservedMetricName)

val observedDataset: Dataset[Row] = input.observe(observation,
  functions.count(lit(1)).as(Rows),
  functions.max($"nr").as(MaxNr),
  functions.min($"nr").as(MinNr))
```

### Implementación de QueryExecutionListener
```scala
spark.listenerManager.register(new QueryExecutionListener {
  override def onSuccess(funcName: String, qe: QueryExecution, durationNs: Long): Unit = {
    qe.observedMetrics.get(ObservedMetricName).foreach(logResult)
  }

  override def onFailure(funcName: String, qe: QueryExecution, exception: Exception): Unit = {
    println(s"Error: $exception")
  }

  def logResult(batchTestRow: org.apache.spark.sql.Row): Unit = {
    println(s"$Rows  = " + batchTestRow.getAs[Int](Rows))
    println(s"$MaxNr = " + batchTestRow.getAs[Int](MaxNr))
    println(s"$MinNr = " + batchTestRow.getAs[Int](MinNr))
  }
})
```

## 3. Manejo de Errores y Retries en Streaming

### Configuración de retries
```scala
val spark: SparkSession = SparkSession.builder()
  .appName("Retries demo for continuous trigger")
  .master("local[3, 6]") // # threads, # retries
  .getOrCreate()
```

### Implementación de lógica con retries
```scala
spark.readStream.format("rate")
  .option("rowsPerSecond", "10")
  .option("numPartitions", "2")
  .load().as[(Timestamp, Long)]
  .map(nr => {
    if (nr._2 == 3) {
      throw new RuntimeException("nr 3 detected!")
    }
    nr._2
  })
  .writeStream
  .format("console")
  .start()
  .awaitTermination()
```

## 4. Transformaciones en Streaming DataLake

### Definición de estructura de DataLake
```scala
sealed trait DataLakeLayerType {
  def layerName: String
}

case object Inbound extends DataLakeLayerType {
  override def layerName: String = "inbound"
}

class DataLakeLayer(name: String, layerType: DataLakeLayerType) {
  val path: String = s"./data-lake/data-layers/$name/${layerType.layerName}"
  def checkPointPath: String = s"${path}_chk"
}
```

### Procesamiento de datos en streaming
```scala
val system1ReaderSourceRate = spark.readStream
  .format("rate")
  .option("rampUpTime", 1)
  .option("numPartitions", 1)
  .option("rowsPerSecond", 1000)
  .load()

val system1CsvDirSinkWriter = system1ReaderSourceRate.writeStream
  .format("csv")
  .option("header", "true")
  .option("checkpointLocation", System01.inbound.checkPointPath)
  .option("path", System01.inbound.path)
  .trigger(system1SinkWriterTrigger)
  .start()
```

## 5. Joins con Broadcast en Streaming

### Lectura de datos estáticos
```scala
val static = spark.read
  .format("delta")
  .load(StreamStaticJoinsDeltaConfig.staticDataPath)
  .withColumn("static_time", $"time".cast("timestamp"))

static.createOrReplaceTempView("static_data")
```

### Implementación de join con broadcast
```scala
val joined = events.join(
  broadcast(spark.table("static_data")),
  expr("event_time <= static_time + interval 10 minutes")
)
```

## 6. Window y Watermark en Streaming

### Definición de ventana y watermark
```scala
val averageTemperatures = dataProcessor.calculateAverageTemperature(enrichedSensorData)

def calculateAverageTemperature(df: DataFrame): DataFrame = {
  df.withWatermark("event_time", "5 minutes")
    .groupBy(
      window($"event_time", "10 minutes"),
      $"sensorId"
    )
    .agg(
      avg($"temperature").as("avg_temperature")
    )
}
```

## 7. Operaciones con Estado en Streaming

### Definición de función de actualización de estado
```scala
def updateSensorState(sensorId: String, inputs: Iterator[SensorData], oldState: GroupState[SensorAverage]): SensorAverage = {
  var state = if (oldState.exists) oldState.get else SensorAverage(sensorId, 0.0, 0)
  val (sumTemperature, count) = inputs.foldLeft((0.0, 0)) { case ((sum, cnt), data) =>
    (sum + data.temperature, cnt + 1)
  }
  state = SensorAverage(sensorId, (state.avgTemperature * state.count + sumTemperature) / (state.count + count), state.count + count)
  oldState.update(state)
  state
}
```

### Aplicación de operación con estado
```scala
val statefulSensorData = sensorDataParsed
  .groupByKey(_.sensorId)
  .mapGroupsWithState(GroupStateTimeout.NoTimeout)(updateSensorState)
```

## 8. Deduplicación en Streaming

### Implementación de lógica de deduplicación
```scala
def removeDuplicates(sensorId: String, inputs: Iterator[SensorData], oldState: GroupState[SensorState]): Iterator[SensorData] = {
  var state = if (oldState.exists) oldState.get else SensorState(sensorId, Double.MinValue, Long.MinValue)
  val uniqueInputs = inputs.filter { data =>
    if (!isDuplicate(state, data)) {
      state = SensorState(sensorId, data.temperature, data.timestamp)
      oldState.update(state)
      true
    } else {
      false
    }
  }
  uniqueInputs
}
```

### Aplicación de deduplicación en streaming
```scala
val deduplicatedData: Dataset[SensorData] = sensorDataParsed
  .groupByKey(_.sensorId)
  .flatMapGroupsWithState(OutputMode.Append, GroupStateTimeout.NoTimeout)(removeDuplicates)
```