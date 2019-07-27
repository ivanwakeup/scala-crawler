package analyzer

import akka.actor.{ActorSystem, Props}
import akka.kafka.ProducerMessage.MultiResultPart
import akka.kafka.scaladsl.Producer
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

class HtmlAccumulator(system: ActorSystem) extends BaseAnalyzer {

  implicit val mat = ActorMaterializer()(system)

  val conf = ConfigFactory.load()

  val kafkaConfig = conf.getConfig("akka.kafka.producer")

  val crawlerConfig = conf.getConfig("crawler")

  val producerSettings: ProducerSettings[String, String] =
    ProducerSettings(kafkaConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")

  private val crawledUrlTopic = crawlerConfig.getString("crawled-result-topic")

  val q = Source.queue[String](100, OverflowStrategy.backpressure)
    .map {ele => ProducerMessage.single(
      new ProducerRecord[String, String](crawledUrlTopic, metadata.url, ele)
    )}
      .via(Producer.flexiFlow(producerSettings))
      .map{
        case ProducerMessage.Result(metadata, ProducerMessage.Message(record, passThrough)) =>
          s"${metadata.topic}/${metadata.partition} ${metadata.offset}: ${record.value}"

        case ProducerMessage.MultiResult(parts, passThrough) =>
          parts
            .map {
              case MultiResultPart(metadata, record) =>
                s"${metadata.topic}/${metadata.partition} ${metadata.offset}: ${record.value}"
            }
            .mkString(", ")

        case ProducerMessage.PassThroughResult(passThrough) =>
          s"passed through"
      }.recover({case e=> throw e}).to(Sink.ignore)
    .run()

  override def analyze(bytes: ByteString): Future[Unit] = {
    //could we get out of order html here? the q.offer returns a future.
    q.offer(bytes.utf8String).flatMap(res  => {println(res); Future.successful()})
  }

}

object HtmlAccumulator {
  def props(system: ActorSystem): Props = {
    Props(classOf[HtmlAccumulator], system)
  }
}
