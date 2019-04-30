package services

import scala.concurrent.Future

trait CrawlerRepository {
  def insert(any: CrawlData): Future[Unit]
}

case class CrawlData(email: String, url: String)
