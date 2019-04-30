package services

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}
import slick.jdbc.H2Profile.api._


class PsqlCrawlerRepositorySpec extends TestKit(ActorSystem("test")) with FlatSpecLike with BeforeAndAfterAll {


  val setup = DBIO.seq(
    PsqlCrawlerRepository.crawlData.schema.create
  )
  val db = Database.forConfig("h2mem1")

  val cd = CrawlData("someguy@gmail.com", "this.com")

  import scala.concurrent.ExecutionContext.Implicits.global
  val repo = new PsqlCrawlerRepository()

  override def beforeAll(): Unit = {
    db.run(setup)
  }

  "A PsqlCrawlerRepository" should "insert records into a psql database in" in {
    repo.insert(cd)

    db.run(PsqlCrawlerRepository.crawlData.result).map(_.foreach({
      println(_)
    }))
  }

}
