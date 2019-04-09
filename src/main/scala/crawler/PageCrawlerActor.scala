package crawler

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, Attributes}
import akka.stream.scaladsl.{Sink, Source}
import crawler.PageCrawlerActor.CrawlPage

import scala.concurrent.Future
import scala.util.Success

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
    }.log(s"streaming item passd through! ${url}").withAttributes(Attributes.logLevels(
      onElement = Logging.InfoLevel,
      onFinish = Logging.InfoLevel,
      onFailure = Logging.DebugLevel
    ))
    val sink = Sink.foreach[Future[HttpResponse]](
      resFut => resFut.onComplete({
        case Success(res) => res.entity.getDataBytes().take(10).to(Sink.foreach(println))
        case _ => println("fuck")
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
