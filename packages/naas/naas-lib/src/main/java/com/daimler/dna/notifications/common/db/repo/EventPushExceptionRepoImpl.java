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

package com.daimler.dna.notifications.common.db.repo;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.daimler.dna.notifications.common.consumer.KafkaDynamicConsumerService;
import com.daimler.dna.notifications.common.db.entities.EventPushException;
import com.daimler.dna.notifications.common.db.entities.EventPushExceptionNsql;
import com.daimler.dna.notifications.common.event.config.GenericEventRecord;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class EventPushExceptionRepoImpl
//	<EventPushExceptionNsql,String>  
		implements EventPushExceptionRepo
//	<EventPushExceptionNsql,String>
{

	@PersistenceContext
	protected EntityManager em;

	@Override
	public EventPushExceptionNsql update(EventPushExceptionNsql entity) {
		return em.merge(entity);
	}

	@Override
	public void create(EventPushExceptionNsql entity) {
		em.persist(entity);
	}

	@Override
	public List<EventPushExceptionNsql> findAll(int limit, int offset) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventPushExceptionNsql> cq = cb.createQuery(EventPushExceptionNsql.class);
		Root<EventPushExceptionNsql> root = cq.from(EventPushExceptionNsql.class);
		CriteriaQuery<EventPushExceptionNsql> getAll = cq.select(root);
		TypedQuery<EventPushExceptionNsql> getAllQuery = em.createQuery(getAll);
		log.debug("Given offset and limit for fetching eventExceptions is {} and {} ", offset, limit);
		if (offset >= 0)
			getAllQuery.setFirstResult(offset);
		if (limit > 0)
			getAllQuery.setMaxResults(limit);
		return getAllQuery.getResultList();
	}

	@Override
	public void logPushException(String outBinder, GenericEventRecord record, String exceptionMessage) {
		EventPushException eventException = new EventPushException();
		eventException.setRecord(record);
		eventException.setRetryCount(new BigDecimal("0"));
		eventException.setRetrySuccess(false);
		eventException.setExceptionMsg(exceptionMessage);
		eventException.setOutBinder(outBinder);
		EventPushExceptionNsql entity = new EventPushExceptionNsql();
		entity.setData(eventException);
		String publishingAppName = record != null ? record.getPublishingAppName() : null;
		String eventType = record != null ? record.getEventType() : null;
		String publishingUser = record != null ? record.getPublishingUser() : null;
		log.info("New event exception added to table with outBinder {} and record details {} {} {}", outBinder,
				publishingAppName, eventType, publishingUser);
		this.create(entity);
	}

}
