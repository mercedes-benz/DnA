package com.daimler.data.db.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.daimler.data.db.json.Capacity;

@Entity
@Table(name = "capacity_nsql")
public class CapacityNsql extends BaseEntity<Capacity> implements Serializable {

    private static final long serialVersionUID = 1L;

    public CapacityNsql() {
        super();
    }

    public CapacityNsql(String id, Capacity data) {
        this.setId(id);
        this.setData(data);
    }  
}
