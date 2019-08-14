package crawler.conf

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient

trait SchemaRegistrySupport extends ConfigSupport {

  val schemaRegistryClient: CachedSchemaRegistryClient
}
