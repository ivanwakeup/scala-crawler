package app

import java.util.Properties

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.kafka.{ConsumerSettings, ProducerSettings}
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import crawler.{AnalyzerRegistryActor, CrawlerQueuer}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer, StringSerializer}

import scala.concurrent.duration._
import scala.io.StdIn

object main extends App {

  implicit val system = ActorSystem("crawler-sys")
  implicit val timeout = 5.seconds

  val registry = system.actorOf(AnalyzerRegistryActor.props())

  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val pages: List[String] = List(
    "https://www.regular-expressions.info/email.html",
    "https://www.regextester.com/99232",
    "https://docs.microsoft.com/en-us/dotnet/standard/base-types/how-to-verify-that-strings-are-in-valid-email-format"
  )

  val crawlerQ = new CrawlerQueuer(system)


  val route = path("crawl") {
    get {
      crawlerQ.crawlUrls(pages)
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
    }
  }

  val addUrlsToCrawlRoute = path("add-url") {
    post {
      entity(as[String]) { url =>
        //give url to kafka producer to send to kafka
        sendUrlToKafka(url)
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }
  }

  val server = Http().bindAndHandle(route ~ addUrlsToCrawlRoute, "0.0.0.0", 8081)

  val config = system.settings.config.getConfig("akka.kafka.consumer")

  val bootstrapServers = "localhost:9092"

  val consumerSettings =
    ConsumerSettings(config, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")


  StdIn.readLine() // let it run until user presses return
  server
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.terminate())


  def sendUrlToKafka(url: String): Unit = {
    val config = system.settings.config.getConfig("akka.kafka.producer")
    println("sending to kafka")
    val producerSettings =
      ProducerSettings(config, new StringSerializer, new StringSerializer)
        .withBootstrapServers(bootstrapServers)

    val topic: String = "scala-crawler.urls"
    val s = Source(List(url)).map(value => {
      println(value)
      new ProducerRecord[String, String](topic, value)
    })
      .runWith(Producer.plainSink(producerSettings))
      .recover({case e => println(e); throw e})

  }

}
