package crawler

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util
import java.util.Properties

import akka.actor.ActorSystem
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util

class UrlConsumer(system: ActorSystem) extends Runnable {


  val config = system.settings.config.getConfig("akka.kafka.consumer")
  val urlTopic = system.settings.config.getConfig("crawler").getString("url-topic")
  val bootstrapServers = "localhost:9092"

  private val kafkaConsumer = new KafkaConsumer[String, String](UrlConsumer.consumerProps)
  kafkaConsumer.subscribe(util.Arrays.asList(urlTopic))

  private val queuer = new CrawlerQueuer(system)

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
        queuer.crawlUrls(Seq(record.value()))
      }
    }
  }

  override def run(): Unit = {
    consume()
  }

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
