package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fabric2FabricDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean isFabric2Fabric;
    private Date initiatedOn;
    private List<GroupNameDetail> groupsNames;
}
