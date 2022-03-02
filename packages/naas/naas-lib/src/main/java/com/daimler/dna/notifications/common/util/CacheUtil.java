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

package com.daimler.dna.notifications.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.dna.notifications.common.event.config.GenericEventRecord;
import com.daimler.dna.notifications.common.producer.KafkaDynamicProducerService;
import com.daimler.dna.notifications.dto.NotificationVO;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Searchable;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.search.attribute.DynamicAttributesExtractor;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

@Component
public class CacheUtil {

	@Value(value = "${kafka.centralReadTopic.name}")
	private String readTopicName;

	@Value(value = "${kafka.centralDeleteTopic.name}")
	private String deleteTopicName;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private KafkaDynamicProducerService dynamicProducer;

	public void createCache(String cacheName) {
		final String attrNames[] = { "isRead" };
		Searchable search = new Searchable();
		search.allowDynamicIndexing(true);
		CacheConfiguration cacheConfiguration = new CacheConfiguration(cacheName, 2000);
		cacheConfiguration.addSearchable(search);
		Ehcache testCache = new Cache(
				cacheConfiguration.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU).eternal(true));

		testCache.registerDynamicAttributesExtractor(new DynamicAttributesExtractor() {
			public Map<String, Object> attributesFor(Element element) {
				Map<String, Object> attrs = new HashMap<String, Object>();
				NotificationVO value = (NotificationVO) element.getObjectValue();
				// For example, extract first name only
				attrs.put(attrNames[0], value.getIsRead());
				return attrs;
			}
		});
		cacheManager.addCache(testCache);
	}

	public Ehcache getCache(String cacheName) {
		return this.cacheManager.getEhcache(cacheName);
	}

	public void addEntry(String cacheName, NotificationVO record) {
		this.getCache(cacheName).put(new Element(record.getId(), record));
	}

	public void deleteEntry(String cacheName, String id) {
		this.getCache(cacheName).remove(id);
	}

	public List<NotificationVO> getNotificationVO(String userId, String eventType, String readType, String searchTerm,
			Integer offset, Integer limit) {
		Attribute<String> isRead = this.getCache(userId).getSearchAttribute("isRead");
		Query query = null;
		if ("READ".equalsIgnoreCase(readType)) {
			query = this.getCache(userId).createQuery().addCriteria(isRead.eq("true")).includeKeys().includeValues()
					.end();
		} else if ("UNREAD".equalsIgnoreCase(readType)) {
			query = this.getCache(userId).createQuery().addCriteria(isRead.eq("false")).includeKeys().includeValues()
					.end();
		} else {
			List<String> values = new ArrayList<String>();
			values.add("true");
			values.add("false");
			query = this.getCache(userId).createQuery().addCriteria(isRead.in(values)).includeKeys().includeValues()
					.end();
		}

		Results results = query.execute();
		List<NotificationVO> list = results.all().stream().map(n -> {
			NotificationVO l = new NotificationVO();
			BeanUtils.copyProperties(n.getValue(), l);
			return l;
		}).collect(Collectors.toList());

		/*
		 * DateTimeFormatter formatter =
		 * DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		 * Comparator<NotificationVO> compareByDateTime = (NotificationVO r1,
		 * NotificationVO r2) -> LocalDateTime.parse(r2.getDateTime(),
		 * formatter).compareTo(LocalDateTime.parse(r1.getDateTime(), formatter));
		 * Collections.sort(list, compareByDateTime); //applying limit and offset
		 * if(limit==0 || (offset + limit)>=list.size()) limit = list.size(); else limit
		 * = offset + limit; if(results!=null && !list.isEmpty()) list =
		 * list.subList(offset, limit);
		 */

		return list;
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
			Ehcache cache = this.getCache(cacheName);
			NotificationVO vo = new NotificationVO();
			Element element = this.getCache(cacheName).get(id);
			BeanUtils.copyProperties(element.getObjectValue(), vo);
			vo.setIsRead(isRead);
			if (deleteRecord) {
				cache.remove(vo.getId());
			} else {
				Element element1 = new Element(vo.getId(), vo);
				cache.put(element1);
			}
			GenericEventRecord markedMessage = new GenericEventRecord();
			markedMessage.setUuid(vo.getId());
			markedMessage.setMessage(vo.getMessage());
			markedMessage.setResourceId(vo.getResourceId());
			markedMessage.setMessageDetails(vo.getMessageDetails());
			markedMessage.setEventType(vo.getEventType());
			markedMessage.setTime(vo.getDateTime());
			markedMessage.setPublishingUser(cacheName);
			markedMessage.setPublishingAppName("DNA");
			dynamicProducer.sendMessage(topicName, markedMessage);
		}

	}
}
