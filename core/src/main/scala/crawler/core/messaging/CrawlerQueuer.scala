package crawler.core.messaging

import akka.actor.ActorSystem
import crawler.core.cache.CachingClients._
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

  private val redisHost = redisConfg.getString("host")
  private val redisPort = redisConfg.getInt("port")

  def crawlUrls(payloads: Seq[UrlPayload]): Unit = {
    payloads.foreach { payload =>
      withRedisClient(redisHost, redisPort) { client =>
        client.get(payload.url)
      }.getOrElse { _ =>
        setCrawledAsync(payload.url).map { bool =>
          if(bool) {
            val supProps = AnalyzerSupervisorActor.props(registry, payload)
            val crawler = sys.actorOf(PageCrawlerActor.props(supProps))
            crawler ! CrawlPage(payload)
          }
          else throw RedisSetKeyException("couldn't set redis key!!")
        }
      }
    }
  }

  val setCrawledAsync: String => Future[Boolean] = (url: String) => Future {
    withRedisClient(redisHost, redisPort) { client =>
      client.set(url, true)
    }
  }

}

case class RedisSetKeyException(msg: String = null, cause: Throwable = null) extends RuntimeException
