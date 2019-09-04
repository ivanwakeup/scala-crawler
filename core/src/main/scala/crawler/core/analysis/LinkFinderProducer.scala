package crawler.core.analysis

import akka.actor.Props
import akka.kafka.ProducerSettings
import akka.stream.ActorMaterializer
import akka.util.ByteString
import crawler.core.conf.ConfigSupport
import crawler.core.data.UrlPayload
import org.apache.kafka.common.serialization.StringSerializer
import org.jsoup.Jsoup

import scala.collection.mutable
import scala.concurrent.Future

class LinkFinderProducer extends BaseAnalyzer with ConfigSupport {

  implicit val mat = ActorMaterializer()(context.system)

  val producerSettings: ProducerSettings[String, String] =
    ProducerSettings(kafkaProducerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(kafkaSettings.getProperty("bootstrap.servers"))

  private val toCrawlTopic = crawlerConfig.getString("url-topic")

  override def analyze(bytes: ByteString): Future[Unit] = {
    val links = parseDocLinks(bytes.utf8String)
    Future.successful()
  }


  def parseDocLinks(htmlString: String): mutable.TreeSet[String] = {
    val parsedDoc = Jsoup.parse(htmlString)
    val links = parsedDoc.select("a[href]")
    val it = links.iterator()
    var linkList: mutable.TreeSet[String] = scala.collection.mutable.TreeSet()

    while(it.hasNext) {
      val link = it.next()
      linkList += link.text()
    }
    linkList
  }

  val calcCrawlDepth = (prevPayload: UrlPayload, nextPayload: UrlPayload) => {
    if(prevPayload.depth <= 0) {
      nextPayload.copy(-1, nextPayload.url, nextPayload.ack)
    }
    else {
      nextPayload.copy(prevPayload.depth - 1, nextPayload.url, nextPayload.ack)
    }
  }


}

object LinkFinderProducer {
  def props(): Props = {
    Props(classOf[LinkFinderProducer])
  }

}