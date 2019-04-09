package crawler

import akka.NotUsed
import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Attributes}
import akka.util.ByteString
import crawler.PageCrawlerActor.CrawlPage

import scala.concurrent.Future
import scala.util.Success
import scala.concurrent.duration._

class PageCrawlerActor() extends Actor {


  implicit val ec = context.dispatcher
  implicit val mat = ActorMaterializer()


  override def receive: Receive = {
    case CrawlPage(url) => {
      crawlPage(url)
    }
  }


  def crawlPage(url: String) = {
    val source: Source[Future[HttpResponse], NotUsed] = Source(List(url)).map { url =>
      val req = HttpRequest(uri = url)
      Http(context.system).singleRequest(req)
    }
    val sink = Sink.foreach[Future[HttpResponse]](
      resFut => resFut.onComplete({
        case Success(res) => {
          val time = 1000.millis
          res.entity.toStrict(time).onComplete({
            case Success(res) => println(res.data.utf8String)
          })
        }
      })(ec)
    )
    source.to(sink).run()
  }

}

object PageCrawlerActor {
  case class CrawlPage(url: String)
  def props() = {
    Props(classOf[PageCrawlerActor])
  }
}
