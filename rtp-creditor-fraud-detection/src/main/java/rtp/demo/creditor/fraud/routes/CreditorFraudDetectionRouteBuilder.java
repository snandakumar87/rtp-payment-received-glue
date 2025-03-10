package rtp.demo.creditor.fraud.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import rtp.demo.creditor.domain.payments.serde.PaymentSerializer;
import rtp.demo.creditor.domain.payments.serde.PaymentDeserializer;

@Component
public class CreditorFraudDetectionRouteBuilder extends RouteBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(CreditorFraudDetectionRouteBuilder.class);

	private String kafkaBootstrap = System.getenv("BOOTSTRAP_SERVERS");

	private String kafkaCreditorPaymentsTopic = System.getenv("CREDITOR_PAYMENTS_TOPIC");
	private String kafkaCreditorProcessedPaymentsTopic = System.getenv("CREDITOR_PROCESSED_PAYMENTS_TOPIC");
	private String consumerMaxPollRecords = System.getenv("CONSUMER_MAX_POLL_RECORDS");
	private String consumerCount = System.getenv("CONSUMER_COUNT");
	private String consumerSeekTo = System.getenv("CONSUMER_SEEK_TO");
	private String consumerGroup = System.getenv("CONSUMER_GROUP");

	@Override
	public void configure() throws Exception {
		LOG.info("Configuring Creditor Fraud Detection RTP Routes");

		KafkaComponent kafka = new KafkaComponent();
		kafka.setBrokers(kafkaBootstrap);

		this.getContext().addComponent("kafka", kafka);

		from("kafka:" + kafkaCreditorPaymentsTopic + "?brokers=" + kafkaBootstrap + "&maxPollRecords="
				+ consumerMaxPollRecords + "&consumersCount=" + consumerCount + "&seekTo=" + consumerSeekTo
				+ "&groupId=" + consumerGroup
				+ "&valueDeserializer=" + PaymentDeserializer.class.getName())
						.log("\\n/// Creditor Fraud Detection - Sending Payment Message >>> ${body}")
						.to("kafka:" + kafkaCreditorProcessedPaymentsTopic
								+ "?serializerClass=" + PaymentSerializer.class.getName());

	}

}
