import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import org.apache.avro.{Protocol, Schema}
import java.io.File
import java.net.URI

import scala.util.{Failure, Success, Try}
import scala.util.Try
import appb.Order
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.kafka.clients.producer.ProducerRecord

package object appb {

  def generateSchemas(schemaDef: URI): (Option[Schema], Option[Schema]) = {
    Try(new File(schemaDef)) match {
      case Success(file) => {
        val p: Protocol = Protocol.parse(file)
        val valueSchema: Schema = p.getType("appb.appb.OrderValue")
        val keySchema: Schema = p.getType("appb.appb.OrderKey")
        (Option(keySchema), Option(valueSchema))
      }
      case Failure(file) => (None, None)
    }
  }

  def generateProducerRecord(keySchema: Option[Schema],
                             valueSchema: Option[Schema],
                             targetTopic: String,
                             order: Order) = {

    (keySchema, valueSchema) match {
      case (None, None) => None
      case (ks: Some[Schema], vs: Some[Schema]) => {

        // Build the Generic Avro record
        val val_r: GenericRecord = new GenericData.Record(valueSchema.get)
        val key_r: GenericRecord = new GenericData.Record(keySchema.get)

        key_r.put("orderId", order.orderId)

        val_r.put("customerId", order.customerId)
        val_r.put("orderState", order.orderState)
        val_r.put("productId", order.productId)
        val_r.put("quantity", order.quantity)
        val_r.put("unitPrice", order.unitPrice)

        val kr = new ProducerRecord[GenericRecord, GenericRecord](targetTopic, null, key_r, val_r)
        Some(kr)
      }
      case (_, _) => None
    }

  }

}
