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

package com.daimler.dna.notifications.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.dna.notifications.common.event.config.GenericEventRecord;
import com.daimler.dna.notifications.common.event.config.RedisCacheUtil;
import com.daimler.dna.notifications.dto.NotificationCollectionVO;
import com.daimler.dna.notifications.dto.NotificationVO;

@Service
public class NotificationsServiceImpl implements NotificationsService {

	private static final Logger LOG = LoggerFactory.getLogger(NotificationsServiceImpl.class);

	@Autowired
	private KafkaCoreCampaignService kafkaCoreService;

	@Autowired
	private RedisCacheUtil cacheUtil;

	/*
	 * @Override public NotificationCollectionVO getAll( String userId, String
	 * eventType, String readType, String searchTerm,Integer offset, Integer limit)
	 * { List<String> eventTypes = kafkaCoreService.getEventCategories(userId);
	 * List<NotificationVO> notifications = kafkaCoreService.getMessages(userId,
	 * eventType, readType, searchTerm, offset, limit); NotificationCollectionVO
	 * collectionVO = new NotificationCollectionVO();
	 * collectionVO.setCategories(eventTypes);
	 * collectionVO.setRecords(notifications); return collectionVO; }
	 */

	@Override
	public NotificationCollectionVO getAll(String userId, String eventType, String readType, String searchTerm,
			Integer offset, Integer limit) {
		NotificationCollectionVO collectionVO = new NotificationCollectionVO();
		List<String> eventTypes = new ArrayList<String>();
		List<NotificationVO> notifications = new ArrayList<NotificationVO>();
		try {
			notifications = cacheUtil.getNotificationVO(userId, eventType, readType, searchTerm, offset, limit);
		} catch (Exception e) {
			LOG.error("unable to fetch message. reason::{} caused by::{}", e.getMessage(), e.getCause());
		}

		if (notifications != null && !notifications.isEmpty()) {
			eventTypes = notifications.stream().map(n -> n.getEventType()).collect(Collectors.toList());
			eventTypes = eventTypes.stream().distinct().collect(Collectors.toList());
		}
		collectionVO.setCategories(eventTypes);
		collectionVO.setRecords(notifications);
		collectionVO.setTotalRecordCount(notifications.size());
		return collectionVO;
	}

	@Override
	public void markMessageAsRead(String userId, List<String> messageUUID) {
		cacheUtil.markMessages("READ", userId, messageUUID);
	}

	@Override
	public void markMessageAsDelete(String userId, List<String> messageUUID) {
		cacheUtil.markMessages("DELETE", userId, messageUUID);
	}

	@Override
	public void publishMessage(GenericEventRecord request) {
		kafkaCoreService.publishMessageTocentralTopic(request);
	}

}
