package crawler

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import crawler.PageCrawlerActor.{CrawlPage, CrawlerResponseBody, FoundEmails}
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpecLike
import services.CrawlerRepository

import scala.concurrent.Future

class PageCrawlerActorSpec extends TestKit(ActorSystem("test"))
  with FlatSpecLike
  with ImplicitSender
  with MockFactory {

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
  override def storeAny(any: AnyRef): Future[Unit] = Future.successful()
}
