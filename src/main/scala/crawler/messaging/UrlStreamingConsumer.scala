package crawler.messaging

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import crawler.conf.KafkaConfigSupport
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

class UrlStreamingConsumer(system: ActorSystem)(implicit sys: ActorSystem) extends KafkaConfigSupport {

  val urlTopic = crawlerConfig.getString("url-topic")
  val consumerGroup = crawlerConfig.getString("url-consumer-group")

  implicit val materializer = ActorMaterializer()

  val consumerSettings =
    ConsumerSettings(kafkaConsumerConfig, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(kafkaSettings.getProperty("bootstrap.servers"))
      .withGroupId(consumerGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")


  val stream = Consumer.plainSource(consumerSettings, Subscriptions.topics(urlTopic))

  private val q = new CrawlerQueuer(system)

  stream.runWith(Sink.foreach(ele => {q.crawlUrls(Seq(ele.value()))}))

}
