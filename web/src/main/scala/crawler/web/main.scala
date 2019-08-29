package crawler.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.Directives._
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import crawler.core.conf.ConfigSupport
import crawler.core.data.UrlPayload
import crawler.core.messaging.KafkaUrlProducer
import crawler.core.messaging.KafkaUrlProducer.KafkaUrlPayloadMessage

import scala.concurrent.duration._

object main extends App with ConfigSupport with SprayJsonSupport {

  implicit val system = ActorSystem("crawler-sys")
  implicit val timeout = Timeout(5.seconds)

  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val urlProducerNoAck = KafkaUrlProducer.actorSourceNoAck()
  val urlProducerAck = KafkaUrlProducer.actorSourceAck()

  val route = path("add-url") {
    post {
      entity(as[UrlPayload]) { payload: UrlPayload =>
        payload.ack.fold({
          urlProducerNoAck ! KafkaUrlPayloadMessage(payload)
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "url produced, fire-n-forget-style!"))
        }) { _ =>
          val result = urlProducerAck ? KafkaUrlPayloadMessage(payload)
          complete(result.map(ele => ele.toString))
        }
      }
    }
  }

  val server = Http().bindAndHandle(route, "0.0.0.0", 8181)

}

