package com.mb.dna.data.db.entities;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;


@Entity
@Table(name = "userprivilege_nsql")
@MappedSuperclass
@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
public class DataikuNsql implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", updatable = false, nullable = false)
	private String id;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	private DataikuProjectDetails data;
	
	public DataikuNsql() {
		super();
	}

	public DataikuNsql(String id, DataikuProjectDetails data) {
		super();
		this.id = id;
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DataikuProjectDetails getData() {
		return data;
	}

	public void setData(DataikuProjectDetails data) {
		this.data = data;
	}

	@PrePersist
	public void populateId() {
		if (Objects.isNull(this.getId()))
			this.setId(UUID.randomUUID().toString());
	}
}
