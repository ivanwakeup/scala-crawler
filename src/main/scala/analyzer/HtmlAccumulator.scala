package analyzer

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import akka.util.ByteString
import app.main.{bootstrapServers, system}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

class HtmlAccumulator extends BaseAnalyzer {

  val config = system.settings.config.getConfig("akka.kafka.producer")
  val producerSettings =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  //how do we ship each incoming byteString to the flow?
  val s = Source(List(metadata.url)).map(value => {
    println(value)
    new ProducerRecord[String, String](topic, value)
  })
    .runWith(Producer.plainSink(producerSettings))
    .recover({case e => throw e})

  val q = Source.queue[String](100, OverflowStrategy.backpressure)
    .map
  override def analyze(bytes: ByteString): Future[Unit] = {
    persistBytes(bytes)
    Future.successful()
  }

  def persistBytes(bytes: ByteString): Unit = {

  }

}
