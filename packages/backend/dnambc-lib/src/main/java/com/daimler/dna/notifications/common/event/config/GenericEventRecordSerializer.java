package com.daimler.dna.notifications.common.event.config;

import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("rawtypes")
public class GenericEventRecordSerializer implements Serializer<GenericEventRecord> {

	private static final Logger LOG = LoggerFactory.getLogger(GenericEventRecordSerializer.class);

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public byte[] serialize(String topic, GenericEventRecord record) {
		try {
			if (record == null) {
				LOG.debug("Null received at serializing");
				return null;
			}
			LOG.debug("Serializing GenericEventRecord received from topic");
			return objectMapper.writeValueAsBytes(record);
		} catch (Exception e) {
			throw new SerializationException("Error when serializing MessageDto to byte[]");
		}
	}

	@Override
	public void close() {
	}

}
