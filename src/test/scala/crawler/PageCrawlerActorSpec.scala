package crawler

import akka.actor.ActorSystem
import akka.testkit.TestKit
import crawler.PageCrawlerActor.CrawlPage
import org.scalatest.FlatSpecLike

class PageCrawlerActorSpec extends TestKit(ActorSystem("test")) with FlatSpecLike {

  val pageCrawler = system.actorOf(PageCrawlerActor.props())

  "A PageCrawlerActor" should "return HTML from webpage" in {
    pageCrawler ! CrawlPage("https://doc.akka.io/docs/akka/current/actors.html")
    Thread.sleep(5000)
  }

}
