package crawler.messaging

import java.io.ByteArrayOutputStream

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.scaladsl.Producer
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.sksamuel.avro4s.{AvroOutputStream, Record, RecordFormat}
import crawler.CrawlerBootstrap
import crawler.conf.KafkaConfigSupport
import crawler.data.UrlPayload
import crawler.messaging.KafkaUrlProducer.KafkaUrlPayloadMessage
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, Serializer, StringSerializer}

class KafkaUrlProducer private()(implicit system: ActorSystem) extends KafkaConfigSupport with CrawlerBootstrap {

  private implicit val materializer = ActorMaterializer()
  private implicit val ec = system.dispatcher

  implicit val urlPayloadFormat = RecordFormat[UrlPayload]

  private val urlTopic = crawlerConfig.getString("url-topic")

  private val schemaRegProps: java.util.Map[String, String] = new java.util.HashMap[String, String]()
  schemaRegProps.put("key.subject.name.strategy", "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy")
  schemaRegProps.put("auto.register.schemas", "true")
  schemaRegProps.put("schema.registry.url", "http://localhost:8181")

  val serializer = new KafkaAvroSerializer(new CachedSchemaRegistryClient(), schemaRegProps).asInstanceOf[Serializer[GenericRecord]]
    .configure(schemaRegProps, false)

  private val producerSettings =
    ProducerSettings(kafkaProducerConfig, new StringSerializer, serializer)
      .withBootstrapServers(kafkaSettings.getProperty("bootstrap.servers"))

  private def actorSourceAck: ActorRef = {
    runViaKafka[String, GenericRecord](kafkaSourceAck, producerSettings)
  }

  private def actorSourceNoAck: ActorRef = {
    runViaKafka[String, GenericRecord](kafkaSourceNoAck, producerSettings)
  }

  private def runViaKafka[K, V](src: Source[ProducerMessage.Envelope[K, V, NotUsed], ActorRef], settings: ProducerSettings[K, V]): ActorRef = {
    src.via(
    Producer.flexiFlow[K, V, NotUsed](settings).recover{case e => throw e})
        .to(Sink.ignore)
      .run()
  }

  private def kafkaSourceAck: Source[ProducerMessage.Envelope[String, GenericRecord, NotUsed], ActorRef] = {
    Source.actorRefWithAck[KafkaUrlPayloadMessage]("ack").map(message => {
      val record = UrlPayload(1, "this.com")
      val gr: Record = urlPayloadFormat.to(record)
      ProducerMessage.single(
        new ProducerRecord[String, GenericRecord](urlTopic, gr))
    })
  }



  private def kafkaSourceNoAck: Source[ProducerMessage.Envelope[String, GenericRecord, NotUsed], ActorRef] = {
    Source.actorRef[KafkaUrlPayloadMessage](100, OverflowStrategy.dropTail).map(message => {
      val record = UrlPayload(1, "this.com")
      val gr: Record = urlPayloadFormat.to(record)
      ProducerMessage.single(
        new ProducerRecord[String, GenericRecord](urlTopic, gr))
    })
  }

}

object KafkaUrlProducer {

  case class KafkaUrlPayloadMessage(url: UrlPayload)

  case object KafkaUrlAck

  def actorSourceAck()(implicit system: ActorSystem): ActorRef = {
    val producer = new KafkaUrlProducer()(system)
    producer.actorSourceAck
  }

  def actorSourceNoAck()(implicit system: ActorSystem): ActorRef = {
    val producer = new KafkaUrlProducer()(system)
    producer.actorSourceNoAck
  }

}

