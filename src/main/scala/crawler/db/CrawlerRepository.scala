package crawler.db

import scala.concurrent.Future

trait CrawlerRepository {
  def insert(crawlData: CrawlData*): Future[Int]
}

case class CrawlData(email: String, url: String)
