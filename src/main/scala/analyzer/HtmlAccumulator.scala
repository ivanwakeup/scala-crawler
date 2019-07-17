package analyzer

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Source}
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

  val q = Source.queue[String](100, OverflowStrategy.backpressure)
    .map {ele => new ProducerRecord[String, String](metadata.url, ele)}
      .recover({case e => throw e})
    .toMat(Producer.plainSink(producerSettings))(Keep.left)
    .run()

  override def analyze(bytes: ByteString): Future[Unit] = {
    q.offer(bytes.utf8String).flatMap(_ => Future.successful())
  }

}
