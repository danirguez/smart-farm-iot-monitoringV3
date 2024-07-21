package spark.listeners

import org.apache.spark.sql.streaming.StreamingQueryListener
import org.apache.spark.sql.streaming.StreamingQueryListener.{QueryProgressEvent, QueryStartedEvent, QueryTerminatedEvent}

import scala.Console.{BOLD, RESET}

class PerformanceMetricsListener extends StreamingQueryListener {
  override def onQueryStarted(event: QueryStartedEvent): Unit = {}

  override def onQueryProgress(event: QueryProgressEvent): Unit = {
    println(BOLD +  "-- PerformanceMetricsListener:")
    println(s"   * Batch processing time: ${event.progress.durationMs.get("triggerExecution")}")
    println(s"   * Number of input rows: ${event.progress.numInputRows}" + RESET)
    println()
  }

  override def onQueryTerminated(event: QueryTerminatedEvent): Unit = {}
}