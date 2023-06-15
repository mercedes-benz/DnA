package com.mb.dna.data.userprivilege.db.entities;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "userprivilege_sql")
public class UserPrivilegeSql implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", updatable = false, nullable = false)
	private String id;
	
	@NotNull
    @Column(name = "userId", nullable = false, unique = true)
	private String userId;
	
	@NotNull
    @Column(name = "profile", nullable = false)
	private String profile;
	
    @Column(name = "givenName")
	private String givenName;
    
    @Column(name = "surName")
	private String surName;
	
	public UserPrivilegeSql() {
		super();
	}
	
	public UserPrivilegeSql(String id, @NotNull String userId, @NotNull String profile, String givenName,
			String surName) {
		super();
		this.id = id;
		this.userId = userId;
		this.profile = profile;
		this.givenName = givenName;
		this.surName = surName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getSurName() {
		return surName;
	}

	public void setSurName(String surName) {
		this.surName = surName;
	}

	@PrePersist
	public void populateId() {
		if (Objects.isNull(this.getId()))
			this.setId(UUID.randomUUID().toString());
	}
	
}
