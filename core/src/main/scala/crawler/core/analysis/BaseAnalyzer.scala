package crawler.core.analysis

import akka.actor.Actor
import akka.util.ByteString
import BaseAnalyzer.{ Analyze, AnalyzerMetadata }
import crawler.core.data.UrlPayload

import scala.concurrent.Future

abstract class BaseAnalyzer extends Actor {

  implicit val ec = context.dispatcher

  var metadata: AnalyzerMetadata = AnalyzerMetadata(UrlPayload(0, "no-url", None))

  override def receive: Receive = {
    case Analyze(bytes) => analyze(bytes)
    case am @ AnalyzerMetadata(_) => metadata = am
  }

  def analyze(bytes: ByteString): Future[Unit] = {
    Future.successful()
  }

}

object BaseAnalyzer {

  case class Analyze(bytes: ByteString)
  case class AnalyzerMetadata(payload: UrlPayload)
}
