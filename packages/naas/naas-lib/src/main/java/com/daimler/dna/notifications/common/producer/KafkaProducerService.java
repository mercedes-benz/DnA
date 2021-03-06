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

package com.daimler.dna.notifications.common.producer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.dna.notifications.common.event.config.GenericEventRecord;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaProducerService {

	@Value(value = "${kafka.centralTopic.name}")
	private String topicName;

	@Autowired
	private KafkaDynamicProducerService dynamicProducer;

	@Transactional
	public void send(String eventType, String resourceId,String messageDetails, String publishingUser, String message, 
			Boolean mail_required, List<String> subscribedUsers, List<String> subscribedUsersEmail, List<ChangeLogVO> changeLogs) {
		GenericEventRecord record = this.defaultRecordBuilder(eventType, resourceId, messageDetails, publishingUser,
				message, mail_required, subscribedUsers, subscribedUsersEmail, changeLogs);
		dynamicProducer.sendMessage(topicName, record);
	}

	private GenericEventRecord defaultRecordBuilder(String eventType, String resourceId, String messageDetails,
			String publishingUser, String message, Boolean mail_required, List<String> subscribedUsers, List<String> subscribedUsersEmail, List<ChangeLogVO> changeLogs) {
		GenericEventRecord eventRecord = new GenericEventRecord();
		eventRecord.setUuid(UUID.randomUUID().toString());
		eventRecord.setPublishingAppName("DNA");
		eventRecord.setPublishingUser(publishingUser);
		eventRecord.setEventType(eventType);
		eventRecord.setMessage(message);
		eventRecord.setResourceId(resourceId);
		eventRecord.setMessageDetails(messageDetails);
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		eventRecord.setTime(dateFormatter.format(new Date()));
		eventRecord.setSubscribedUsers(subscribedUsers);
		eventRecord.setSubscribedUsersEmail(subscribedUsersEmail);
		eventRecord.setChangeLogs(changeLogs);
		log.debug("New event record created with detail eventtype : {} " + "publishingUser: {} and subscribers: {}",
				eventType, publishingUser, subscribedUsers);
		return eventRecord;
	}

}
