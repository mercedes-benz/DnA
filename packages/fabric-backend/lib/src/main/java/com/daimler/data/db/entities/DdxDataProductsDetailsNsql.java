package com.daimler.data.db.entities;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.daimler.data.db.json.DdxDataProductsDetail;

@Entity
@Table(name = "ddx_dataProducts_details_nsql")
public class DdxDataProductsDetailsNsql extends BaseEntity<DdxDataProductsDetail> implements Serializable {

    private static final long serialVersionUID = 1L;

    public DdxDataProductsDetailsNsql() {
        super();
    }

    public DdxDataProductsDetailsNsql(String id, DdxDataProductsDetail data) {
        this.setId(id);
        this.setData(data);
    }
}
