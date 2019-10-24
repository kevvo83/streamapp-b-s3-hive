# Kafka CheatSheet


### REST Calls Reference

#### POST an Order to the OrdersService `orders` endpoint

```
curl --header "Content-Type: application/json" \
   --request POST \
   --data '{"id":"1000", "customerId":999299191, "orderState":{"currentState":"INIT"}, "product":{"id":"2", "manufacturer":"Bosch", "countryOfOrigin":"Germany"}, "quantity":1, "unitPrice":20.1}' \
   http://localhost:9089/orders
```

#### GET the base URL
```
curl -X GET http://localhost:9089
```

### Kafkacat Reference 

#### List all topics and their partitions
```bash
kafkacat -b localhost:9092 -L
```

####


### bin/kafka-topics.sh Reference

#### Create a new topic

```
./kafka-topics.sh --create --bootstrap-server localhost:9092 \
    --replication-factor 1 --partitions 1 --topic orders
```


### Schema Registry Reference

For further documentation on the Confluent Schema Registry, refer to the [Confluent Website](https://docs.confluent.io/current/schema-registry/develop/api.html).


#### Query all subjects in the schema registry

```bash
curl -X GET  "http://localhost:8081/subjects"
```


#### Create a new Orders_k subject in the schema registry

```bash
curl --header "Content-type: application/json" \
    --data '{"schema": "{\"fields\":[{\"name\":\"id\",\"type\":\"string\"}],\"name\":\"key_order\",\"namespace\":\"app1.exps\",\"type\":\"record\"}"}' \
    localhost:8081/subjects/orders_k/versions
```

#### Create a new Orders_v subject in the schema registry

```bash
curl --header "Content-type: application/json" \
    --data '{"schema": "{\"fields\":[{\"name\":\"customerId\",\"type\":\"long\"},{\"name\":\"orderState\",\"type\":{\"fields\":[{\"name\":\"currentState\",\"type\":\"string\"}],\"name\":\"OrderStateRecord\",\"type\":\"record\"}},{\"name\":\"product\",\"type\":{\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"manufacturer\",\"type\":\"string\"},{\"name\":\"countryOfOrigin\",\"type\":\"string\"}],\"name\":\"ProductRecord\",\"type\":\"record\"}},{\"name\":\"quantity\",\"type\":\"int\"},{\"name\":\"unitPrice\",\"type\":\"double\"}],\"name\":\"Order\",\"namespace\":\"app1.exps\",\"type\":\"record\"}"}' \
    localhost:8081/subjects/orders_v/versions
```



#### What is a Subject in the Schema Registry?

A subject refers to the name in which the Schema is registered. For eg., a subject refers to _topicname-key_ or _topicname-value_.



#### Query all subjects in the Schema Registry








