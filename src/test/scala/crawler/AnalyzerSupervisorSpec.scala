package crawler

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
import crawler.AnalyzerSupervisorActor.Distribute
import org.scalatest.FlatSpecLike


class AnalyzerSupervisorSpec extends TestKit(ActorSystem("test"))
  with FlatSpecLike
  with ImplicitSender {


  val registry = system.actorOf(AnalyzerRegistryActor.props())
  val supervisor = system.actorOf(AnalyzerSupervisorActor.props(registry, "testurl"))

  "A PageCrawlerActor" should "respond with HTML from a page" in {
    supervisor ! Distribute(ByteString("this"))
    import scala.concurrent.duration._
    expectMsgPF(4000.millis) {
      case _: Any => succeed
      case _ => fail("AnalyzerSupervisorSpec received unexpected response from trying to distribute a message!")
    }
  }

}

