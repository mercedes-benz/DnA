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

package com.daimler.dna.notifications.common.consumer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.dna.notifications.common.event.config.GenericEventRecord;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaDynamicConsumerService {

	@Autowired
	AdminClient adminClient;

	@Value(value = "${spring.cloud.stream.kafka.binder.brokers}")
	private String bootstrapAddress;

	@Value(value = "${kafka.consumer.pollingTime}")
	private String pollingTime;

	public List<GenericEventRecord> consumeRecordsFromTopic(List<String> topicsNameList) {
		List<GenericEventRecord> messages = new ArrayList<>();
		Properties props = new Properties();
		props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "test");
		props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		// props.setProperty("enable.auto.commit", "false");
		// props.setProperty("auto.commit.interval.ms", "1000");
		props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringDeserializer");
		props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				"com.daimler.dna.notifications.common.event.config.GenericEventRecordDeserializer");
		KafkaConsumer<String, GenericEventRecord> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(topicsNameList);
		log.debug("List of topics given for fetch {}", topicsNameList);
		int i = 10;
		while (i >= 0) {
			ConsumerRecords<String, GenericEventRecord> records = consumer
					.poll(Duration.ofMillis(Integer.parseInt(pollingTime)));
			if (Objects.nonNull(records) && !records.isEmpty()) {
				for (ConsumerRecord<String, GenericEventRecord> record : records) {
					log.debug("offset {} key {} value {} fetched from topic are ", record.offset(), record.key(),
							record.value());
					messages.add(record.value());
				}
				break;
			}
			i--;
			log.debug("topic fetching iteration count is {} for given topicsList {} ", i, topicsNameList);
		}

		consumer.close();
		log.debug("topic consumer for list {} is closed", topicsNameList);
		return messages;
	}

	public void deleteSome(String topicName, int partitionIndex, int beforeIndex) {
		TopicPartition topicPartition = new TopicPartition(topicName, partitionIndex);
		Map<TopicPartition, RecordsToDelete> deleteMap = new HashMap<>();
		deleteMap.put(topicPartition, RecordsToDelete.beforeOffset(beforeIndex));
		log.debug("deleting records from topic {} with partitionIndex {} and beforeIndex {}", topicName, partitionIndex,
				beforeIndex);
		adminClient.deleteRecords(deleteMap);
	}

}
