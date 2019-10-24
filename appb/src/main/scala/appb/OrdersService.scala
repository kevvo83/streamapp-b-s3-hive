package appb

import java.io.FileInputStream
import java.time.{Clock, Instant, ZoneId}

import exps.KafkaClusterConfig
import org.slf4j.{Logger, LoggerFactory}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import java.util.{Properties => juUtil}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}

import scala.io.StdIn
import scala.util.{Failure, Success}

object OrdersService extends App with KafkaClusterConfig with CustomJsonSupport {

  val kafkaBrokerProps: String = Option(args(0)).getOrElse("")
  val ordersServiceProps: String = Option(args(1)).getOrElse("")

  assert(kafkaBrokerProps != "", "Kafka Broker Properties file must be passed")
  assert(ordersServiceProps != "", "Orders Service Properties file must be passed")

  System.setProperty("log4j.configuration",getClass.getResource("../log4j.properties").toString)
  final val logger: Logger = LoggerFactory.getLogger("OrdersService")

  val kafkaClusterConfig: KafkaClusterConfig = new KafkaClusterConfig(kafkaBrokerProps)
  val ordersServiceConfig: juUtil = new juUtil()
  ordersServiceConfig.load(new FileInputStream(ordersServiceProps))

  implicit val system = ActorSystem("akka-http-rest-server")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val orderKeyValueSchemaPair = generateSchemas(getClass.getResource("/appb/Order.avpr").toURI)

  val routes: Route = {

    // Base Get Route
    path(pm="") {
      get {
        val clock: Clock = Clock.system(ZoneId.of("GMT"))
        val time: String = Instant.now(clock).toString
        complete(HttpEntity(ContentTypes.`application/json`, s"""Generic GET request sent at ${time}"""))
      }
    } ~
    path(pm="orders") {
      post {
        entity(as[Order]) {
          (specificorder:Order) => {

            var kp: KafkaProducer[GenericRecord, GenericRecord] =
              new KafkaProducer[GenericRecord, GenericRecord](kafkaClusterConfig.kpProps)

            val kr: Option[ProducerRecord[GenericRecord, GenericRecord]] =
                                                  generateProducerRecord(orderKeyValueSchemaPair._1,
                                                    orderKeyValueSchemaPair._2,
                                                    kafkaClusterConfig.props.getProperty("ORDERSTOPIC"),
                                                    specificorder)

            kp.send(kr.get)

            complete(HttpResponse(StatusCodes.OK,
              Nil,
              HttpEntity(ContentTypes.`application/json`,s"""""")))
          }
        }
      }
    }
  }

  val httpServerFuture = Http().
                          bindAndHandle(routes,
                            Option(ordersServiceConfig.getProperty("ordersServiceHost")).getOrElse("localhost"),
                            Integer.parseInt(Option(ordersServiceConfig.getProperty("ordersServicePort")).
                              getOrElse("9089")))

  httpServerFuture onComplete[Unit] (x => {
    x match {
      case Success(serverBinding) => logger.info(s"HTTP Server is Up and bound to ${serverBinding}")
      case Failure(exp) => {
        logger.info(s"HTTP Server could not startup due to: ${exp.getCause}")
      }
    }
  })

  StdIn.readLine() // Does 'Enter' terminate the string read from the Terminal? - Oh yeah, maybe it does!
  httpServerFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())

}
