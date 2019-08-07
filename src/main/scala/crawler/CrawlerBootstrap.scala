package crawler

import com.sksamuel.avro4s.AvroSchema
import crawler.conf.{ConfigSupport, SchemaRegistryConfigSupport}
import crawler.data.{CrawlerJsonSupport, UrlPayload}
import io.confluent.kafka.schemaregistry.client.{CachedSchemaRegistryClient, SchemaRegistryClient}
import spray.json._


trait CrawlerBootstrap extends SchemaRegistryConfigSupport {

  import com.sksamuel

  val urlPayloadSchema = AvroSchema[UrlPayload]

  val client = new CachedSchemaRegistryClient(conf.getString("schema.registry.url"), 1000)

  client.register("url-payload", urlPayloadSchema)

}



