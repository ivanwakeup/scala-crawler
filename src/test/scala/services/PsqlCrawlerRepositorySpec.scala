package services

import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll}
import slick.jdbc.H2Profile.api._


class PsqlCrawlerRepositorySpec extends AsyncFlatSpec with BeforeAndAfterAll {


  val setup = DBIO.seq(
    PsqlCrawlerRepository.crawlData.schema.create
  )
  val db = Database.forConfig("h2mem1")

  val cd = CrawlData("someguy@gmail.com", "this.com")

  val repo = new PsqlCrawlerRepository()

  override def beforeAll(): Unit = {
    db.run(setup)
  }

  "A PsqlCrawlerRepository" should "insert records into a psql database in" in {
    repo.insert(cd).map{ rows =>
      val result = PsqlCrawlerRepository.crawlData.result
      db.run(result).map{ tup =>
        assert(tup.head._2 == "someguy@gmail.com")
      }
    }.flatMap(assertion => assertion)
  }

}
