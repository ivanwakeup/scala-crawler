package crawler

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import crawler.PageCrawlerActor.{CrawlPage, FoundWord}
import org.scalatest.FlatSpecLike

class PageCrawlerActorSpec extends TestKit(ActorSystem("test"))
  with FlatSpecLike
  with ImplicitSender {

  val pageCrawler = system.actorOf(PageCrawlerActor.props())

  "A PageCrawlerActor" should "respond with HTML from a page" in {
    pageCrawler ! CrawlPage("https://www.regular-expressions.info/email.html")


    import scala.concurrent.duration._
    expectMsgPF(4000.millis) {
      case FoundWord(word) => succeed
    }

  }

}
