package crawler.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import crawler.core.conf.ConfigSupport
import crawler.core.data.UrlPayload
import crawler.core.messaging.KafkaUrlProducer.KafkaUrlPayloadMessage
import crawler.core.messaging.{ AnalyzerRegistryActor, KafkaUrlProducer }

import scala.concurrent.duration._

object main extends App with ConfigSupport with SprayJsonSupport {

  implicit val system = ActorSystem("crawler-sys")
  implicit val timeout = 5.seconds

  val registry = system.actorOf(AnalyzerRegistryActor.props())

  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val urlProducer = KafkaUrlProducer.actorSourceNoAck()
  //val consumer = new UrlStreamingConsumer(system)

  val route = path("add-url") {
    post {
      entity(as[UrlPayload]) { payload =>
        //give url to kafka producer to send to kafka
        urlProducer ! KafkaUrlPayloadMessage(payload)
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }
  }

  val server = Http().bindAndHandle(route, "0.0.0.0", 8181)

  //return triggers app shutdown
  //  StdIn.readLine()
  //  server
  //    .flatMap(_.unbind())
  //    .onComplete(_ ⇒ system.terminate())

}

