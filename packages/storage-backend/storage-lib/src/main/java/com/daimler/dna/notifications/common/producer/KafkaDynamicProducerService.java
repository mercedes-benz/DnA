package com.daimler.dna.notifications.common.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.daimler.dna.notifications.common.db.repo.EventPushExceptionRepo;
import com.daimler.dna.notifications.common.event.config.GenericEventRecord;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaDynamicProducerService {

	@Autowired
	private KafkaTemplate<String, GenericEventRecord> kafkaTemplate;

	@Autowired
	private EventPushExceptionRepo eventRepo;

	@Transactional
	public void sendMessage(String topicName, GenericEventRecord record) {
		String publishingAppName = record != null ? record.getPublishingAppName() : null;
		String eventType = record != null ? record.getEventType() : null;
		String publishingUser = record != null ? record.getPublishingUser() : null;
		ListenableFuture<SendResult<String, GenericEventRecord>> future = kafkaTemplate.send(topicName, record);
		future.addCallback(new ListenableFutureCallback<SendResult<String, GenericEventRecord>>() {
			@Override
			public void onSuccess(SendResult<String, GenericEventRecord> result) {
//	             result.getRecordMetadata().offset()
				log.debug("Message send successfully to topic {} with eventDetails {} {} {}", topicName,
						publishingAppName, eventType, publishingUser);
			}

			@Override
			public void onFailure(Throwable ex) {
				eventRepo.logPushException(topicName, new GenericEventRecord(), ex.getMessage());
				log.error("Exception {} occured while sendingMessage to topic {} with eventDetails " + "{} {} {} ",
						ex.getMessage(), topicName, publishingAppName, eventType, publishingUser);
			}
		});
	}

}
