package crawler.messaging

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.scaladsl.Producer
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import crawler.conf.KafkaConfigSupport
import crawler.messaging.KafkaUrlProducer.UrlMessage
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer


class KafkaUrlProducer private()(implicit system: ActorSystem) extends KafkaConfigSupport {

  private implicit val materializer = ActorMaterializer()
  private implicit val ec = system.dispatcher

  private val urlTopic = crawlerConfig.getString("url-topic")
  private val producerSettings =
    ProducerSettings(kafkaProducerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(kafkaSettings.getProperty("bootstrap.servers"))

  private def actorSourceAck: ActorRef = {
    runViaKafka(kafkaSourceAck, producerSettings)
  }

  private def actorSourceNoAck: ActorRef = {
    runViaKafka(kafkaSourceNoAck, producerSettings)
  }

  private def runViaKafka[K, V](src: Source[ProducerMessage.Envelope[K, V, NotUsed], ActorRef], settings: ProducerSettings[K, V]): ActorRef = {
    src.via(
    Producer.flexiFlow[K, V, NotUsed](settings).recover{case e => throw e})
        .to(Sink.ignore)
      .run()
  }

  private def kafkaSourceAck: Source[ProducerMessage.Envelope[String, String, NotUsed], ActorRef] = {
    Source.actorRefWithAck[UrlMessage]("ack").map(message => {
      ProducerMessage.single(
        new ProducerRecord[String, String](urlTopic, message.url))
    })
  }

  private def kafkaSourceNoAck: Source[ProducerMessage.Envelope[String, String, NotUsed], ActorRef] = {
    Source.actorRef[UrlMessage](100, OverflowStrategy.dropTail).map(message => {
      ProducerMessage.single(
        new ProducerRecord[String, String](urlTopic, message.url))
    })
  }

}

object KafkaUrlProducer {

  case class UrlMessage(url: String)

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

