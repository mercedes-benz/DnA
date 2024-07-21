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
public class CreateRoleResponseDto implements Serializable{

	private static final long serialVersionUID = 1L;

    private String roleType;
    private String dataClassification;
    private AccessReviewDto accessReview;
    private int defaultValidityDays;
    private String defaultValidityType;
    private boolean deprovisioning;
    private String description;
    private String id;
    private boolean isDynamic;
    private boolean isGlobalCentralAvailable;
    private boolean isJobTitle;
    private boolean isSelfRequestable;
    private boolean isWorkflowBased;
    private String name;
    private boolean needsAdditionalSelfRequestApproval;
    private boolean needsCustomScopes;
    private boolean needsOrgScopes;
    private boolean notificationsActive;
    private String uuid;
    private WorkflowDefinitionDto workflowDefinition;
    private List<String> communityAvailability;
    
}
