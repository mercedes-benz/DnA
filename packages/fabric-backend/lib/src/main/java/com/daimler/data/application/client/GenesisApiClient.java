package com.daimler.data.application.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.fabric.CreateRoleResponseDto;
import com.daimler.data.dto.fabricCatalogManagement.LegalEntitiesResponseVO;
import com.daimler.data.util.ConstantsUtility;

import lombok.extern.slf4j.Slf4j;
import com.daimler.data.dto.fabric.LegalEntityDto;

/**
 * This class is to make the call the geneisis api lab 
 * 
 * @author TSATEND
 */
@Component
@Slf4j
public class GenesisApiClient {

    @Value("${genesis.uri}")
    private String uri;

	@Value("${genesis.apiKey}")
	private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    public List<LegalEntityDto> getLegalEntities(){

        List<LegalEntityDto> responseListDto = new ArrayList<>();
        try {
			if(!Objects.nonNull(apiKey)) {
				log.error("Failed to fetch apiKay to invoke genesis Apis");
				return responseListDto;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set(ConstantsUtility.GENESIS_AUTH_HEADER, this.apiKey);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<List<LegalEntityDto>> response = restTemplate.exchange(
				this.uri, HttpMethod.GET,
					requestEntity, new ParameterizedTypeReference<List<LegalEntityDto>>() {});
			if (response!=null && response.hasBody()) {
                responseListDto = response.getBody();
			}
			log.info("successfully featched records : " + responseListDto.size());
		}catch(Exception e) {
			log.error("Failed to get the legal enteties", e.getMessage());
		}
		return responseListDto;
    }

	public LegalEntitiesResponseVO createVoObject(LegalEntityDto legalEntityDto){
		LegalEntitiesResponseVO responseVO = new LegalEntitiesResponseVO();
		responseVO.setLegalName(legalEntityDto.getLegalName());
		responseVO.setCompanyCode(legalEntityDto.getCompanyCode());
		return responseVO;
	}
    
}
