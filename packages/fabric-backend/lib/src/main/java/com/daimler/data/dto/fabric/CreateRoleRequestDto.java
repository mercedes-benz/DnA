package com.daimler.data.dto.fabric;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateRoleRequestDto implements Serializable{

	private static final long serialVersionUID = 1L;

    private String roleType;
    private String id;
    private boolean isGlobalCentralAvailable;
    private boolean isWorkflowBased;
    private String name;
    private boolean needsAdditionalSelfRequestApproval;
    private boolean needsOrgScopes;
    private String description;
    private String defaultValidityType;
    private boolean isSelfRequestable;
    private boolean needsCustomScopes;
    private boolean isDynamic;
    private boolean isJobTitle;
    private boolean notificationsActive;
    private String dataClassification;
    private List<String> communityAvailability;
    private WorkflowDefinitionDto workflowDefinition;
    private List<String> marketAvailabilities;
    private List<String> organizationAvailabilities;
    private boolean deprovisioning;
    private AccessReviewDto accessReview;
    
}
