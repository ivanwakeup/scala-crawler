package crawler.data

import com.sksamuel.avro4s.{AvroName, AvroNamespace}

@AvroName("UrlPayload")
@AvroNamespace("scala-crawler")
case class UrlPayload(depth: Int, url: String)

