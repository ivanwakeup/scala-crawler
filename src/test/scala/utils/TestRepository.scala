package utils

import services.{CrawlData, CrawlerRepository}

import scala.concurrent.Future

case class TestRepository() extends CrawlerRepository {
  override def insert(any: CrawlData*): Future[Int] = Future.successful(1)
}

