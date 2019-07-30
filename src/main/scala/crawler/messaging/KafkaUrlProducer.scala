package crawler.messaging

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import crawler.conf.{ConfigSupport, KafkaConfigSupport}
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer


class KafkaUrlProducer()(implicit system: ActorSystem) extends KafkaConfigSupport {

  private val adminClient = AdminClient.create(kafkaSettings)
  private implicit val materializer = ActorMaterializer()
  private implicit val ec = system.dispatcher

  def sendUrlToKafka(url: String): Unit = {
    val producerSettings =
      ProducerSettings(kafkaProducerConfig, new StringSerializer, new StringSerializer)
        .withBootstrapServers(kafkaSettings.getProperty("bootstrap.servers"))

    val topic: String = "scala-crawler.urls"
    val s = Source(List(url)).map(value => {
      new ProducerRecord[String, String](topic, value)
    })
      .runWith(Producer.plainSink(producerSettings))
      .recover({case e => println(e); throw e})

  }


  def createTopic(topicName: String): Unit = {

  }


}
