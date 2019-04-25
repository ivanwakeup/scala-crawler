package crawler

import akka.actor.{Actor, ActorLogging}
import crawler.HtmlAnalyzerActor.Analyze
import services.CrawlerRepository

import scala.collection.mutable.ListBuffer

class HtmlAnalyzerActor(crawlerRepository: CrawlerRepository) extends Actor with ActorLogging {

  override def receive: Receive = {
    case Analyze(htmlString) => {
      val emailResult = findEmails(htmlString)
      emailResult.foreach({
        emails => emails.map{email => crawlerRepository.storeAny(email)}
      })
    }
  }


  private def findEmails(string: String): Option[Seq[String]] = {
    val emails = HtmlAnalyzerActor.EMAIL_REGEX.findAllIn(string.toCharArray)
    val result = ListBuffer[String]()
    while(emails.hasNext) {
      result.append(emails.next())
    }
    if(result.isEmpty) None else Some(result)
  }

}

object HtmlAnalyzerActor {

  sealed trait HtmlAnalyzerActorMessage
  case class Analyze(htmlString: String) extends HtmlAnalyzerActorMessage
  val EMAIL_REGEX = "[a-z0-9\\.\\-+_]+@[a-z0-9\\.\\-+_]+\\.com".r
}