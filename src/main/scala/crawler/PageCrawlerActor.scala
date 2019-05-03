package crawler

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.client.ClusterClient.Publish
import akka.cluster.pubsub.DistributedPubSub
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import crawler.HtmlAnalyzerActor.Analyze
import crawler.PageCrawlerActor.{CrawlPage, CrawlerResponseBody}
import utils.ConfigValues

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.Success

class PageCrawlerActor(analyzerProps: Props) extends Actor with ActorLogging {


  implicit val ec = context.dispatcher
  implicit val mat = ActorMaterializer()

  val mediator = DistributedPubSub(context.system).mediator

  override def receive: Receive = {
    case CrawlPage(url) => crawlPage(url)
  }

  private def crawlPage(url: String): Unit = {

    val req = HttpRequest(uri = url)
    Http(context.system).singleRequest(req).map({
      response =>
        response.entity.dataBytes.runWith(Sink.foreach({ byteString =>
          mediator ! Publish(ConfigValues.CRAWL_BYTES_TOPIC, byteString)
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

