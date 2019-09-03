package crawler.core.messaging

import akka.Done
import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import PageCrawlerActor.CrawlPage
import crawler.core.data.UrlPayload
import org.scalatest.FlatSpecLike

import scala.concurrent.duration._


class PageCrawlerActorSpec extends TestKit(ActorSystem("test"))
  with FlatSpecLike
  with ImplicitSender {


  val pageCrawlerActor = system.actorOf(PageCrawlerActor.props(TestReceiver.props()))

  "A PageCrawlerActor" should "respond with HTML from a page" in {
    pageCrawlerActor ! CrawlPage(UrlPayload(100, "https://www.regular-expressions.info/email.html", None))
    expectMsgPF(4000.millis) {
      case _: Done => succeed
      case _ => fail("PageCrawlerActorSpec received unexpected response from initiating a crawl!")
    }
  }

}

class TestReceiver extends Actor {

  override def receive: Receive = {
    case _ => sender() ! Done
  }

}
object TestReceiver {
  def props(): Props = {
    Props(classOf[TestReceiver])
  }
}

