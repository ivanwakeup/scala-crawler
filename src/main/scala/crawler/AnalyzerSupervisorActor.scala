package crawler

import akka.actor.{Actor, ActorRef, Props}
import akka.util.{ByteString, Timeout}
import crawler.AnalyzerSupervisorActor.Distribute
import akka.pattern.ask
import scala.concurrent.duration._

/*
we want this actor to:
1. request "interested" actors from the analyzer registry
    - while this is happening, we are "initializing"
2. once analyzer actor props are ready, create all of them
3. forward the incoming bytestring to each of the interested actors

 */
class AnalyzerSupervisorActor(analyzerRegistry: ActorRef) extends Actor {

  private val wordCounterActor = context.actorOf(Props())
  private val emailFinderActor = context.actorOf(Props())
  private val htmlParserActor = context.actorOf(Props())

  implicit val ec = context.dispatcher
  implicit val timeout = Timeout(5.seconds)

  override def preStart(): Unit = {
    (analyzerRegistry ? GetAnalyzers)
  }

  override def receive: Receive = {
    case Distribute(byteString) => _
  }
}

object AnalyzerSupervisorActor {
  case class Distribute(byteString: ByteString)
}
