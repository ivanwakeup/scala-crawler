package crawler.core.analysis

import akka.actor.{ActorRef, Props}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import crawler.core.conf.ConfigSupport
import crawler.core.data.UrlPayload
import crawler.core.messaging.KafkaUrlProducer.KafkaUrlPayloadMessage
import org.jsoup.Jsoup

import scala.collection.mutable
import scala.concurrent.Future

class LinkFinderProducer(urlProducer: ActorRef) extends BaseAnalyzer with ConfigSupport {

  implicit val mat = ActorMaterializer()(context.system)

  override def analyze(bytes: ByteString): Future[Unit] = {
    val links = parseDocLinks(bytes.utf8String)
    links.foreach { link =>
      val newPayload = UrlPayload(-1, link, None)
      val withDepthUpdated = calcCrawlDepth(metadata.payload, newPayload)
      urlProducer ! KafkaUrlPayloadMessage(withDepthUpdated)
    }
    Future.successful()
  }

  def parseDocLinks(htmlString: String): mutable.TreeSet[String] = {
    val parsedDoc = Jsoup.parse(htmlString)
    val links = parsedDoc.select("a[href]")
    val it = links.iterator()
    var linkList: mutable.TreeSet[String] = scala.collection.mutable.TreeSet()

    while (it.hasNext) {
      val link = it.next()
      linkList += link.attr("abs:href")
    }
    linkList
  }

  val calcCrawlDepth = (prevPayload: UrlPayload, nextPayload: UrlPayload) => {
    if (prevPayload.depth <= 0) {
      nextPayload.copy(-1, nextPayload.url, nextPayload.ack)
    } else {
      nextPayload.copy(prevPayload.depth - 1, nextPayload.url, nextPayload.ack)
    }
  }

}

object LinkFinderProducer {
  def props(urlProducer: ActorRef): Props = {
    Props(classOf[LinkFinderProducer], urlProducer)
  }

}