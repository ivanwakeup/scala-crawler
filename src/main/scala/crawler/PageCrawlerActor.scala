package crawler

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import crawler.HtmlAnalyzerActor.Analyze
import crawler.PageCrawlerActor.{CrawlPage, CrawlerResponseBody}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.Success

class PageCrawlerActor(analyzerProps: Props) extends Actor with ActorLogging {


  implicit val ec = context.dispatcher
  implicit val mat = ActorMaterializer()

  private val htmlAnalyzer = context.actorOf(analyzerProps)

  override def receive: Receive = {
    case CrawlPage(url) => {
      val s = sender
      val source = crawlPage(url, s)
      source.runWith(Sink.ignore)
    }
  }


  private def crawlPage(url: String, sender: ActorRef): Source[Future[HttpResponse], NotUsed] = {
    val source: Source[Future[HttpResponse], NotUsed] = Source(List(url)).map { url =>
      val req = HttpRequest(uri = url)
      Http(context.system).singleRequest(req)
    }.map({
      futResponse =>
        futResponse.map { res =>
          buildResponseBody(res.entity.dataBytes, sender)
          res
        }
    })
    source
  }

  private def buildResponseBody(byteSource: Source[ByteString, Any], sender:ActorRef): Unit = {
    val result = ListBuffer[String]()
    val completion = byteSource.runWith(Sink.foreach { byteString =>
       result.append(byteString.utf8String)
       htmlAnalyzer ! Analyze(byteString.utf8String)
      }
    )
    completion.onComplete({
      case Success(_) => sender ! CrawlerResponseBody(result.toList)
    })
  }

}

object PageCrawlerActor {
  case class CrawlPage(url: String)
  def props(htmlAnalyzerProps: Props) = {
    Props(classOf[PageCrawlerActor], htmlAnalyzerProps)
  }

  case class PageCrawlResponse()
  case class CrawlerResponseBody[A](response: Iterable[A])
  case class FoundEmails(emails: Seq[String])

}


trait Animal
