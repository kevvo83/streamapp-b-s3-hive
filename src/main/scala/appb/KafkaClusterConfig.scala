package exps

import java.io.FileInputStream
import java.util
import java.util.{Properties => juProps}

import io.confluent.kafka.schemaregistry.client.rest.RestService
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs

trait KafkaClusterConfig {

  val kafkaClusterConfig: KafkaClusterConfig

  class KafkaClusterConfig(val genericPropertiesFile: String, val producerPropertiesFile: String) {

    assert(genericPropertiesFile != "")
    assert(producerPropertiesFile != "")

    lazy val props: juProps = new juProps()
    props.load(new FileInputStream(genericPropertiesFile))

    lazy val producerProps: juProps = new juProps()
    producerProps.load(new FileInputStream(producerPropertiesFile))

    // Kafka Common Properties
    lazy val kcProps: juProps = new juProps()
    kcProps.put(CommonClientConfigs.CLIENT_ID_CONFIG, "blahblah")
    kcProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, props.getProperty("CCLOUD_BROKERS"))
    kcProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
      Option(props.getProperty("CCLOUD_SECURITY_PROTOCOL")).getOrElse("SASL_SSL"))
    kcProps.put(SaslConfigs.SASL_MECHANISM,
      Option(props.getProperty("CCLOUD_SASL_MECH")).getOrElse("PLAIN"))

    val ccloud_access_key_id      = Option(props.getProperty("CCLOUD_ACCESS_KEY_ID")).getOrElse("")
    val ccloud_secret_access_key  = Option(props.getProperty("CCLOUD_SECRET_ACCESS_KEY")).getOrElse("")
    val ccloud_sasl_jaas          = s"""org.apache.kafka.common.security.plain.PlainLoginModule """ +
      s"""required username="${ccloud_access_key_id}" """ +
      s"""password="${ccloud_secret_access_key}";"""

    if ((ccloud_access_key_id != "") && (ccloud_secret_access_key != "")){
      kcProps.put(SaslConfigs.SASL_JAAS_CONFIG,ccloud_sasl_jaas)
    }

    // Schema Registry Properties
    kcProps.put("schema.registry.url", props.getProperty("CCLOUD_SCHEMA_REGISTRY"))

    val schema_reg_access_key_id      = Option(props.getProperty("CCLOUD_SCHEMA_REGISTRY_ACCESS_KEY_ID")).getOrElse("")
    val schema_reg_secret_access_key  = Option(props.getProperty("CCLOUD_SCHEMA_REGISTRY_SECRET_ACCESS_KEY")).
      getOrElse("")

    if ((schema_reg_access_key_id != "") && (schema_reg_secret_access_key != "")) {
      kcProps.put("basic.auth.credentials.source",
        Option(props.getProperty("CCLOUD_SCHEMA_REGISTRY_CRED_SOURCE")).getOrElse("USER_INFO"))
      kcProps.put("basic.auth.user.info", s"""${schema_reg_access_key_id}:${schema_reg_secret_access_key}""")
    }

    // Kafka Producer Properties
    lazy val kpProps: juProps = new juProps()
    kpProps.putAll(kcProps)
    kpProps.put(ProducerConfig.ACKS_CONFIG, "all")
    kpProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer])
    kpProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer])

    // Schema Registry Rest API
    val rs1: RestService = new RestService(s"${props.getProperty("CCLOUD_SCHEMA_REGISTRY")}")
    val headers = new util.HashMap[String, String]()
    headers.put("Authorization","Basic " +
      util.Base64.
        getEncoder().
        encodeToString(s"""${kcProps.getProperty("basic.auth.user.info")}""".getBytes()))
  }

}
