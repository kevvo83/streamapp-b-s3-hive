import java.time.Instant

package object appb {

  final case class GenericResponse(ts: String, genericMessage: String = "Orders Service Akka-Http backend server")
  final case class Order(orderId: String, customerId: Long, orderState: String,
                         val productId: String, val quantity: Int, val unitPrice: Double)

}
