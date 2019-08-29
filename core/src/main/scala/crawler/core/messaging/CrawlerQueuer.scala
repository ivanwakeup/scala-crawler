package crawler.core.messaging

import akka.actor.ActorSystem
import crawler.core.data.UrlPayload
import crawler.core.messaging.PageCrawlerActor.CrawlPage
import com.redis._
import crawler.core.cache.UrlCache._

/*
queues up urls to be crawled
 */
class CrawlerQueuer(sys: ActorSystem) {

  private val registry = sys.actorOf(AnalyzerRegistryActor.props())

  def crawlUrls(payloads: Seq[UrlPayload]): Unit = {
    payloads.foreach { payload =>
      withRedisClient("localhost", 100) { client =>
        client.get(payload.url)
      }.foreach { _ =>
        val supProps = AnalyzerSupervisorActor.props(registry, payload)
        val crawler = sys.actorOf(PageCrawlerActor.props(supProps))
        crawler ! CrawlPage(payload)
      }
    }
  }

}
