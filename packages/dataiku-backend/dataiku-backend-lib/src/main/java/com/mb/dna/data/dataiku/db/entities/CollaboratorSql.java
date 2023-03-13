package com.mb.dna.data.dataiku.db.entities;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "collaborator_sql")
public class CollaboratorSql implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", updatable = false, nullable = false)
	private String id;
	
	@NotNull
    @Column(name = "userid", nullable = false)
	private String userId;
	
    @Column(name = "dataiku_id")
	private String dataikuId;
	
	@Column(name = "givenname")
	private String givenName;
	
	@Column(name = "surname")
	private String surName;
	
	@Column(name = "permission")
	private String permission;
	
	@ManyToOne(fetch= FetchType.LAZY)
	@PrimaryKeyJoinColumn(name="dataiku_id",referencedColumnName="id")
	private DataikuSql dataikuProject;

	@PrePersist
	public void populateId() {
		if (Objects.isNull(this.getId()))
			this.setId(UUID.randomUUID().toString());
	}
	public CollaboratorSql() {
		super();
	}

	public CollaboratorSql(String id, @NotNull String userId, String dataikuId, String givenName, String surName,
			String permission, DataikuSql dataikuProject) {
		super();
		this.id = id;
		this.userId = userId;
		this.dataikuId = dataikuId;
		this.givenName = givenName;
		this.surName = surName;
		this.permission = permission;
		this.dataikuProject = dataikuProject;
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

	public String getDataikuId() {
		return dataikuId;
	}

	public void setDataikuId(String dataikuId) {
		this.dataikuId = dataikuId;
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

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public DataikuSql getDataikuProject() {
		return dataikuProject;
	}

	public void setDataikuProject(DataikuSql dataikuProject) {
		this.dataikuProject = dataikuProject;
	}
	
	
	
}
