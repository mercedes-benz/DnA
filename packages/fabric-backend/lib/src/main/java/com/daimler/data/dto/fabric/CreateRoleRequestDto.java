package com.daimler.data.dto.fabric;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateRoleRequestDto implements Serializable{

	private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String description;
    @JsonProperty("isJobTitle")
    private boolean isJobTitle;
    private boolean notificationsActive;

    @JsonProperty("isDynamic")
    private boolean isDynamic;
    @JsonProperty("isSelfRequestable")
    private boolean isSelfRequestable;
    private boolean needsAdditionalSelfRequestApproval;
    private boolean needsOrgScopes;
    private boolean needsCustomScopes;
    private String defaultValidityType;
    private boolean deprovisioning;
    @JsonProperty("isGlobalCentralAvailable")
    private boolean isGlobalCentralAvailable;
    @JsonProperty("isWorkflowBased")
    private boolean isWorkflowBased;
    private String roleType;
    private String dataClassification;
    private List<String> communityAvailability;
    private WorkflowDefinitionDto workflowDefinition;
    private List<String> marketAvailabilities;
    private List<String> organizationAvailabilities;
    
    private AccessReviewDto accessReview;
    
}
