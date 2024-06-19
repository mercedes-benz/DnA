package com.daimler.data.dto.userinfo.dashboard.dataProduct;

import java.io.Serializable;

import com.daimler.data.db.jsonb.dataproduct.TeamMember;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataProductTeamLov implements Serializable {
    
    private String id;
    private TeamMember member;
    
}