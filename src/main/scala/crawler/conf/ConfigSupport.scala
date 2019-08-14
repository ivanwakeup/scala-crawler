package crawler.conf

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigSupport {
   val baseConfig: Config = ConfigFactory.load()
   val crawlerConfig = baseConfig.getConfig("crawler")

  val kafkaProducerConfig: Config = baseConfig.getConfig("akka.kafka.producer")
  val kafkaConsumerConfig: Config = baseConfig.getConfig("akka.kafka.consumer")

  val kafkaSettings = new Properties()
  baseConfig.getConfig("kafka-settings").entrySet().forEach({
    entry => kafkaSettings.setProperty(entry.getKey, entry.getValue.unwrapped().toString)
  })

  val schemaRegConfig = baseConfig.getConfig("schema-registry")

  var schemaRegistrySettings: Map[String, String] = Map()
  schemaRegConfig.entrySet().forEach({
    entry => schemaRegistrySettings += (entry.getKey -> entry.getValue.unwrapped().toString)
  })

}