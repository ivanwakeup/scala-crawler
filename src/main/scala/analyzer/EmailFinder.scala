package analyzer
import akka.actor.Props
import akka.util.ByteString

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class EmailFinder extends BaseAnalyzer {

  override def analyze(bytes: ByteString): Future[Unit] = {
    Future {
      val result = ListBuffer[String]()
      val emails = EmailFinder.EMAIL_REGEX.findAllIn(bytes.utf8String)
      while(emails.hasNext) {
        result.append(emails.next())
      }
    }
  }
}

object EmailFinder {
  def props(): Props = {
    Props(classOf[EmailFinder])
  }
  val EMAIL_REGEX = "[a-z0-9\\.\\-+_]+@[a-z0-9\\.\\-+_]+\\.com".r
}
