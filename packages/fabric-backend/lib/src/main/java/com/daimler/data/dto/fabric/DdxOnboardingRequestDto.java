package com.daimler.data.dto.fabric;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.daimler.data.dto.fabric.DdxLegalEntityDto;
import com.daimler.data.dto.fabric.DigitalUsageInformationDataDto;
import com.daimler.data.dto.fabric.DataProductConnectionsDto;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DdxOnboardingRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dataProductName;
    private String dataProductDescription;
    private String informationOwner;
    private String cdcDatabaseLink;
    private String cdcDataProductLink;
    private String securityLevel;
    private List<String> purposes;
    private List<String> dataProviders;
    private String divisions;
    private Boolean isTransferPricing;
    private List<String> criteriaTransferPricing;
    private List<String> qualificationTransferPricing;
    private DdxLegalEntityDto legalEntity;
    private Boolean fulfillsDataCloudFramework;
    private String businessDomain;
    private Boolean personalDataContained;
    private List<DataProductConnectionsDto> dataProductConnections;
}