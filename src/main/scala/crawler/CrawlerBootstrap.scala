package crawler

import com.sksamuel.avro4s.AvroSchema
import crawler.conf.SchemaRegistryConfigSupport
import crawler.data.UrlPayload
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient


trait CrawlerBootstrap extends SchemaRegistryConfigSupport {

  val urlPayloadSchema = AvroSchema[UrlPayload]

  val client = new CachedSchemaRegistryClient(conf.getString("schema.registry.url"), 1000)

  client.register("url-payload", urlPayloadSchema)

}



