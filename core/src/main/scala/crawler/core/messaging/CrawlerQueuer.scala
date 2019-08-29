package crawler.core.messaging

import akka.actor.ActorSystem
import crawler.core.data.UrlPayload
import crawler.core.messaging.PageCrawlerActor.CrawlPage
import com.redis._

/*
queues up urls to be crawled
 */
class CrawlerQueuer(sys: ActorSystem, redisClient: RedisClient) {

  private val registry = sys.actorOf(AnalyzerRegistryActor.props())

  def crawlUrls(payloads: Seq[UrlPayload]): Unit = {
    payloads.foreach { payload =>
      val supProps = AnalyzerSupervisorActor.props(registry, payload)
      val crawler = sys.actorOf(PageCrawlerActor.props(supProps))
      crawler ! CrawlPage(payload)
    }
  }

  val urlExists: (String, RedisClient) => Boolean = (url, client) => {
    client.get(url).fold(false)({_ => true})
  }

}
