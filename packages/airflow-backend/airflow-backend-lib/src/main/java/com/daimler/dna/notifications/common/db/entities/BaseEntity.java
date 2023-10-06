package com.daimler.dna.notifications.common.db.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
public class BaseEntity<T> {

	@Id
	@Column(name = "id", updatable = false, nullable = false)
	private String id;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	private T data;

	@PrePersist
	public void populateId() {
		if (Objects.isNull(this.getId()))
			this.setId(UUID.randomUUID().toString());
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
