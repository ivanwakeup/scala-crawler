package crawler.core.analysis

import akka.actor.Actor
import akka.util.ByteString
import BaseAnalyzer.{Analyze, AnalyzerMetadata}

import scala.concurrent.Future

abstract class BaseAnalyzer extends Actor {

  implicit val ec = context.dispatcher

  var metadata: AnalyzerMetadata = AnalyzerMetadata("NOURL")

  override def receive: Receive = {
    case Analyze(bytes) => analyze(bytes)
    case am @ AnalyzerMetadata(url) => metadata = am
  }

  def analyze(bytes: ByteString): Future[Unit] = {
    Future.successful()
  }

}

object BaseAnalyzer {

  case class Analyze(bytes: ByteString)
  case class AnalyzerMetadata(url: String)

}
