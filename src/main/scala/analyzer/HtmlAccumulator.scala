package analyzer

import akka.actor.{ActorSystem, Props}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Source}
import akka.util.ByteString
import app.main.bootstrapServers
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

class HtmlAccumulator(system: ActorSystem) extends BaseAnalyzer {


  implicit val mat = ActorMaterializer()(system)

  val config = ConfigFactory.load().getConfig("akka.kafka.producer")
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

object HtmlAccumulator {
  def props(system: ActorSystem): Props = {
    Props(classOf[HtmlAccumulator], system)
  }
}
