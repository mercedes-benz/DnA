package com.daimler.data.notifications.common.db.repo;

import java.util.List;

import com.daimler.data.notifications.common.db.entities.EventPushExceptionNsql;
import com.daimler.data.notifications.common.event.config.GenericEventRecord;

public interface EventPushExceptionRepo {

	EventPushExceptionNsql update(EventPushExceptionNsql entity);

	void create(EventPushExceptionNsql entity);

	List<EventPushExceptionNsql> findAll(int limit, int offset);

	void logPushException(String outTopic, GenericEventRecord record, String exceptionMessage);

}
