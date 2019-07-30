package crawler.messaging

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, _}
import akka.util.{ByteString, Timeout}
import crawler.analysis.BaseAnalyzer.{Analyze, AnalyzerMetadata}
import crawler.messaging.AnalyzerRegistryActor.GetAnalyzers
import crawler.messaging.AnalyzerSupervisorActor.Distribute

import scala.concurrent.Future
import scala.concurrent.duration._

/*
we want this actor to:
1. request "interested" actors from the analyzer registry
    - while this is happening, we are "initializing"
2. once analyzer actor props are ready, create all of them
3. forward the incoming bytestring to each of the interested actors

 */
class AnalyzerSupervisorActor(analyzerRegistry: ActorRef, url: String) extends Actor with ActorLogging {

  implicit val ec = context.dispatcher
  implicit val timeout = Timeout(5.seconds)


  private var analyzers: Seq[ActorRef] = Seq()

  /*
  how to ensure all interested actors are registered in response?
   */
  override def preStart(): Unit = {
    log.debug(s"total of ${analyzers.size} on initialization")
    (analyzerRegistry ? GetAnalyzers).mapTo[AnalyzerRegistryActor.AnalyzersResponse].map { res =>
      res.analyzers.foreach({ props =>
        val nextAnalyzer:ActorRef = context.actorOf(props)
        nextAnalyzer ! AnalyzerMetadata(url)
        analyzers = analyzers :+ nextAnalyzer
      })
      log.debug(s"${analyzers.size} analyzers now available")
    }
  }

  override def receive: Receive = {
    case Distribute(byteString) => pipe(distribute(byteString)) to sender
  }

  /*careful with the future here, messages could arrive out of order*/
  private[crawler] def distribute(byteString: ByteString): Future[Any] = {
    analyzers.foreach { analyzer =>
      analyzer ! Analyze(byteString)
    }
    Future.successful()
  }

}

object AnalyzerSupervisorActor {

  def props(analyzerRegistry: ActorRef, url: String): Props = {
    Props(classOf[AnalyzerSupervisorActor], analyzerRegistry, url)
  }

  sealed trait AnalyzerSupervisorActorMessage
  case class Distribute(byteString: ByteString) extends AnalyzerSupervisorActorMessage
  case object DistributionInitiated extends AnalyzerSupervisorActorMessage

}
