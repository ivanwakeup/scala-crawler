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
import akka.kafka.ConsumerSettings
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

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

  val config = system.settings.config.getConfig("akka.kafka.consumer")

  val bootstrapServers = "localhost:2181"

  val consumerSettings =
    ConsumerSettings(config, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

}
