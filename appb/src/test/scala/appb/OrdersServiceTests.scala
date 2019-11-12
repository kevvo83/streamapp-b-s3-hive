package appb

import java.time.{Clock, Instant, ZoneId}

import org.scalatest.{FlatSpec, Matchers}
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._
import akka.util.ByteString
import appb.OrdersService.routes
import spray.json._

class OrdersServiceTests extends FlatSpec with Matchers with CustomJsonSupport with ScalatestRouteTest {

  val getRoute =
    get {
      concat(
        pathSingleSlash {
          val clock: Clock = Clock.system(ZoneId.of("GMT"))
          complete(HttpEntity(ContentTypes.`application/json`, GenericResponse(Instant.now(clock).toString).toJson.prettyPrint))
        }
      )
    }

  val postRoute =
    path("orders") {
      concat(
        post {
          entity(as[Order]) {
            order => complete(HttpEntity(ContentTypes.`application/json`, order.toJson.prettyPrint))
          }
        }
      )
    }

  val postedOrderData = ByteString(
    """
      |{
      | "orderId":"666999",
      | "customerId":999999,
      | "orderState":["INIT"],
      | "productId":"1",
      | "quantity":1,
      | "unitPrice":30.0
      |}
    """.stripMargin)
  val postRequest: HttpRequest = HttpRequest(method = HttpMethods.POST,
                                              uri = "/orders",
                                              entity = HttpEntity(MediaTypes.`application/json`, postedOrderData))


  behavior of "The Orders Service"

  it should "return a Json string with 2 fields for GET(/) Request " in {
    Get() ~> getRoute ~> check {
      val responseFields = responseAs[String].parseJson.asJsObject.fields
      assert(responseFields.contains("ts"), "Response Json doesn't contain field named 'ts'")
      assert(responseFields.contains("genericMessage"), "Response Json doesn't contain field named 'genericMessage'")
      //status shouldEqual (200)
    }
  }

  // TODO : Is this due to appropriate Reader/Writer not being implicitly available?
  // TODO : Worse case - don't use ENUM (in the avro schema) for the status field - just use STRING -
  //        need to move forward as Learning Lunch is in JAN 2020
  it should "return the successfully Unmarshalled Orders object on Post(/orders) Request " in {
    postRequest ~> postRoute ~> check {
      val response = responseAs[String].parseJson.asJsObject.fields
      assert(response.contains("orderId"))
    }
  }

}
