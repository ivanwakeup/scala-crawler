package services

import scala.concurrent.Future

trait CrawlerRepository {
  def storeAny(any: AnyRef): Future[Unit]
}
