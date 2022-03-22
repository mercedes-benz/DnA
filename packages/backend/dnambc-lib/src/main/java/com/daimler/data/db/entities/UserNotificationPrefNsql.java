package com.daimler.data.db.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.daimler.data.db.jsonb.Algorithm;

@Entity
@Table(name = "usernotificationpref_nsql")
public class UserNotificationPrefNsql  extends BaseEntity<Algorithm> implements Serializable {

	private static final long serialVersionUID = -3671274916160814194L;
	
	public UserNotificationPrefNsql() {
		super();
	}

}
