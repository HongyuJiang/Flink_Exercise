kafka-create-topic:
	docker-compose exec kafka-topics --create --topic item_v1 --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181

kafka-avro-console-producer:
	docker-compose exec schema-registry kafka-avro-console-producer \
	--broker-list kafka:19092 --topic item_v1 \
	--property value.schema='{"type":"record","name":"myrecord","fields":[{"name":"f1","type":"string"}, {"name": "f2", "type": "string"}]}'

kafka-avro-console-consumer:
	docker-compose exec schema-registry kafka-avro-console-consumer --topic item_v1 --bootstrap-server kafka:19092 --from-beginning  --partition 0

kafka-avro-console-consumer-2:
	docker-compose exec schema-registry kafka-avro-console-consumer --topic item_v1 --bootstrap-server kafka:19092 --from-beginning  --partition 0

kafka-avro-console-consumer-3:
	docker-compose exec kafka-topics kafka-avro-console-consumer --topic item_v1 --bootstrap-server kafka:19092 --from-beginning  --partition 0