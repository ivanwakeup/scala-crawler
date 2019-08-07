package crawler

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import crawler.data.{CrawlerJsonSupport, UrlPayload}
import crawler.messaging.KafkaUrlProducer.KafkaUrlPayloadMessage
import crawler.messaging.{AnalyzerRegistryActor, KafkaUrlProducer, UrlStreamingConsumer}

import scala.concurrent.duration._
import scala.io.StdIn


object main extends App with CrawlerBootstrap with CrawlerJsonSupport {

  implicit val system = ActorSystem("crawler-sys")
  implicit val timeout = 5.seconds

  val registry = system.actorOf(AnalyzerRegistryActor.props())

  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher


  val urlProducer = KafkaUrlProducer.actorSourceNoAck()
  val consumer = new UrlStreamingConsumer(system)


  val route = path("add-url") {
    post {
      entity(as[UrlPayload]) { payload =>
        //give url to kafka producer to send to kafka
        urlProducer ! KafkaUrlPayloadMessage(payload)
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }
  }

  val server = Http().bindAndHandle(route, "0.0.0.0", 8081)

  //return triggers app shutdown
  StdIn.readLine()
  server
    .flatMap(_.unbind())
    .onComplete(_ ⇒ system.terminate())


}



