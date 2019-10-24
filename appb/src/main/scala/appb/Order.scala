package appb

import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.avro.{Protocol, Schema}

final case class Order(orderId: String, customerId: Long, orderState: String,
                       val productId: String, val quantity: Int, val unitPrice: Double)



