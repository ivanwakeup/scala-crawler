package crawler

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import crawler.PageCrawlerActor.{CrawlPage, CrawlerResponseBody, FoundEmails}

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

    case FoundEmails(emails) =>
      log.debug(s"found emails: $emails")
      sender ! FoundEmails(emails)

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

  private def findEmails(string: ByteString): Option[Seq[String]] = {
    val emails = PageCrawlerActor.EMAIL_REGEX.findAllIn(string.utf8String.toCharArray)
    val result = ListBuffer[String]()
    while(emails.hasNext) {
      result.append(emails.next())
    }
    if(result.isEmpty) None else Some(result)
  }

  private def buildResponseBody(byteSource: Source[ByteString, Any]): Unit = {
    val result = ListBuffer[String]()
    val completion = byteSource.runWith(Sink.foreach { byteString =>
       findEmails(byteString).foreach{ emailSeq =>
         result.append(byteString.utf8String)
         self ! FoundEmails(emailSeq)
       }
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
  case class FoundEmails(emails: Seq[String])

  val EMAIL_REGEX = "[a-z0-9\\.\\-+_]+@[a-z0-9\\.\\-+_]+\\.com".r
}
