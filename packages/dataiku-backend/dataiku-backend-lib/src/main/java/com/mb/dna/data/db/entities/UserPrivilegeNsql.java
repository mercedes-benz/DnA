package com.mb.dna.data.db.entities;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "userprivilege_nsql")
public class UserPrivilegeNsql implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", updatable = false, nullable = false)
	private String id;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	private UserPrivilege data;
	
	
	public UserPrivilegeNsql() {
		super();
	}
	
	public UserPrivilegeNsql(String id, UserPrivilege data) {
		super();
		this.id = id;
		this.data = data;
	}

	public UserPrivilegeNsql(UserPrivilege userinfo) {
		super();
		this.data = userinfo;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public UserPrivilege getData() {
		return data;
	}

	
	public void setData(UserPrivilege data) {
		this.data = data;
	}

	@PrePersist
	public void populateId() {
		if (Objects.isNull(this.getId()))
			this.setId(UUID.randomUUID().toString());
	}
	
}
