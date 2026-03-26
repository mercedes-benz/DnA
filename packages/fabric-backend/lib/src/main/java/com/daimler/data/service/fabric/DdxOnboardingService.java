package com.daimler.data.service.fabric;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.fabric.DdxOnboardingRequestDto;

public interface DdxOnboardingService {

    GenericMessage onboardToDdx(DdxOnboardingRequestDto publishDdxRequest,String workspaceId, String workspaceName, String lakehouseId, String userId);
    
}



