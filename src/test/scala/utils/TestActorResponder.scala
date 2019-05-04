package utils

import akka.actor.{Actor, Props}
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
