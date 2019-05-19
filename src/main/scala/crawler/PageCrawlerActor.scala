package crawler

import akka.Done
import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import crawler.AnalyzerSupervisorActor.{Distribute, DistributionInitiated}
import crawler.PageCrawlerActor.CrawlPage

import scala.concurrent.Future
import scala.concurrent.duration._
class PageCrawlerActor(analyzerSupervisorProps: Props) extends Actor with ActorLogging {

  lazy val timeout: FiniteDuration = 20.milliseconds
  implicit val ec = context.dispatcher
  implicit val mat = ActorMaterializer()

  val analyzerSupervisor = context.actorOf(analyzerSupervisorProps)

  override def receive: Receive = {
    case CrawlPage(url) => crawlPage(url)
    case DistributionInitiated => context.parent ! DistributionInitiated
  }

  private def crawlPage(url: String): Future[Done] = {

    val req = HttpRequest(uri = url)

    val resStream: HttpResponse => Future[Done] = (res: HttpResponse) => {
      res.entity.dataBytes.runWith(Sink.foreach({ byteString =>
        analyzerSupervisor ! Distribute(byteString)
      }))
    }

    Http(context.system).singleRequest(req).map({
      resStream
    }).flatMap(fut => fut)
  }

}

object PageCrawlerActor {
  case class CrawlPage(url: String)
  def props(analyzerSupervisorProps: Props) = {
    Props(classOf[PageCrawlerActor], analyzerSupervisorProps)
  }

  case class PageCrawlResponse()
  case class CrawlerResponseBody[A](response: Iterable[A])
  case class FoundEmails(emails: Seq[String])

}

