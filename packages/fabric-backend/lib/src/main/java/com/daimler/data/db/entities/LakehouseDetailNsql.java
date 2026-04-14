package com.daimler.data.db.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.daimler.data.db.json.LakehouseDetail;

@Entity
@Table(name = "lakehouse_detail_nsql")
public class LakehouseDetailNsql extends BaseEntity<LakehouseDetail> implements Serializable {

    private static final long serialVersionUID = 1L;

    public LakehouseDetailNsql() {
        super();
    }

    public LakehouseDetailNsql(String id, LakehouseDetail data) {
        this.setId(id);
        this.setData(data);
    }
}
