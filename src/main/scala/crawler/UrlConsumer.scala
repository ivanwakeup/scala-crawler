package crawler

import java.util
import java.util.Properties

import akka.actor.ActorSystem
import org.apache.kafka.clients.consumer.KafkaConsumer

class UrlConsumer(system: ActorSystem) {


  val config = system.settings.config.getConfig("akka.kafka.consumer")
  val urlTopic = system.settings.config.getConfig("crawler").getString("url-topic")
  val bootstrapServers = "localhost:9092"

  private val kafkaConsumer = new KafkaConsumer[String, String](UrlConsumer.consumerProps)
  kafkaConsumer.subscribe(util.Arrays.asList(urlTopic))


  def consume(): Unit = {

    println("consuming!!")

    while (true) {
      val records = kafkaConsumer.poll(100)
      val it = records.iterator()
      while (it.hasNext) {
        val record = it.next()
        println(s"offset = ${record.offset()}, key = ${record.key()}, value = ${record.value()}")
      }
    }
  }

  consume()

}

object UrlConsumer {

  val consumerProps: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("group.id", "scala-crawler-url-consumer")
    props.put("enable.auto.commit", "true")
    props.put("auto.commit.interval.ms", "1000")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props
  }

}
