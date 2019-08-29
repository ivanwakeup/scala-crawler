package crawler.core.messaging

import akka.actor.{ Actor, ActorLogging, Props }
import crawler.core.analysis.{ EmailFinder, HtmlAccumulator }
import crawler.core.messaging.AnalyzerRegistryActor.{ AnalyzersResponse, GetAnalyzers }

class AnalyzerRegistryActor extends Actor with ActorLogging {

  private val analyzers: Seq[Props] = Seq(
    EmailFinder.props(),
    HtmlAccumulator.props(context.system))

  override def preStart(): Unit = {
    log.info(s"starting ${this.getClass.getCanonicalName} actor!!")
  }

  override def receive: Receive = {
    case GetAnalyzers => sender ! AnalyzersResponse(analyzers)
  }

}

object AnalyzerRegistryActor {
  sealed trait AnalyzerRegistryMessage
  case object GetAnalyzers extends AnalyzerRegistryMessage
  case class AnalyzersResponse(analyzers: Seq[Props]) extends AnalyzerRegistryMessage

  def props(): Props = {
    Props(classOf[AnalyzerRegistryActor])
  }
}