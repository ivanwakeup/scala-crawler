package crawler.data

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait CrawlerJsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val urlPayloadFormat = jsonFormat2(UrlPayload)
}