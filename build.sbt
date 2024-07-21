import Versions._
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "smart-farm-iot-monitoring"
  )

enablePlugins(JmhPlugin)

// Importaciones necesarias para trabajar con Spark y Kafka

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql"            % Versions.spark % Provided,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % Versions.spark % Provided,
  // Delta Lake
  "io.delta"        %% "delta-spark"           % Versions.delta % Provided,
  "com.typesafe" % "config" % "1.4.3",
  // ScalaTest
  "org.scalatest"   %% "scalatest"             % "3.2.19" % Test,
  "org.apache.kafka" % "kafka-clients" % Versions.kafka,
  // Spark Fast Tests
  "com.github.mrpowers" %% "spark-fast-tests"  % "1.3.0" % Test,
  "org.openjdk.jmh" % "jmh-core" % "1.37",
  "org.openjdk.jmh" % "jmh-generator-annprocess" % "1.37"
)

// Configuración para JMH
Test / sourceDirectory := baseDirectory.value / "src" / "test"

Compile / resourceDirectory := baseDirectory.value / "src" / "main" / "resources"
