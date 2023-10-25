package com.daimler.data.db.entities;


import com.daimler.data.db.jsonb.AnalyticsSolution;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "analyticssolution_nsql")
public class AnalyticsSolutionNsql extends BaseEntity<AnalyticsSolution> implements Serializable {

    private static final long serialVersionUID = -7715637669188213350L;

    public AnalyticsSolutionNsql() {
    }

}
