package com.daimler.dna.notifications.common.event.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;

import com.daimler.dna.notifications.common.producer.KafkaDynamicProducerService;
import com.daimler.dna.notifications.dto.NotificationVO;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisCacheUtil {

	@Autowired
	private RedisCacheManager cacheManager;
	
	@Autowired
	private RedisCacheConfig redisCacheConfig;
	
	@Value(value = "${kafka.centralReadTopic.name}")
	private String readTopicName;

	@Value(value = "${kafka.centralDeleteTopic.name}")
	private String deleteTopicName;

	@Autowired
	private KafkaDynamicProducerService dynamicProducer;
	
	
	public Cache getCache(String cacheName) {
		return this.cacheManager.getCache(cacheName);
	}

	public void addEntry(String cacheName, NotificationVO record) {
		this.getCache(cacheName).put(record.getId(), record);
		log.info("Added entry to cache with name {} and key {}",cacheName,record.getId());
	}

	public void deleteEntry(String cacheName, String id) {
		this.getCache(cacheName).evict(id);
		log.info("Removed entry from cache with name {} and key {}",cacheName,id);
	}

	public List<NotificationVO> getNotificationVO(String userId, String eventType, String readType, String searchTerm,
			Integer offset, Integer limit) {
		List<NotificationVO> voList = new ArrayList<>();
		List<String> keys = redisCacheConfig.getKeys(userId);
		if(keys!=null && !keys.isEmpty()) {
			log.info("Successfully got lettuceConnection and readKeys");
			for(String key: keys) {
				if(key!=null) {
					String[] keySplitArray = key.split(userId+"::");
					if(keySplitArray!=null && keySplitArray.length>1 && keySplitArray[1]!=null) {
						ValueWrapper value = this.getCache(userId).get(keySplitArray[1]);
						NotificationVO record = (NotificationVO) value.get();
						if("READ".equalsIgnoreCase(readType)) {
							if(record!=null && "true".equalsIgnoreCase(record.getIsRead()))
								voList.add(record);
						} else if("UNREAD".equalsIgnoreCase(readType)) {
							if(record!=null && "false".equalsIgnoreCase(record.getIsRead()))
								voList.add(record);
						} else {
							voList.add(record);
						}
					}
				}
			}
		}
		return voList;
	}

	@SuppressWarnings("deprecation")
	public void markMessages(String type, String cacheName, List<String> messageIds) {
		String topicName = cacheName;
		String isRead = "";
		boolean deleteRecord = false;
		if ("READ".equalsIgnoreCase(type)) {
			topicName = readTopicName;
			isRead = "true";
		}
		if ("DELETE".equalsIgnoreCase(type)) {
			topicName = deleteTopicName;
			deleteRecord = true;
		}
		for (String id : messageIds) {
			Cache cache = this.getCache(cacheName);
			NotificationVO vo = new NotificationVO();
			ValueWrapper value = this.getCache(cacheName).get(id);
			vo = (NotificationVO) value.get();
			vo.setIsRead(isRead);
			if (deleteRecord) {
				cache.evict(vo.getId());
				log.info("Cache evict successful for cache {} and key {} ",cacheName,id);
			} else {
				cache.put(vo.getId(), vo);
				log.info("Cache put successful for cache {} and key {}, marked as read.",cacheName,id);
			}
			GenericEventRecord markedMessage = new GenericEventRecord();
			markedMessage.setUuid(vo.getId());
			markedMessage.setMessage(vo.getMessage());
			markedMessage.setResourceId(vo.getResourceId());
			markedMessage.setMessageDetails(vo.getMessageDetails());
			markedMessage.setEventType(vo.getEventType());
			markedMessage.setChangeLogs(vo.getChangeLogs());
			markedMessage.setTime(vo.getDateTime());
			markedMessage.setPublishingUser(cacheName);
			markedMessage.setPublishingAppName("DNA");
			dynamicProducer.sendMessage(topicName, markedMessage);
			log.info("Published record {} to topic {} ", vo.getId(), topicName);
		}

	}
}
