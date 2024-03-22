/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.dna.notifications.common.event.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
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

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;
	
	@Value(value = "${spring.kafka.properties.ssl.keystore.location}")
	private String sslKeyStoreLocation;

	@Value(value = "${spring.kafka.properties.ssl.keystore.password}")
	private String sslKeyStorePassword;

	@Value(value = "${spring.kafka.properties.ssl.truststore.location}")
	private String sslTrustStoreLocation;

	@Value(value = "${spring.kafka.properties.ssl.truststore.password}")
	private String sslTrustStorePassword;

	@Bean
	public ProducerFactory<String, GenericEventRecord> producerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GenericEventRecordSerializer.class);
		configProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,sslKeyStoreLocation);
		configProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeyStorePassword);
		configProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,sslTrustStoreLocation);
		configProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTrustStorePassword);
		configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
		configProps.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG,sslKeyStorePassword);
		configProps.put(SslConfigs.DEFAULT_SSL_KEYSTORE_TYPE, "PKCS12");
		configProps.put(SslConfigs.DEFAULT_SSL_TRUSTSTORE_TYPE,"PKCS12");
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
