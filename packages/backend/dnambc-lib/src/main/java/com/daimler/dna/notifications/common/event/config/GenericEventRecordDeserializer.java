package com.daimler.dna.notifications.common.event.config;

import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GenericEventRecordDeserializer implements Deserializer<GenericEventRecord> {

	private static final Logger LOG = LoggerFactory.getLogger(GenericEventRecordDeserializer.class);

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public GenericEventRecord deserialize(String topic, byte[] data) {
		try {
			if (data == null) {
				LOG.debug("Null received at deserializing");
				return null;
			}
			LOG.debug("Deserializing string received from topic to GenericEventRecord");
			return objectMapper.readValue(new String(data, "UTF-8"), GenericEventRecord.class);
		} catch (Exception e) {
			throw new SerializationException("Error when deserializing byte[] to MessageDto");
		}
	}

	@Override
	public void close() {
	}

}
