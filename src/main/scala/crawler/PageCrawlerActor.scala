package crawler

import akka.actor.Actor
import crawler.PageCrawlerActor.CrawlPage

import akka.stream.scaladsl.Source
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

class PageCrawlerActor extends Actor {


  override def receive: Receive = {
    case CrawlPage(url) => crawlPage(url)
  }

  def crawlPage(url: String): Unit = {
    val source = Source(List(url)).map { url =>
      val response = HttpResponse().entity
    }
  }

}

object PageCrawlerActor {
  case class CrawlPage(url: String)
}
