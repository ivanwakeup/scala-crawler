package crawler.core.messaging

import akka.actor.{ ActorLogging, ActorSystem }
import crawler.core.cache.CachingClients
import crawler.core.conf.ConfigSupport
import crawler.core.data.UrlPayload
import crawler.core.messaging.PageCrawlerActor.CrawlPage

import scala.concurrent.Future

/*
queues up urls to be crawled. ensures the url hasn't been crawled
already by first checking redis cache. Sets redis key before crawling.
 */
class CrawlerQueuer(sys: ActorSystem) extends ConfigSupport {

  private val registry = sys.actorOf(AnalyzerRegistryActor.props())
  implicit val ec = sys.dispatcher
  private val log = sys.log

  //use connection pool to help speed up throughput of incoming crawl requests
  private val pool = CachingClients.redisClientPool

  def crawlUrls(payloads: Seq[UrlPayload]): Unit = {
    payloads.foreach { payload =>
      pool.withClient { client =>
        val result = client.get(payload.url)
        result
      }.getOrElse {
        log.info("url not crawled, beginning...")
        setCrawledAsync(payload.url).map { bool =>
          if (bool) {
            val supProps = AnalyzerSupervisorActor.props(registry, payload)
            val crawler = sys.actorOf(PageCrawlerActor.props(supProps))
            crawler ! CrawlPage(payload)
          } else throw RedisSetKeyException("couldn't set redis key!!")
        }
      }
    }
  }

  val setCrawledAsync: String => Future[Boolean] = (url: String) => Future {
    pool.withClient { client =>
      client.set(url, true)
    }
  }

}

case class RedisSetKeyException(msg: String = null, cause: Throwable = null) extends RuntimeException
