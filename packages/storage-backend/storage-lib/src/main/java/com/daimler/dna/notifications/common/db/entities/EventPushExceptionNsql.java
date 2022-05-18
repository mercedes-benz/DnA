package com.daimler.dna.notifications.common.db.entities;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "eventpushexception_nsql")
public class EventPushExceptionNsql extends BaseEntity<EventPushException> implements Serializable {

	private static final long serialVersionUID = 7390290082819270171L;

	public EventPushExceptionNsql() {
		super();
	}

}
