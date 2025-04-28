package com.daimler.data.db.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.daimler.data.db.json.CodeServerUserGroupList;

@Entity
@Table(name = "user_wsgroup_nsql")
public class CodeServerUserGroupNsql extends BaseEntity<CodeServerUserGroupList> implements Serializable {

    private static final long serialVersionUID = 4857908075537600169L;

    public CodeServerUserGroupNsql() {
        super();
    }

    public CodeServerUserGroupNsql(String id, CodeServerUserGroupList data) {
        this.setId(id);
        this.setData(data);
    }



}
