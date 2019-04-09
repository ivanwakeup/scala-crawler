package crawler

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import crawler.PageCrawlerActor.{CrawlPage, CrawlerResponseBody, FoundWord}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.Success

class PageCrawlerActor extends Actor with ActorLogging {


  implicit val ec = context.dispatcher
  implicit val mat = ActorMaterializer()


  override def receive: Receive = {
    case CrawlPage(url) => {
      val source = crawlPage(url)
      source.runWith(Sink.ignore)
    }

    case FoundWord =>
      println("found what we wanted!!!")

    case CrawlerResponseBody(res) => log.debug(s"received payload like: ${res.take(10)}")
  }


  private def crawlPage(url: String): Source[Future[HttpResponse], NotUsed] = {
    val source: Source[Future[HttpResponse], NotUsed] = Source(List(url)).map { url =>
      val req = HttpRequest(uri = url)
      Http(context.system).singleRequest(req)
    }.map({
      futResponse =>
        futResponse.map { res =>
          buildResponseBody(res.entity.dataBytes)
          res
        }
    })
    source
  }

  private def findWords(string: ByteString): Unit = {
    if (string.utf8String.contains("hello")) {
      self ! FoundWord
    }
  }

  private def buildResponseBody(byteSource: Source[ByteString, Any]): Unit = {
    val result = ListBuffer[String]()
    val completion = byteSource.runWith(Sink.foreach { byteString =>
       result.append(byteString.utf8String)
       findWords(byteString)
      }
    )
    completion.onComplete({
      case Success(_) => self ! CrawlerResponseBody(result.toList)
    })
  }

}

object PageCrawlerActor {
  case class CrawlPage(url: String)
  def props() = {
    Props(classOf[PageCrawlerActor])
  }

  case class PageCrawlResponse()
  case class CrawlerResponseBody[A](response: Iterable[A])
  case object FoundWord
}
