package crawler.core.messaging

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem }
import akka.kafka.scaladsl.Producer
import akka.kafka.{ ProducerMessage, ProducerSettings }
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import com.sksamuel.avro4s.Record
import crawler.core.conf.ConfigSupport
import crawler.core.data.UrlPayload
import crawler.core.messaging.KafkaUrlProducer.{ KafkaUrlPayloadMessage, KafkaUrlProducedAck }
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ Serializer, StringSerializer }

import scala.collection.JavaConverters.mapAsJavaMap

class KafkaUrlProducer private ()(implicit system: ActorSystem)
  extends ConfigSupport {

  private implicit val materializer = ActorMaterializer()
  private implicit val ec = system.dispatcher

  private val urlTopic = crawlerConfig.getString("url-topic")
  private val schemaRegUrl = schemaRegConfig.getString("schema.registry.url")

  private val serializer = new KafkaAvroSerializer(
    new CachedSchemaRegistryClient(schemaRegUrl, 100),
    mapAsJavaMap(schemaRegistrySettings)).asInstanceOf[Serializer[GenericRecord]]

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
      Producer.flexiFlow[K, V, NotUsed](settings).recover { case e => throw e })
      .to(Sink.ignore)
      .run()
  }

  private def kafkaSourceAck: Source[ProducerMessage.Envelope[String, GenericRecord, NotUsed], ActorRef] = {
    Source.actorRefWithAck[KafkaUrlPayloadMessage](KafkaUrlProducedAck).map(formatUrlPayload)
  }

  private def kafkaSourceNoAck: Source[ProducerMessage.Envelope[String, GenericRecord, NotUsed], ActorRef] = {
    Source.actorRef[KafkaUrlPayloadMessage](100, OverflowStrategy.dropTail).map(formatUrlPayload)
  }

  val formatUrlPayload = (msg: KafkaUrlPayloadMessage) => {
    val gr: Record = UrlPayload.format.to(msg.urlPayload)
    ProducerMessage.single(
      new ProducerRecord[String, GenericRecord](urlTopic, gr))
  }

}

object KafkaUrlProducer {

  case class KafkaUrlPayloadMessage(urlPayload: UrlPayload)

  def actorSourceAck()(implicit system: ActorSystem): ActorRef = {
    val producer = new KafkaUrlProducer()(system)
    producer.actorSourceAck
  }

  def actorSourceNoAck()(implicit system: ActorSystem): ActorRef = {
    val producer = new KafkaUrlProducer()(system)
    producer.actorSourceNoAck
  }

  case object KafkaUrlProducedAck

}

