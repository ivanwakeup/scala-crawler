package app

import akka.Done
import akka.actor.ActorSystem
import crawler.PageCrawlerActor.CrawlPage
import crawler.{AnalyzerRegistryActor, AnalyzerSupervisorActor, PageCrawlerActor}
import akka.pattern.ask

import scala.concurrent.duration._

object main extends App {

  implicit val system = ActorSystem("crawler-sys")
  implicit val timeout = 5.seconds

  val registry = system.actorOf(AnalyzerRegistryActor.props())
  val supervisorProps = AnalyzerSupervisorActor.props(registry)

  val pageCrawlerActor = system.actorOf(PageCrawlerActor.props(supervisorProps))


  val pages: List[String] = List(
    "https://www.regular-expressions.info/email.html",
    "https://www.regextester.com/99232"
  )

  pages.foreach { page =>
    pageCrawlerActor ! CrawlPage(page)
  }

  //need some way to signal that we're done? instead of just calling sys.exit

}
