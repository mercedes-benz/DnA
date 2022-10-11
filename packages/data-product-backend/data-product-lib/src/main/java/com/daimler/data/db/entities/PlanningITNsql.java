package com.daimler.data.db.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.daimler.data.db.jsonb.PlanningIT;

@Entity
@Table(name = "planningit_nsql")
public class PlanningITNsql extends BaseEntity<PlanningIT> implements Serializable {

	private static final long serialVersionUID = 1L;

	public PlanningITNsql() {
		super();
	}

	public PlanningITNsql(String id, PlanningIT data) {
		
		this.setId(id);
		this.setData(data);
	}

}
