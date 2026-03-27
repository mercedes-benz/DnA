package com.daimler.data.service.fabric;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.fabric.DdxOnboardingRequestDto;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;

public interface DdxOnboardingService {

    GenericMessage onboardToDdx(DdxOnboardingRequestDto publishDdxRequest, String workspaceId, String workspaceName, String lakehouseId, String userId, CreatedByVO createdBy);
    
}



