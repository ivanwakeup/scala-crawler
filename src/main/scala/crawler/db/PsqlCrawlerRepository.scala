package crawler.db

import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}

class PsqlCrawlerRepository()(implicit ec: ExecutionContext) extends CrawlerRepository {

  val db = Database.forConfig("h2mem1")

  override def insert(crawlData: CrawlData*): Future[Int] = {
    val action = PsqlCrawlerRepository.crawlData ++= crawlData.map(data => (0, data.email, data.url))
    db.run(action).map{
      case Some(rows) => rows
      case None => 0
    }
  }

}



object PsqlCrawlerRepository{
  val crawlData = TableQuery[CrawlData]

  val insertQuery = crawlData returning crawlData

  class CrawlData(tag: Tag) extends Table[(Int, String, String)](tag, "CrawlData") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def email = column[String]("email")
    def url = column[String]("url")
    def * = (id, email, url)
  }

  val num: Int = 1
}
