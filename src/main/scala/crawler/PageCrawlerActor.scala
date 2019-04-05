package crawler

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Sink, Source}
import crawler.PageCrawlerActor.CrawlPage

import scala.concurrent.Future
import scala.util.Success

class PageCrawlerActor(tst: String)(implicit ec: ActorSystem) extends Actor {


  override def receive: Receive = {
    case CrawlPage(url) => crawlPage(url)
  }


  def crawlPage(url: String): Unit = {
    val source: Source[Future[HttpResponse], NotUsed] = Source(List(url)).map { url =>
      val req = HttpRequest(uri = url)
      Http().singleRequest(req)
    }
    val sink = Sink.foreach[Future[HttpResponse]](
      resFut => resFut.onComplete({
        case Success(res) => println(res)
      })(ec.dispatcher)
    )
    source.to(sink)
  }

}

object PageCrawlerActor {
  case class CrawlPage(url: String)
  def props(tst: String) = Props(classOf[PageCrawlerActor], tst)
}
