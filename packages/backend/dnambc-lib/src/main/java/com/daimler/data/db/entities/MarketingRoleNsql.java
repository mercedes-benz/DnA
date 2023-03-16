package com.daimler.data.db.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.daimler.data.db.jsonb.MarketingRole;

@Entity
@Table(name = "marketingrole_nsql")
public class MarketingRoleNsql extends BaseEntity<MarketingRole> implements Serializable {

	private static final long serialVersionUID = -4499530360891328972L;

	public MarketingRoleNsql() {
		super();
	}
	
}
