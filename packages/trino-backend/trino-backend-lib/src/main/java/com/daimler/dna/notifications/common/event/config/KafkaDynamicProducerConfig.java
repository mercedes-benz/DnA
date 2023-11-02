package com.daimler.dna.notifications.common.event.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KafkaDynamicProducerConfig {

	@Value(value = "${spring.cloud.stream.kafka.binder.brokers}")
	private String bootstrapAddress;

	@Bean
	public ProducerFactory<String, GenericEventRecord> producerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GenericEventRecordSerializer.class);
		log.info("New ProducerFactory created with bootstrap_server{} and String Key serializer "
				+ "and GenericEventRecordSerializer for value", bootstrapAddress);
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<String, GenericEventRecord> kafkaTemplate() {
		log.info("New KafkaTemplate created for above factory ");
		return new KafkaTemplate<>(producerFactory());
	}

}
