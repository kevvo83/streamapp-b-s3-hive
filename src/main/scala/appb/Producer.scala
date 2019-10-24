package appb

import exps.KafkaClusterConfig

class Producer extends App with KafkaClusterConfig {

  val genPropsFile: String = Option(args(0)).getOrElse("")
  val producerPropsFile: String = Option(args(1)).getOrElse("")

  val kafkaClusterConfig: KafkaClusterConfig = new KafkaClusterConfig(genPropsFile, producerPropsFile)

}
