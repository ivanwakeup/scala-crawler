package crawler

import akka.actor.{Actor, Props}
import akka.util.ByteString
import crawler.AnalyzerSupervisorActor.Distribute


class AnalyzerSupervisorActor extends Actor {

  private val wordCounterActor = context.actorOf(Props())
  private val emailFinderActor = context.actorOf(Props())
  private val htmlParserActor = context.actorOf(Props())

  override def receive: Receive = {
    case Distribute(byteString) => _
  }
}

object AnalyzerSupervisorActor {
  case class Distribute(byteString: ByteString)
}
