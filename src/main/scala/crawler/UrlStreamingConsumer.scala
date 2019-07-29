package crawler

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

class UrlStreamingConsumer(system: ActorSystem)(implicit sys: ActorSystem) {

  val conf = ConfigFactory.load()
  val kafkaConfig = conf.getConfig("akka.kafka.consumer")
  val bootstrapServers = conf.getConfig("akka.kafka").getString("bootstrap.servers")
  val crawlerConfig = conf.getConfig("crawler")

  val urlTopic = crawlerConfig.getString("url-topic")
  val consumerGroup = crawlerConfig.getString("url-consumer-group")

  implicit val materializer = ActorMaterializer()

  val consumerSettings =
    ConsumerSettings(kafkaConfig, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId(consumerGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")


  val stream = Consumer.plainSource(consumerSettings, Subscriptions.topics(urlTopic))

  private val q = new CrawlerQueuer(system)

  stream.runWith(Sink.foreach(ele => {q.crawlUrls(Seq(ele.value()))}))

}
