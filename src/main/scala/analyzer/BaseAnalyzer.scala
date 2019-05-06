package analyzer

import akka.actor.Actor
import akka.util.ByteString
import analyzer.BaseAnalyzer.Analyze

import scala.concurrent.Future
import akka.pattern.pipe

abstract class BaseAnalyzer extends Actor {

  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case Analyze(bytes) => pipe(analyze(bytes)) to sender
  }


  def analyze(bytes: ByteString): Future[Unit] = {
    Future.successful()
  }

}

object BaseAnalyzer {

  case class Analyze(bytes: ByteString)

}
