package com.daimler.data.db.entities;

import com.daimler.data.db.json.Matomo;
import com.daimler.data.db.json.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "matomo_nsql")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatomoNsql  extends BaseEntity<Matomo> implements Serializable {
    private static final long serialVersionUID = 4857908075537600169L;

    public MatomoNsql() {
        super();
    }

    public MatomoNsql(String id, Matomo data) {
        this.setId(id);
        this.setData(data);
    }

}
