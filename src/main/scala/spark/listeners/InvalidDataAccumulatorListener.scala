package spark.listeners

import app.MonitoringApp
import org.apache.spark.sql.streaming.StreamingQueryListener

import scala.Console.{BOLD, RESET}
// Listener para ver tras cada microbatch los datos inválidos: StreamingQueryListener

class InvalidDataAccumulatorListener extends StreamingQueryListener {
  override def onQueryProgress(event: StreamingQueryListener.QueryProgressEvent): Unit = {
    println(BOLD + s"-- Errores acumulados hasta el batch ${event.progress.batchId}: ${MonitoringApp.errorCounter.value}" + RESET)
  }

  override def onQueryStarted(event: StreamingQueryListener.QueryStartedEvent): Unit = {}

  override def onQueryTerminated(event: StreamingQueryListener.QueryTerminatedEvent): Unit = {}
}