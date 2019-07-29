package kafka

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import app.main.{bootstrapServers, system}
import com.typesafe.config.Config
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

class KafkaUrlProducer(kafkaConfig: Config) {

  private val adminClient = AdminClient.create(kafkaConfig.entrySet().)

  def sendUrlToKafka(url: String): Unit = {
    val config = system.settings.config.getConfig("akka.kafka.producer")
    val producerSettings =
      ProducerSettings(config, new StringSerializer, new StringSerializer)
        .withBootstrapServers(bootstrapServers)

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
