package crawler.conf

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}

sealed trait ConfigSupport {
   val baseConfig: Config = ConfigFactory.load()
   val crawlerConfig = baseConfig.getConfig("crawler")
}

trait KafkaConfigSupport extends ConfigSupport {

  val kafkaProducerConfig: Config = baseConfig.getConfig("akka.kafka.producer")
  val kafkaConsumerConfig: Config = baseConfig.getConfig("akka.kafka.consumer")

  val kafkaSettings = new Properties()
  baseConfig.getConfig("kafka-settings").entrySet().forEach({
    entry => kafkaSettings.setProperty(entry.getKey, entry.getValue.unwrapped().toString)
  })

}
