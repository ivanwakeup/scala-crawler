package utils

import akka.actor.{Actor, ActorLogging, Props}
import utils.TestResponder.TestMessageReceived

class TestResponder extends Actor {
  override def receive: Receive = {
    case _ => sender ! TestMessageReceived
  }
}
object TestResponder {
  def props(): Props = {
    Props(classOf[TestResponder])
  }
  case object TestMessageReceived
}


class TestReceiver extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg => log.info(s"received message: $msg")
  }
}
object TestReceiver {
  def props(): Props = {
    Props(classOf[TestReceiver])
  }
}

