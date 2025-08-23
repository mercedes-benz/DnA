package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ADAProjectDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private String projectID;
    @JsonProperty("ADA_id")
    private String adaID;
    private String projectName;
    private String leanIX;
    private String costCenter;
    private String plant;
    private String internalOrder;
    private String division;
    private String subdivision;
    private String department;
    private Service service;
    private List<Stakeholder> stakeholders;
    private List<Tag> tags;
    private Date startingDate;
    private boolean active;
    private Date inactiveDate;

    @Data
    public class Service {
        private String serviceName;
        private int serviceQuantity;
    }

    @Data
    public class Stakeholder {
        private String position;
        private String userID;
    }

    @Data
    public class Tag {
        private String description;
        private String value;
    }
}
