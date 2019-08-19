package crawler.core.messaging

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
import AnalyzerSupervisorActor.Distribute
import org.scalatest.FlatSpecLike

import scala.concurrent.duration._

class AnalyzerSupervisorSpec extends TestKit(ActorSystem("test"))
  with FlatSpecLike
  with ImplicitSender {


  val registry = system.actorOf(AnalyzerRegistryActor.props())
  val supervisor = system.actorOf(AnalyzerSupervisorActor.props(registry, "testurl"))

  "A PageCrawlerActor" should "respond with HTML from a page" in {
    supervisor ! Distribute(ByteString("this"))
    expectMsgPF(4000.millis) {
      case _: Any => succeed
      case _ => fail("AnalyzerSupervisorSpec received unexpected response from trying to distribute a message!")
    }
  }

}

