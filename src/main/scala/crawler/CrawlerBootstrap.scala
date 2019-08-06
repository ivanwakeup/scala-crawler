package crawler

import crawler.data.{CrawlerJsonSupport, SchemaDef}
import spray.json._


trait CrawlerBootstrap extends CrawlerJsonSupport {

  //register the UrlPayload data type with schema registry
  val urlPayloadSchema = SchemaDef.toJson
  println(urlPayloadSchema.compactPrint)
}



