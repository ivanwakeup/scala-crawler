package crawler

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import crawler.PageCrawlerActor.CrawlPage
import org.scalatest.FlatSpecLike
import utils.TestResponder
import utils.TestResponder._


class PageCrawlerActorSpec extends TestKit(ActorSystem("test"))
  with FlatSpecLike
  with ImplicitSender {

  val pageCrawler = system.actorOf(TestResponder.props())

  "A PageCrawlerActor" should "respond with HTML from a page" in {
    pageCrawler ! CrawlPage("https://www.regular-expressions.info/email.html")

    import scala.concurrent.duration._
    expectMsgPF(4000.millis) {
      case TestMessageReceived => succeed
      case _ => fail()
    }

  }

}


