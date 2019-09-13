package crawler.core.analysis

import akka.actor.{ActorLogging, Props}
import akka.kafka.scaladsl.Producer
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.util.ByteString
import crawler.core.conf.ConfigSupport
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

class HtmlAccumulatorProducer extends BaseAnalyzer with ConfigSupport with ActorLogging {

  implicit val mat = ActorMaterializer()(context.system)

  val producerSettings: ProducerSettings[String, String] =
    ProducerSettings(kafkaProducerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(kafkaSettings.getProperty("bootstrap.servers"))

  private val crawledUrlTopic = crawlerConfig.getString("crawled-result-topic")

  private val producer = producerSettings.createKafkaProducer()

  val q = Source.queue[String](100, OverflowStrategy.backpressure)
    .map { ele =>
      ProducerMessage.single(
        new ProducerRecord[String, String](crawledUrlTopic, metadata.payload.url, ele))
    }
    .via(Producer.flexiFlow(producerSettings, producer))
    .recover({ case e => throw e })
    .to(Sink.ignore)
    .run()

  override def analyze(bytes: ByteString): Future[Unit] = {
    q.offer(bytes.utf8String).flatMap(res => { println(res); Future.successful() })
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info(reason.getMessage)
    super.preRestart(reason, message)
  }

}

object HtmlAccumulatorProducer {
  def props(): Props = {
    Props(classOf[HtmlAccumulatorProducer])
  }
}
