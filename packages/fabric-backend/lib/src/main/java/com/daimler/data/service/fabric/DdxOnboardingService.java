package com.daimler.data.service.fabric;

import com.daimler.data.dto.fabric.DdxOnboardingRequestDto;
import com.daimler.data.dto.fabric.DdxOnboardingResultDto;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;

public interface DdxOnboardingService {

    DdxOnboardingResultDto onboardToDdx(DdxOnboardingRequestDto publishDdxRequest, String workspaceId, String workspaceName, String lakehouseId, String userId, CreatedByVO createdBy);
    
}



