package com.daimler.data.dto.userinfo.dataTransfer;

import java.io.Serializable;

import com.daimler.data.db.jsonb.datatransfer.TeamMember;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataTransferTeamMemLov implements Serializable {
    
    private String id;
    private TeamMember member;
    
}
