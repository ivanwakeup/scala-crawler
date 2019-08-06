package crawler.data

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait CrawlerJsonSupport extends SprayJsonSupport {
  implicit val urlPayloadFormat = UrlPayloadProtocol.format
  implicit val schemaDefFormat = SchemaDefProtocol.format
}

object UrlPayloadProtocol extends DefaultJsonProtocol {
  val format = jsonFormat2(UrlPayload)
}

object SchemaDefProtocol extends DefaultJsonProtocol {
  val format = jsonFormat1(SchemaDef)
}