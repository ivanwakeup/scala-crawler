package crawler.crawl

import akka.actor.ActorSystem
import crawler.core.messaging.UrlStreamingConsumer

object main extends App {

  implicit val sys = ActorSystem("crawl")
  val _ = new UrlStreamingConsumer()

}
