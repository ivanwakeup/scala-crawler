package crawler.core.analysis

import akka.actor.Props
import akka.util.ByteString

import scala.collection.mutable
import scala.concurrent.Future

class EmailFinder extends BaseAnalyzer {

  var emailSet: mutable.TreeSet[String] = scala.collection.mutable.TreeSet()

  override def analyze(bytes: ByteString): Future[Unit] = {
    val emails = EmailFinder.EMAIL_REGEX.findAllIn(bytes.utf8String)
    while (emails.hasNext) {
      val nxtEmail = emails.next()
      emailSet += nxtEmail
      println(s"found email $nxtEmail at ${metadata.url}")
    }
    Future.successful()
  }
}

object EmailFinder {
  def props(): Props = {
    Props(classOf[EmailFinder])
  }

  val EMAIL_REGEX = "[a-z0-9\\.\\-+_]+@[a-z0-9\\.\\-+_]+\\.com".r

  sealed trait EmailFinderMessage
  case object PrintEmails extends EmailFinderMessage
}
