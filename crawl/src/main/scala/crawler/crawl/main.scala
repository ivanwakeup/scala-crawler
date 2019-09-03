package crawler.crawl

import akka.actor.ActorSystem
import crawler.core.messaging.{CrawlerQueuer, UrlStreamingConsumer}

object main extends App {

  implicit val sys = ActorSystem("crawl")
  val q = new CrawlerQueuer()
  val _ = new UrlStreamingConsumer(q)

}
