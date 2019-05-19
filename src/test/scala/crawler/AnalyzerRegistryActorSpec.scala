package crawler

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import crawler.AnalyzerRegistryActor.GetAnalyzers
import org.scalatest.AsyncFlatSpecLike

import scala.concurrent.duration._

class AnalyzerRegistryActorSpec extends TestKit(ActorSystem("test"))
  with AsyncFlatSpecLike
  with ImplicitSender {


  val registry = system.actorOf(AnalyzerRegistryActor.props())
  implicit val timeout = Timeout(5.seconds)
  implicit val ec = system.dispatcher

  "An analyzer registry" should "return analyzers from the registry" in {
    (registry ? GetAnalyzers).mapTo[AnalyzerRegistryActor.AnalyzersResponse].map { res =>
      assert(res.analyzers.nonEmpty)
    }
  }

}

