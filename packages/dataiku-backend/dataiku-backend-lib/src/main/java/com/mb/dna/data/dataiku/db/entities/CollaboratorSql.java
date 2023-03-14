package com.mb.dna.data.dataiku.db.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "collaborator_sql")
public class CollaboratorSql implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", updatable = false, nullable = false)
	private String id;
	
    @Column(name = "userid", nullable = false)
	private String userId;
	
    @Column(name = "dataiku_id", insertable = false ,updatable = false)
	private String dataikuId;
	
	@Column(name = "givenname")
	private String givenName;
	
	@Column(name = "surname")
	private String surName;
	
	@Column(name = "permission")
	private String permission;
	
	@ManyToOne(fetch= FetchType.LAZY)
	@PrimaryKeyJoinColumn(name="dataiku_id",referencedColumnName="id")
	private DataikuSql dataiku;

	public CollaboratorSql() {
		super();
	}
	
	public CollaboratorSql(String id, String userId, String dataikuId, String givenName, String surName,
				String permission) {
			super();
			this.id = id;
			this.userId = userId;
			this.dataikuId = dataikuId;
			this.givenName = givenName;
			this.surName = surName;
			this.permission = permission;
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
		return dataiku;
	}

	public void setDataikuProject(DataikuSql dataiku) {
		this.dataiku = dataiku;
	}
	
}
