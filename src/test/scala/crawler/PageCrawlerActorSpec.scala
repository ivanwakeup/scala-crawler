package crawler

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import crawler.PageCrawlerActor.{CrawlPage, CrawlerResponseBody}
import org.scalatest.FlatSpecLike
import services.{CrawlData, CrawlerRepository}

import scala.concurrent.Future


class PageCrawlerActorSpec extends TestKit(ActorSystem("test"))
  with FlatSpecLike
  with ImplicitSender {

  val probe = TestProbe()

  val repo = TestRepository()

  val pageCrawler = system.actorOf(PageCrawlerActor.props(HtmlAnalyzerActor.props(repo)))

  "A PageCrawlerActor" should "respond with HTML from a page" in {
    pageCrawler ! CrawlPage("https://www.regular-expressions.info/email.html")

    import scala.concurrent.duration._
    expectMsgPF(4000.millis) {
      case CrawlerResponseBody(_) => succeed
    }

  }

}

case class TestRepository() extends CrawlerRepository {
  override def insert(any: CrawlData): Future[Unit] = Future.successful()
}
