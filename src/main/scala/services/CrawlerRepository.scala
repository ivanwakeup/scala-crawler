package services

import scala.concurrent.Future

trait CrawlerRepository {
  def storeAny(any: Any): Future[Unit]
}
