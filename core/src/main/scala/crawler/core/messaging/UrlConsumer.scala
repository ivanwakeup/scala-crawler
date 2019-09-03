package crawler.core.messaging

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util
import java.util.Properties

import akka.actor.ActorSystem
import crawler.core.conf.ConfigSupport
import crawler.core.data.UrlPayload
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.{ Deserializer, StringDeserializer }

import scala.collection.JavaConverters.mapAsJavaMap

class UrlConsumer()(implicit system: ActorSystem) extends Runnable with ConfigSupport {

  val config = kafkaConsumerConfig.getConfig("akka.kafka.consumer")
  val urlTopic = crawlerConfig.getString("url-topic")
  val schemaRegUrl = schemaRegConfig.getString("schema.registry.url")

  val deser = new KafkaAvroDeserializer(
    new CachedSchemaRegistryClient(schemaRegUrl, 100),
    mapAsJavaMap(schemaRegistrySettings)).asInstanceOf[Deserializer[GenericRecord]]

  private val kafkaConsumer = new KafkaConsumer[String, GenericRecord](UrlConsumer.consumerProps, new StringDeserializer, deser)
  kafkaConsumer.subscribe(util.Arrays.asList(urlTopic))

  private val queuer = new CrawlerQueuer()

  def consume(): Unit = {

    while (true) {
      Thread.sleep(50)
      println("consuming!!")
      val dur: Duration = Duration.of(100, ChronoUnit.MILLIS)
      val records = kafkaConsumer.poll(dur)
      val it = records.iterator()
      while (it.hasNext) {
        val record = it.next()
        println(s"offset = ${record.offset()}, key = ${record.key()}, value = ${record.value()}")
        val payload = UrlPayload.format.from(record.value)
        queuer.crawlUrls(Seq(payload))
      }
    }
  }

  override def run(): Unit = {
    consume()
  }

}

object UrlConsumer extends ConfigSupport {

  val consumerProps: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", kafkaSettings.getProperty("bootstrap.servers"))
    props.put("group.id", "scala-crawler-url-consumer")
    props.put("enable.auto.commit", "true")
    props.put("auto.commit.interval.ms", "1000")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props
  }

}
