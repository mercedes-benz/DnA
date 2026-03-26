package com.daimler.data.service.azure;

import com.daimler.data.dto.azure.AzureTokenRequestDto;
import com.daimler.data.dto.azure.AzureTokenResponseDto;
import com.daimler.data.dto.databricks.ClustersListResponseDto;
import com.daimler.data.dto.databricks.CreateCatalogRequestDto;
import com.daimler.data.dto.databricks.CreateCatalogResponseDto;

public interface AzureTokenService {

	AzureTokenResponseDto getAccessToken(AzureTokenRequestDto tokenRequest);

	ClustersListResponseDto listClusters(AzureTokenRequestDto tokenRequest, String databricksHost);

	CreateCatalogResponseDto createCatalog(AzureTokenRequestDto tokenRequest, CreateCatalogRequestDto catalogRequest);
}
