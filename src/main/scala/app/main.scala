package app

import akka.actor.ActorSystem
import crawler.PageCrawlerActor.CrawlPage
import crawler.{AnalyzerRegistryActor, AnalyzerSupervisorActor, PageCrawlerActor}

import scala.concurrent.duration._

object main extends App {

  implicit val system = ActorSystem("crawler-sys")
  implicit val timeout = 5.seconds

  val registry = system.actorOf(AnalyzerRegistryActor.props())



  val pages: List[String] = List(
    "https://www.regular-expressions.info/email.html",
    "https://www.regextester.com/99232",
    "https://docs.microsoft.com/en-us/dotnet/standard/base-types/how-to-verify-that-strings-are-in-valid-email-format"
  )

  pages.foreach { page =>
    val supervisorProps = AnalyzerSupervisorActor.props(registry, page)
    val pageCrawlerActor = system.actorOf(PageCrawlerActor.props(supervisorProps))
    pageCrawlerActor ! CrawlPage(page)
  }

  //need some way to signal that we're done? instead of just calling sys.exit

}
