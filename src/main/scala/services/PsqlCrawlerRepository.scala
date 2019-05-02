package services
import slick.dbio.Effect

import scala.concurrent.{Await, ExecutionContext, Future}
import slick.jdbc.H2Profile.api._
import slick.sql.FixedSqlAction
import scala.concurrent.duration._

class PsqlCrawlerRepository()(implicit ec: ExecutionContext) extends CrawlerRepository {

  val db = Database.forConfig("h2mem1")

  override def insert(crawlData: CrawlData): Future[Unit] = {
    val action: FixedSqlAction[Option[Int], NoStream, Effect.Write] = PsqlCrawlerRepository.crawlData ++= Seq(
      (1, crawlData.email, crawlData.url)
    )
    Await.result(db.run(action), 1000.millis)
    Future.successful()
  }

}



object PsqlCrawlerRepository{
  val crawlData = TableQuery[CrawlData]

  class CrawlData(tag: Tag) extends Table[(Int, String, String)](tag, "CrawlData") {
    def id = column[Int]("id", O.PrimaryKey) // This is the primary key column
    def email = column[String]("email")
    def url = column[String]("url")
    def * = (id, email, url)
  }

  val num: Int = 1
}
