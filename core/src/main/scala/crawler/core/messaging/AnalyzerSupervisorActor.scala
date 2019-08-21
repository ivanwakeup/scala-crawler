package crawler.core.messaging

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.pattern.{ ask, _ }
import akka.util.{ ByteString, Timeout }
import crawler.core.analysis.BaseAnalyzer.{ Analyze, AnalyzerMetadata }
import crawler.core.messaging.AnalyzerRegistryActor.GetAnalyzers
import crawler.core.messaging.AnalyzerSupervisorActor.Distribute

import scala.concurrent.Future
import scala.concurrent.duration._

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
        val nextAnalyzer: ActorRef = context.actorOf(props)
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
  private[core] def distribute(byteString: ByteString): Future[Any] = {
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
