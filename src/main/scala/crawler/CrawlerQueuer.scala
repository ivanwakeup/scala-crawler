package crawler

import akka.actor.ActorSystem
import crawler.PageCrawlerActor.CrawlPage

/*
queues up urls to be crawled
 */
class CrawlerQueuer(sys: ActorSystem) {

  private val registry = sys.actorOf(AnalyzerRegistryActor.props())

  def crawlUrls(urls: Seq[String]): Unit = {
    urls.foreach { url =>
      val supProps = AnalyzerSupervisorActor.props(registry, url)
      val crawler = sys.actorOf(PageCrawlerActor.props(supProps))
      crawler ! CrawlPage(url)
    }
  }
  
}
