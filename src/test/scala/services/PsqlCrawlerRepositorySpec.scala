package services

import akka.testkit.ImplicitSender
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}
import slick.jdbc.H2Profile.api._

class PsqlCrawlerRepositorySpec extends FlatSpecLike with BeforeAndAfterAll {


  val setup = DBIO.seq(
    PsqlCrawlerRepository.crawlData.schema.create
  )

  override def beforeAll(): Unit = {

  }
  "A PsqlCrawlerRepository" should "insert records into a psql database in" in {

  }

}
