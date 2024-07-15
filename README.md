# Proyecto de Monitoreo IoT para Agricultura

Este proyecto implementa un sistema de monitoreo IoT para agricultura utilizando Apache Kafka y Apache Spark para procesar datos de sensores en tiempo real. Se han implementado nuevas funcionalidades y mejoras basadas en los requisitos del módulo 10, semana 2.

## Nuevas Funcionalidades

1. Manejo de zonas con JSON y Delta Lake
2. Procesamiento de datos de sensores en streaming
3. Deduplicación y manejo de datos tardíos
4. Cálculo de promedios de temperatura usando Watermark y Window
5. Agregaciones de temperatura usando ROLLUP
6. Uso de broadcast join para enriquecer datos
7. Implementación de contadores de errores con Accumulators
8. Implementación de una operación con estado que calcula el valor máximo de temperatura por sensor.

## Estructura del Proyecto

```
smart-farm-iot-monitoring/
│
├── src/
│   ├── main/
│   │   ├── scala/
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.scala
│   │   │   │   └── SparkSessionWrapper.scala
│   │   │   ├── io/
│   │   │   │   ├── KafkaDataGenerator.scala
│   │   │   │   ├── KafkaDataGeneratorConfig.scala
│   │   │   │   └── KafkaDataGeneratorMain.scala
│   │   │   ├── models/
│   │   │   │   ├── SensorData.scala
│   │   │   │   └── SensorReading.scala
│   │   │   ├── services/
│   │   │   │   ├── DataProcessor.scala
│   │   │   │   ├── KafkaService.scala
│   │   │   │   └── ZonesManager.scala
│   │   │   ├── utils/
│   │   │   │   └── DataValidation.scala
│   │   │   └── MonitoringApp.scala
│   │   └── resources/
│   │       ├── application.conf
│   │       └── zones.json
│   └── test/
│       └── scala/
│           └── [archivos de test]
├── project/
│   └── plugins.sbt
├── docker-compose.yml
├── build.sbt
└── README.md
```

## Descripción de los Componentes Principales

- `AppConfig.scala`: Gestiona la configuración global de la aplicación.
- `SparkSessionWrapper.scala`: Configura y crea la sesión de Spark.
- `KafkaDataGenerator.scala`: Genera y envía datos simulados de sensores a Kafka.
- `KafkaDataGeneratorConfig.scala`: Contiene la configuración para el generador de datos.
- `KafkaDataGeneratorMain.scala`: Punto de entrada para ejecutar el generador de datos.
- `SensorReading.scala`: Define estructuras de datos para las lecturas de sensores.
- `SensorData.scala`: Contiene clases de datos para los diferentes tipos de sensores.
- `DataProcessor.scala`: Procesa datos de sensores, realiza agregaciones y enriquecimiento.
- `KafkaService.scala`: Maneja la comunicación con Kafka y la lectura de streams.
- `ZonesManager.scala`: Gestiona la carga y actualización de datos de zonas.
- `DataValidation.scala`: Proporciona funciones de validación para datos de sensores.
- `MonitoringApp.scala`: Aplicación principal que orquesta el proceso de monitoreo.

## Requisitos

- Java 21
- sbt 1.10
- Scala 2.13.14
- Docker y Docker Compose

## Configuración del Entorno

1. Asegúrate de tener instalado el plugin de Scala en IntelliJ IDEA.
2. Configura el SDK de Java 21 en IntelliJ.
3. Instala sbt versión 1.10.
4. Configura el proyecto para usar Scala 2.13.14.

### Configuración en IntelliJ IDEA

1. Ve a `File > Project Structure`.
2. En `Project Settings > Project`, selecciona Java 21 SDK.
3. En `Project Settings > Modules`, elige el módulo Scala 2.13.14.
4. Abre `Run > Edit Configurations`.
5. Edita la configuración de tu aplicación Scala/Spark.
6. Marca la opción "Add dependencies with 'provided' scope to classpath".
7. En la sección `VM options`, añade las siguientes opciones:

```
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
--add-opens java.base/java.io=ALL-UNNAMED
--add-opens java.base/java.util.concurrent=ALL-UNNAMED
--add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED
--add-opens java.base/java.util.concurrent.locks=ALL-UNNAMED
--add-opens java.base/java.util.regex=ALL-UNNAMED
--add-opens java.base/java.util.stream=ALL-UNNAMED
--add-opens java.base/java.util.function=ALL-UNNAMED
--add-opens java.base/java.util.jar=ALL-UNNAMED
--add-opens java.base/java.util.zip=ALL-UNNAMED
--add-opens java.base/java.util.spi=ALL-UNNAMED
--add-opens java.base/java.lang.invoke=ALL-UNNAMED
--add-opens java.base/java.lang.reflect=ALL-UNNAMED
--add-opens java.base/java.net=ALL-UNNAMED
--add-opens java.base/java.nio=ALL-UNNAMED
--add-opens java.base/sun.nio.ch=ALL-UNNAMED
--add-opens java.base/sun.nio.cs=ALL-UNNAMED
--add-opens java.base/sun.security.action=ALL-UNNAMED
--add-opens java.base/sun.util.calendar=ALL-UNNAMED
--add-opens java.security.jgss/sun.security.krb5=ALL-UNNAMED
```

## Ejecución del Proyecto

1. Inicia los servicios de Kafka:
   ```
   docker-compose up -d
   ```

2. Ejecuta el generador de datos:
   ```
   sbt "runMain io.KafkaDataGeneratorMain"
   ```

3. Ejecuta la aplicación de monitoreo:
   ```
   sbt "runMain MonitoringApp"
   ```
