package app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import crawler.{AnalyzerRegistryActor, CrawlerQueuer, UrlConsumer, UrlStreamingConsumer}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.duration._
import scala.io.StdIn

object main extends App {

  implicit val system = ActorSystem("crawler-sys")
  implicit val timeout = 5.seconds

  val registry = system.actorOf(AnalyzerRegistryActor.props())

  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher


  val route = path("add-url") {
    post {
      entity(as[String]) { url =>
        //give url to kafka producer to send to kafka
        sendUrlToKafka(url)
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }
  }

  val bootstrapServers = "localhost:9092"
  val server = Http().bindAndHandle(route, "0.0.0.0", 8081)

  val consumer = new UrlStreamingConsumer(system)


  StdIn.readLine() // let it run until user presses return
  server
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.terminate())


}
