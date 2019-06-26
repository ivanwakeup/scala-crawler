package practice

import akka.actor.Status.Failure

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.Success


case class TaxCut(reduction: Int)

object FuturePractice extends App {

  import concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  def demonstrateFutureInsidePromise(): Unit = {



    val taxCutPromise = Promise[TaxCut]()

    val taxCutFuture = taxCutPromise.future
    taxCutPromise.success(TaxCut(5))

    println(taxCutFuture.getClass)

    taxCutFuture.onComplete {
      case Success(value) => println(value.reduction)
      case _ => println("fuck")
    }


    Await.result(taxCutFuture, 2.seconds)
    //above, we can specify a value within a promise to complete the future with
    //given by taxCutPromise.success(sucess_val)
  }

  def demonstrateCompletingFutureOnSeparateThreadFromPromise(): Unit = {
    def redeemCampaign(): Future[TaxCut] = {
      val p = Promise[TaxCut]()
      //illustrate that we create and actually COMPUTE the future elsewhere
      Future {
        Thread.sleep(100)
        p.success(TaxCut(100))
        println("ok, now our taxcut is in place on the promise")
      }
      p.future
    }
    val fut = redeemCampaign()
    println("start await")
    fut.onComplete{
      case Success(cut) => println(cut.reduction)
      case _ => println("fuck no tax reduction")
    }
    val _ = Await.result(fut, 2.seconds)
    //println("end await")
  }

  demonstrateCompletingFutureOnSeparateThreadFromPromise()





}

