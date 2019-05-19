package app

import akka.actor.ActorSystem
import crawler.PageCrawlerActor.CrawlPage
import crawler.{AnalyzerRegistryActor, AnalyzerSupervisorActor, PageCrawlerActor}


object main extends App {

  implicit val system = ActorSystem("crawler-sys")
  val registry = system.actorOf(AnalyzerRegistryActor.props())
  Thread.sleep(1000)
  val supervisorProps = AnalyzerSupervisorActor.props(registry)

  val pageCrawlerActor = system.actorOf(PageCrawlerActor.props(supervisorProps))

  pageCrawlerActor ! CrawlPage("https://www.regular-expressions.info/email.html")


  Thread.sleep(5000)


}
