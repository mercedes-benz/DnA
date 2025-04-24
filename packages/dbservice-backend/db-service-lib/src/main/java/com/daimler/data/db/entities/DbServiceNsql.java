package com.daimler.data.db.entities;

import java.io.Serializable;

import com.daimler.data.db.json.DbService;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Entity
@Table(name = "dbService_nsql")
public class DbServiceNsql  extends BaseEntity<DbService> implements Serializable{
	private static final long serialVersionUID = 1L;

	public DbServiceNsql() {
		super();
	}
}

