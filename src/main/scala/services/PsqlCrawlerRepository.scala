package services
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.H2Profile.api._

class PsqlCrawlerRepository()(implicit ec: ExecutionContext) extends CrawlerRepository {

  val db = Database.forConfig("h2mem1")

  override def insert(crawlData: CrawlData): Future[Unit] = {
    val action = PsqlCrawlerRepository.crawlData ++= Seq(
      (1, crawlData.email, crawlData.url)
    )
    action.map { insertRes =>
      Future.successful()
    }
    Future.successful()
  }

}



object PsqlCrawlerRepository{
  val crawlData = TableQuery[CrawlData]

  class CrawlData(tag: Tag) extends Table[(Int, String, String)](tag, "CrawlData") {
    def id = column[Int]("SUP_ID", O.PrimaryKey) // This is the primary key column
    def email = column[String]("SUP_NAME")
    def url = column[String]("STREET")
    def * = (id, email, url)
  }
}
