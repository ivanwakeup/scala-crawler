package crawler

import akka.actor.{Actor, Props}
import analyzer.EmailFinder
import crawler.AnalyzerRegistry.GetAnalyzers

class AnalyzerRegistry extends Actor {

  //private val wordCounterActor = context.actorOf(Props())
  //private val emailFinderActor =
  //private val htmlParserActor = context.actorOf(Props())

  private val analyzers: Seq[Props] = Seq(
    EmailFinder.props()
  )

  override def receive: Receive = {
    case GetAnalyzers => sender ! analyzers
  }

}

object AnalyzerRegistry {
  sealed trait AnalyzerRegistryMessage
  case object GetAnalyzers extends AnalyzerRegistryMessage
  case class AnalyzersResponse(analyzers: Seq[Props]) extends AnalyzerRegistryMessage

  def props(): Props = {
    Props(classOf[AnalyzerRegistry])
  }
}