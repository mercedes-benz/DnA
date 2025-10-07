package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ADAProjectDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private String projectID;
    private String adaID;
    private String projectName;
    private String leanIX;
    private String costCenter;
    private String plant;
    private String internalOrder;
    private String division;
    private String subdivision;
    private String department;
    private List<Service> services;
    private List<Stakeholder> stakeholders;
    private List<Tag> tags;
    private Date startingDate;
    private boolean active;
    private Date inactiveDate;

    public ADAProjectDetails() {
        this.stakeholders = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.services = new ArrayList<>();
    }

    @Data
    public static class Service implements Serializable {

        private static final long serialVersionUID = 1L;
        private String serviceName;
        private int serviceQuantity;
    }

    @Data
    public static class Stakeholder implements Serializable {

        private static final long serialVersionUID = 1L;
        private String position;
        private String userID;
    }

    @Data
    public static class Tag implements Serializable {

        private static final long serialVersionUID = 1L;
        private String description;
        private String value;
    }
}
