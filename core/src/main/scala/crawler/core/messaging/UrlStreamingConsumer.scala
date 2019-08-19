package crawler.core.messaging

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import crawler.core.conf.ConfigSupport
import crawler.core.data.UrlPayload
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import collection.JavaConverters.mapAsJavaMap

class UrlStreamingConsumer(system: ActorSystem)(implicit sys: ActorSystem) extends ConfigSupport {

  val urlTopic = crawlerConfig.getString("url-topic")
  val consumerGroup = crawlerConfig.getString("url-consumer-group")
  val schemaRegUrl = schemaRegConfig.getString("schema.registry.url")

  implicit val materializer = ActorMaterializer()

  val deser = new KafkaAvroDeserializer(
    new CachedSchemaRegistryClient(schemaRegUrl, 100),
    mapAsJavaMap(schemaRegistrySettings)).asInstanceOf[Deserializer[GenericRecord]]

  val consumerSettings =
    ConsumerSettings(kafkaConsumerConfig, new StringDeserializer, deser)
      .withBootstrapServers(kafkaSettings.getProperty("bootstrap.servers"))
      .withGroupId(consumerGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")

  val stream = Consumer.plainSource(consumerSettings, Subscriptions.topics(urlTopic))

  private val q = new CrawlerQueuer(system)

  stream.runWith(Sink.foreach(ele => {
    val rec = UrlPayload.format.from(ele.value)
    q.crawlUrls(Seq(rec.url))
  }))

}
