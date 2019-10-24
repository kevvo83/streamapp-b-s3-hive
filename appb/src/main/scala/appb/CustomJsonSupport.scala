package appb

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol


trait CustomJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val orderFormatter = jsonFormat6[String, Long, String, String, Int, Double, Order](Order.apply)
}