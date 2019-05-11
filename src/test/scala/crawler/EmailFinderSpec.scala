package crawler

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.ByteString
import analyzer.BaseAnalyzer.Analyze
import analyzer.EmailFinder
import org.scalatest.concurrent.Eventually
import org.scalatest.{FlatSpecLike, Matchers}

class EmailFinderSpec extends TestKit(ActorSystem("test"))
  with FlatSpecLike
  with ImplicitSender
  with Eventually
  with Matchers {



  val finderTestRef = TestActorRef[EmailFinder]

  "An Email Finder" should "not find an email" in {
    finderTestRef ! Analyze(ByteString("that"))

    eventually {
      finderTestRef.underlyingActor.emailSet shouldBe empty
    }

  }
  it should "find an email" in {
    finderTestRef ! Analyze(ByteString("him@gmail.com"))

    eventually {
      finderTestRef.underlyingActor.emailSet should not be empty
    }
  }

}

