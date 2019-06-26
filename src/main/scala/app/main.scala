package app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.{PathMatcher, PathMatcher0}
import crawler.PageCrawlerActor.CrawlPage
import crawler.{AnalyzerRegistryActor, AnalyzerSupervisorActor, CrawlerQueuer, PageCrawlerActor}

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

object main extends App {

  implicit val system = ActorSystem("crawler-sys")
  implicit val timeout = 5.seconds

  val registry = system.actorOf(AnalyzerRegistryActor.props())

  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val pages: List[String] = List(
    "https://www.regular-expressions.info/email.html",
    "https://www.regextester.com/99232",
    "https://docs.microsoft.com/en-us/dotnet/standard/base-types/how-to-verify-that-strings-are-in-valid-email-format"
  )

  val crawlerQ = new CrawlerQueuer(system)


  val route = path("crawl") {
    get {
      crawlerQ.crawlUrls(pages)
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "127.0.0.1", 8080)


  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

}
