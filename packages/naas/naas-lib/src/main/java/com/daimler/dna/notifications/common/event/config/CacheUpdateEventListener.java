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

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.daimler.data.dto.usernotificationpref.UserNotificationPrefVO;
import com.daimler.dna.notifications.common.dna.client.DnaNotificationPreferenceClient;
import com.daimler.dna.notifications.dto.NotificationVO;
import com.mbc.dna.notifications.mailer.JMailer;

@Component
public class CacheUpdateEventListener {

	private static Logger LOG = LoggerFactory.getLogger(CacheUpdateEventListener.class);

	@Autowired
	AdminClient adminClient;

	@Value(value = "${spring.cloud.stream.kafka.binder.brokers}")
	private String bootstrapAddress;

	@Value(value = "${kafka.consumer.pollingTime}")
	private String pollingTime;

	@Value(value = "${kafka.consumer.maxPollRecors}")
	private String maxPollRecors;

	@Value(value = "${kafka.centralTopic.name}")
	private String centralTopic;

	@Value(value = "${kafka.centralReadTopic.name}")
	private String readTopicName;

	@Value(value = "${kafka.centralDeleteTopic.name}")
	private String deleteTopicName;

	@Autowired
	private RedisCacheUtil cacheUtil;
	
	@Autowired
	private DnaNotificationPreferenceClient userNotificationPreferencesClient;
	
	@Autowired
	private JMailer mailer;
	
	private static String SOLUTION_NOTIFICATION_KEY = "Solution";
	private static String NOTEBOOK_NOTIFICATION_KEY = "Notebook";
	private static String STORAGE_NOTIFICATION_KEY = "Storage";
	private static String DASHBOARD_NOTIFICATION_KEY = "Dashboard";
	private static String DATAPRODUCT_NOTIFICATION_KEY = "DataProduct";
	private static String DATACOMPLIANCE_NOTIFICATION_KEY = "DataCompliance";
	private static String CHRONOS_NOTIFICATION_KEY = "Chronos";
	private static String CODESPACE_NOTIFICATION_KEY = "Codespace";
	
	//@PostConstruct
	public void init() {

		LOG.info("started updating cache from " + centralTopic);
		populateDataOnCache(centralTopic, false, false);
		LOG.info("Successfully updated data on cache from " + centralTopic);

		LOG.info("started updating cache from " + readTopicName);
		populateDataOnCache(readTopicName, true, false);
		LOG.info("Successfully updated data on cache from " + readTopicName);

		LOG.info("started updating cache from " + deleteTopicName);
		populateDataOnCache(deleteTopicName, false, true);
		LOG.info("Successfully updated data on cache from " + deleteTopicName);

		LOG.info("Cache Update Successfull...");

	}

	private void populateDataOnCache(String topicName, boolean readTopic, boolean deleteTopic) {
		Properties props = new Properties();
		props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "test");
		// props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		// props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		props.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecors);
		props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringDeserializer");
		props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				"com.daimler.dna.notifications.common.event.config.GenericEventRecordDeserializer");
		KafkaConsumer<String, GenericEventRecord> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList(topicName));
		LOG.info("partitions :: {}", consumer.partitionsFor(topicName));
		while (true) {
			ConsumerRecords<String, GenericEventRecord> records = consumer
					.poll(Duration.ofMillis(Integer.parseInt(pollingTime)));
			if (!consumer.assignment().isEmpty()) {
				consumer.seekToBeginning(consumer.assignment());
				break;
			}
		}
		int i = 10;
		while (i >= 0) {
			ConsumerRecords<String, GenericEventRecord> records = consumer
					.poll(Duration.ofMillis(Integer.parseInt(pollingTime)));
			LOG.info("Total record ::{}", records.count());
			if (Objects.nonNull(records) && !records.isEmpty()) {
				for (ConsumerRecord<String, GenericEventRecord> record : records) {
					if (record.value() != null && !readTopic && !deleteTopic) {
						List<String> users = record.value().getSubscribedUsers();
						List<String> usersEmails = record.value().getSubscribedUsersEmail();
						int userListPivot = 0;
						if(users!= null) {
						for (String user : users) {
							if (StringUtils.hasText(user) && user != "null") {
								if (cacheUtil.getCache(user) == null) {
									LOG.info("Creating cache for user {}", user);
									cacheUtil.getCache(user);
								}
								UserNotificationPrefVO preferenceVO = userNotificationPreferencesClient.getUserNotificationPreferences(user);
								boolean appNotificationPreferenceFlag = true;
								boolean emailNotificationPreferenceFlag = false;
								if(record.value().getEventType().contains(SOLUTION_NOTIFICATION_KEY)) {
									appNotificationPreferenceFlag = preferenceVO.getSolutionNotificationPref().isEnableAppNotifications();
									emailNotificationPreferenceFlag =  preferenceVO.getSolutionNotificationPref().isEnableEmailNotifications();
								}
								if(record.value().getEventType().contains(NOTEBOOK_NOTIFICATION_KEY)) {
									appNotificationPreferenceFlag = preferenceVO.getNotebookNotificationPref().isEnableAppNotifications();
									emailNotificationPreferenceFlag =  preferenceVO.getNotebookNotificationPref().isEnableEmailNotifications();
								}
								if(record.value().getEventType().contains(STORAGE_NOTIFICATION_KEY)) {
									appNotificationPreferenceFlag = preferenceVO.getPersistenceNotificationPref().isEnableAppNotifications();
									emailNotificationPreferenceFlag =  preferenceVO.getPersistenceNotificationPref().isEnableEmailNotifications();
								}
								if(record.value().getEventType().contains(DASHBOARD_NOTIFICATION_KEY)) {
									appNotificationPreferenceFlag = preferenceVO.getDashboardNotificationPref().isEnableAppNotifications();
									emailNotificationPreferenceFlag =  preferenceVO.getDashboardNotificationPref().isEnableEmailNotifications();
								}
								if(record.value().getEventType().contains(DATAPRODUCT_NOTIFICATION_KEY)) {
									appNotificationPreferenceFlag = preferenceVO.getDataProductNotificationPref().isEnableAppNotifications();
									emailNotificationPreferenceFlag =  preferenceVO.getDataProductNotificationPref().isEnableEmailNotifications();
								}
								if(record.value().getEventType().contains(DATACOMPLIANCE_NOTIFICATION_KEY)) {
									appNotificationPreferenceFlag = preferenceVO.getDataComplianceNotificationPref().isEnableAppNotifications();
									emailNotificationPreferenceFlag =  preferenceVO.getDataComplianceNotificationPref().isEnableEmailNotifications();
								}
								if(record.value().getEventType().contains(CHRONOS_NOTIFICATION_KEY)) {
									appNotificationPreferenceFlag = preferenceVO.getChronosNotificationPref().isEnableAppNotifications();
									emailNotificationPreferenceFlag = preferenceVO.getChronosNotificationPref().isEnableEmailNotifications();
								}
								if(record.value().getEventType().contains(CODESPACE_NOTIFICATION_KEY)) {
									appNotificationPreferenceFlag = preferenceVO.getChronosNotificationPref().isEnableAppNotifications();
									emailNotificationPreferenceFlag =  preferenceVO.getChronosNotificationPref().isEnableEmailNotifications();
								}
								NotificationVO vo = new NotificationVO();
								vo.setDateTime(record.value().getTime());
								vo.setEventType(record.value().getEventType());
								vo.setResourceId(record.value().getResourceId());
								vo.setMessageDetails(record.value().getMessageDetails());
								vo.setId(record.value().getUuid());
								vo.setIsRead("false");
								vo.setMessage(record.value().getMessage());
								vo.setChangeLogs(record.value().getChangeLogs());
								GenericEventRecord message = record.value();
								if(appNotificationPreferenceFlag) {
									cacheUtil.addEntry(user, vo);
									LOG.info("New message with details- user {}, eventType {}, uuid {} added to user notifications", user,
											message.getEventType(), message.getUuid());
								}else {
									LOG.info("Skipped message as per user preference, Details: user {}, eventType {}, uuid {} ", user,
											message.getEventType(), message.getUuid());
								}
//								if(emailNotificationPreferenceFlag) {
//									String userEmail = usersEmails!= null && !usersEmails.isEmpty() ? usersEmails.get(userListPivot) : null;
//									if(userEmail!= null && !"".equalsIgnoreCase(userEmail)) {
//										String emailSubject = message.getEventType()+" Email Notification";
//										mailer.sendSimpleMail(message.getUuid(),userEmail, emailSubject , message.getMessage());
//										LOG.info("Sent email as per user preference, Details: user {}, eventType {}, uuid {}", user,
//												message.getEventType(), message.getUuid());
//									}else {
//										LOG.info("Skipped sending email even after user preference is enabled. Cause is email id not found for the user. Details: user {}, eventType {}, uuid {}", user,
//												message.getEventType(), message.getUuid());
//									}
//								}else {
//									LOG.info("Skipped email as per user preference, Details: user {}, eventType {}, uuid {}", user,
//											message.getEventType(), message.getUuid());
//								}
							}
						}
						}
					} else if (record.value() != null && readTopic && !deleteTopic) {
						String user = record.value().getPublishingUser();
						if (cacheUtil.getCache(user) == null) {
							LOG.info("Creating cache for user {}", user);
							cacheUtil.getCache(user);
						}
						NotificationVO vo = new NotificationVO();
						vo.setDateTime(record.value().getTime());
						vo.setEventType(record.value().getEventType());
						vo.setId(record.value().getUuid());
						vo.setResourceId(record.value().getResourceId());
						vo.setMessageDetails(record.value().getMessageDetails());
						vo.setIsRead("true");
						vo.setMessage(record.value().getMessage());
						vo.setChangeLogs(record.value().getChangeLogs());
						cacheUtil.addEntry(user, vo);
					} else if (record.value() != null && !readTopic && deleteTopic) {

						String user = record.value().getPublishingUser();
						if (cacheUtil.getCache(user) == null) {
							LOG.info("Creating cache for user {}", user);
							cacheUtil.getCache(user);
						}
						cacheUtil.deleteEntry(user, record.value().getUuid());
					}
				}
				consumer.commitSync();
			}
			i--;
		}

		consumer.close();
	}
}
