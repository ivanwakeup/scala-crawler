package crawler.core.data

import com.sksamuel.avro4s.{ AvroName, AvroNamespace, AvroSchema, RecordFormat }
import org.apache.avro.Schema
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

sealed trait AvroEntity[T] {
  //has a way to build an avro schema from a case class
  //as well as a way to convert an entity to a generic record
  val format: RecordFormat[T]
  val schema: Schema
}

sealed trait CrawlerEntity[T] {
  implicit val jsonFormat: RootJsonFormat[T]
}

sealed trait CrawlerAvroEntity[T] extends AvroEntity[T] with CrawlerEntity[T]

@AvroName("UrlPayload")
@AvroNamespace("scala-crawler")
case class UrlPayload(depth: Int, url: String, ack: Option[Boolean])
object UrlPayloadProtocol extends DefaultJsonProtocol {
  val jsonFormat = jsonFormat3(UrlPayload.apply)
}

object UrlPayload extends CrawlerAvroEntity[UrlPayload] {
  val format = RecordFormat[UrlPayload]
  val schema = AvroSchema[UrlPayload]
  implicit val jsonFormat = UrlPayloadProtocol.jsonFormat
}

