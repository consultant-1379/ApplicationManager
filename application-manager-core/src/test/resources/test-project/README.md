# Introduction
flink-app this is base streaming template project generated from the AIA Portal.

# Prerequisites:

- Maven 3.0.0+
- Java 1.7
- Docker
- Vagrant

### Flow.xml

Kafka Input Attributes Usage:
:   **radio-stream** input stream context name
:   **kafka://radio_session_topic** represents Kafka Data Source and 'radio_sesssion_topic' represents Kafka Topic name
:   **group.id** represents Kafka Topic group id
:   **bootstrap.servers** Kafka bootstrap servers(comma seperated value)
:   **zookeeper.connect** Zookeeper server
:   **deserialization.schema** Deserialization schema to use for decoding kafka events(Default)
:   **version** Kafka version of the bootstrap servers.

As mentioned below:
```
	<input name="radio-stream">
        <attribute name="uri" value="kafka://radio_session_topic"/>
        <attribute name ="bootstrap.servers" value="10.0.2.15:9092"/>
		<attribute name ="zookeeper.connect" value="10.0.2.15:2181"/>
        <attribute name="group.id" value="radio_sesssion"/>
		<attribute name="deserialization.schema" value="com.ericsson.component.aia.services.bps.flink.kafka.decoder.FlinkKafkaGenericRecordDeserializationSchema"/>
		<attribute name="partition.assignment.strategy" value="org.apache.kafka.clients.consumer.RangeAssignor"/>
		<attribute name="version" value="9"/>
    </input>
```

Flink Streaming Attributes Usage:
:   **ups-flink-streaming** represents Flink streaming
:   **driver-class** represents Flink driver class
:   **schemaRegistry.address** Schema registry address to get schema for decoding kafka events
:   **schemaRegistry.cacheMaximumSize** Schema registry client cache size

As mentioned below:
```
	<step name="ups-flink-streaming">
	    <attribute name="uri" value="flink-streaming://SampleFlinkStreamingHandler"/>
		<attribute name="driver-class" value="x.y.z.SampleFlinkStreamingHandler" />
		<attribute name="schemaRegistry.address" value="http://10.0.2.15:8081/" />
		<attribute name="schemaRegistry.cacheMaximumSize" value= "5000000" />
	</step> 
```

Local Output Attributes Usage:
:   **local-store** represents Local file system data sink
:   **uri** represents file URL to store processed data
:   **data.format** Format of the processed data (Currently only TXT is supported)

As mentioned below:

```
	<output name="local-store">
        <attribute name="uri" value="file:////tmp/flink"/>
		<attribute name="data.format" value="txt"/>
    </output>
```

Kafka Output Attributes Usage:
:   **output-stream** output stream context name.
:   **kafka://radio_session_topic_out?format=avro** represents Kafka Data sink and 'radio_sesssion_topic represents' Kafka Topic name where events should be published and 'format' of the events published.
:   **bootstrap.servers** Kafka bootstrap servers(comma seperated value).
:   **eventType** full FDN of the schema name in Schema Registry representing schema of the event to be published.
:   **serialization.schema** Serialization schema to use for encoding kafka events(Default).
:   **version** Kafka version of the bootstrap servers.

	<output name="output-stream">
        <attribute name="uri" value="kafka://radio_session_topic_out?format=avro"/>
        <attribute name ="bootstrap.servers" value="10.0.2.15:9092"/>
        <attribute name="eventType" value="pojo.POJO"/>
		<attribute name="serialization.schema" value="com.ericsson.component.aia.services.bps.flink.kafka.encoder.FlinkKafkaGenericRecordSerializationSchema"/>
		<attribute name="version" value="9"/>
    </output>


### This section(<Path>) signifies flow:

#### Flow plan as below:
1. Kafka producers will be publishing messages to Kafka server
2. Flink streaming application will be consuming from Kafka topic
3. Flink Streaming application writes outputs to Local file system and kafka topic

```
<path>
	<from uri="radio-stream" />
	<to uri="ups-flink-streaming" />
	<to uri="local-store" />
	<to uri="output-stream" />
</path>
```
### Build

1. mvn clean  install

### Run locally

1. execute "java -jar target/aia-flink-streaming-deployable.jar <flow.xml path>" to start the application locally


###Run on standalone flink cluster

1. execute "/opt/flink/bin/flink run -m <jobmanager-ip:port> <app target dir>/aia-flink-streaming-deployable.jar  <flow.xml path>" to deploy application to job manager

###Run on flink cluster

1. Build docker using "docker build -t <docker-repo>/<app-name>/<app-version> ."
	for example : "docker build -t armdocker.rnd.ericsson.se/aia/app/aia-flink-streaming-test/0.0.1 ."
2. Run docker using " docker run --env mainClass=<main-class of flink app>  --env masterUrl=<jobmanager-ip:port> --env flowPath=hdfs://<Namenode-ip:port>/<path on hdfs> --env jobArguments="" <app-docker-url>"
	for example : "docker run --env mainClass=com.ericsson.component.aia.services.bps.engine.service.BPSPipeLineExecuter --env masterUrl=0.0.0.0:6123 --env flowPath=hdfs://10.0.2.15:8020/data/flow-kafka-flink.xml --env jobArguments="" armdocker.rnd.ericsson.se/aia/app/aia-flink-streaming-test/0.0.1"
 