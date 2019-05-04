package crawler

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.{ByteString, Timeout}
import crawler.AnalyzerRegistry.GetAnalyzers
import crawler.AnalyzerSupervisorActor.Distribute

import scala.concurrent.duration._

/*
we want this actor to:
1. request "interested" actors from the analyzer registry
    - while this is happening, we are "initializing"
2. once analyzer actor props are ready, create all of them
3. forward the incoming bytestring to each of the interested actors

 */
class AnalyzerSupervisorActor(analyzerRegistry: ActorRef) extends Actor {

  implicit val ec = context.dispatcher
  implicit val timeout = Timeout(5.seconds)


  private val analyzers: Seq[ActorRef] = scala.collection.mutable.Seq()

  /*
  how to ensure all interested actors are registered in response?

   */
  override def preStart(): Unit = {
    (analyzerRegistry ? GetAnalyzers).mapTo[AnalyzerRegistry.AnalyzersResponse].map { res =>
      res.analyzers.foreach({ props =>
        val nextAnalyzer:ActorRef = context.actorOf(props)
        analyzers :+ nextAnalyzer
      })
    }
  }

  override def receive: Receive = {
    case Distribute(byteString) => _
  }

}

object AnalyzerSupervisorActor {
  case class Distribute(byteString: ByteString)
}
