package com.daimler.data.db.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.daimler.data.db.json.DdxMirroredCatalogProduct;

@Entity
@Table(name = "ddx_mirrored_catalog_product_nsql")
public class DdxMirroredCatalogProductNsql extends BaseEntity<DdxMirroredCatalogProduct> implements Serializable {

    private static final long serialVersionUID = 1L;

    public DdxMirroredCatalogProductNsql() {
        super();
    }

    public DdxMirroredCatalogProductNsql(String id, DdxMirroredCatalogProduct data) {
        this.setId(id);
        this.setData(data);
    }
}
