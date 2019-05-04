package crawler

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import crawler.AnalyzerSupervisorActor.Distribute
import crawler.PageCrawlerActor.CrawlPage

class PageCrawlerActor(analyzerProps: Props) extends Actor with ActorLogging {


  implicit val ec = context.dispatcher
  implicit val mat = ActorMaterializer()

  val analyzerSupervisor = context.actorOf(analyzerProps)

  override def receive: Receive = {
    case CrawlPage(url) => crawlPage(url)
  }

  private def crawlPage(url: String): Unit = {

    val req = HttpRequest(uri = url)
    Http(context.system).singleRequest(req).map({
      response =>
        response.entity.dataBytes.runWith(Sink.foreach({ byteString =>
          analyzerSupervisor ! Distribute(byteString)
        }))
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

