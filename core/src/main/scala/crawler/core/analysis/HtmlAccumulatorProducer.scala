package crawler.core.analysis

import akka.actor.{ ActorSystem, Props }
import akka.kafka.ProducerMessage.MultiResultPart
import akka.kafka.scaladsl.Producer
import akka.kafka.{ ProducerMessage, ProducerSettings }
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.util.ByteString
import crawler.core.conf.ConfigSupport
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

class HtmlAccumulatorProducer extends BaseAnalyzer with ConfigSupport {

  implicit val mat = ActorMaterializer()(context.system)

  val producerSettings: ProducerSettings[String, String] =
    ProducerSettings(kafkaProducerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(kafkaSettings.getProperty("bootstrap.servers"))

  private val crawledUrlTopic = crawlerConfig.getString("crawled-result-topic")

  val q = Source.queue[String](100, OverflowStrategy.backpressure)
    .map { ele =>
      ProducerMessage.single(
        new ProducerRecord[String, String](crawledUrlTopic, metadata.payload.url, ele))
    }
    .via(Producer.flexiFlow(producerSettings))
    .recover({ case e => throw e })
    .to(Sink.ignore)
    .run()

  override def analyze(bytes: ByteString): Future[Unit] = {
    q.offer(bytes.utf8String).flatMap(res => { println(res); Future.successful() })
  }

}

object HtmlAccumulatorProducer {
  def props(): Props = {
    Props(classOf[HtmlAccumulatorProducer])
  }
}
