package crawler

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import crawler.PageCrawlerActor.CrawlPage
import org.scalatest.FlatSpecLike
import utils.TestReceiver


class PageCrawlerActorSpec extends TestKit(ActorSystem("test"))
  with FlatSpecLike
  with ImplicitSender {


  val pageCrawlerActor = system.actorOf(PageCrawlerActor.props(TestReceiver.props()))

  "A PageCrawlerActor" should "respond with HTML from a page" in {
    pageCrawlerActor ! CrawlPage("https://www.google.com/")
    import scala.concurrent.duration._
    expectMsgPF(4000.millis) {
      case _: Done => succeed
      case _ => fail("PageCrawlerActorSpec received unexpected response from initiating a crawl!")
    }
  }

}

