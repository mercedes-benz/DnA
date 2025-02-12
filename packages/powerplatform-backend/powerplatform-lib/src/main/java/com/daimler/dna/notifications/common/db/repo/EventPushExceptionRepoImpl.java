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
