# Proyecto de Monitoreo IoT para Agricultura

Este proyecto implementa un sistema de monitoreo IoT para agricultura utilizando Apache Kafka y Apache Spark para procesar datos de sensores en tiempo real.

## Estructura del Proyecto

```
smart-farm-iot-monitoringV3/
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
│   │   │   │   └── KafkaService.scala
│   │   │   ├── utils/
│   │   │   │   └── DataValidation.scala
│   │   │   └── MonitoringApp.scala
│   │   └── resources/
│   │       └── application.conf
│   └── test/
│       └── scala/
│           ├── config/
│           │   ├── ConfigTest.scala
│           ├── services/
│           │   ├── DataProcessorTest.scala
│           │   └── KafkaServiceTest.scala
│           └── utils/
│               └── DataValidationTest.scala
├── docker-compose.yml
├── build.sbt
└── README.md
```

## Descripción del Código

El proyecto está escrito en Scala y utiliza Apache Kafka y Apache Spark. Aquí hay una breve descripción de los componentes principales:

- `AppConfig.scala`: Configura la aplicación cargando los ajustes desde el archivo de configuración.
- `SparkSessionWrapper.scala`: Crea y configura la sesión de Spark.
- `KafkaDataGenerator.scala`: Genera datos simulados de sensores y los envía a Kafka.
- `DataProcessor.scala`: Procesa los datos de los sensores, incluyendo la agregación y el cálculo de promedios.
- `KafkaService.scala`: Maneja la comunicación con Kafka, incluyendo la creación de tópicos y la lectura de streams.
- `MonitoringApp.scala`: La aplicación principal que orquesta todo el proceso de monitoreo.

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