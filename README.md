# PasteShare
Service for storing text blocks and share them with other users

For build this project you should add environments files:

`.env`

* `HOST` - host with port of Postgres instance
* `POSTGRES_DB` - name of database
* `POSTGRES_USERNAME` - username of pg user
* `POSTGRES_PASSWORD` - password of pg user
* `ADMIN_PASSWORD` - password for admin user in app
* `COOKIE_TOKEN_KEY` - password for admin user in app

### kafka
`.env.kafka`

* `KAFKA_BROKER_ID` - Unique identifier for a Kafka broker in a cluster.
* `KAFKA_CFG_ZOOKEEPER_CONNECT` - Connection string for Zookeeper in a Kafka cluster.
* `ALLOW_PLAINTEXT_LISTENER` - Determines if Kafka allows plaintext communication with clients.
* `KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP` - Maps listener names to security protocols.
* `KAFKA_CFG_LISTENERS` - Defines listeners with protocols, addresses, and ports for incoming connections.
* `KAFKA_CFG_ADVERTISED_LISTENERS` - External address advertised to clients for broker accessibility.
* `KAFKA_CFG_INTER_BROKER_LISTENER_NAME` - Name of the listener for inter-broker communication within the cluster.

### Run
1. install pg, gradle
2. configure pg: add user, create bd
3. docker compose -f kafka.docker-compose.yml -p pasteshare up -d
4. docker compose -f clickhouse.docker-compose.yml -p pasteshare up -d
5. configure clickhouse: docker compose -f clickhouse.docker-compose.yml exec clickhouse clickhouse-client, create tables, then: configure kafka engine with property: 
* `kafka_broker_list` = kafka:9092
* `kafka_topic_list` = pastes
* `kafka_group_name` = clickhouse
* `kafka_format` = JSONEachRow


