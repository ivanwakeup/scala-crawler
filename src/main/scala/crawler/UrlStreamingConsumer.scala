package crawler

import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

class UrlStreamingConsumer {

  val conf = ConfigFactory.load()
  val bootstrapServers = conf.getConfig("akka.kafka").getString("bootstrap.servers")
  val urlTopic = conf.getConfig("crawler").getString("url-topic")
  val consumerSettings =
    ConsumerSettings(conf, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")


  val stream = Consumer.plainSource(consumerSettings, Subscriptions.topics(urlTopic))

  stream.runWith(Sink.foreach(println(_)))

}
