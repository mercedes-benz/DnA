/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.application.client;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import com.daimler.data.dto.fabricWorkspace.LakehouseColumnCollectionResponseVO;
import com.daimler.data.dto.fabricWorkspace.LakehouseTableCollectionResponseVO;
import com.daimler.data.dto.fabricCatalogManagement.LakehouseObjectsResponseVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FabricCDCPushServiceClient {
    
   
   	@Value("${cdcIntegration.fabricCdcPushService.baseUrl}")
    private String baseUrl;

    @Value("${cdcIntegration.fabricCdcPushService.authToken}")
    private String authToken;

    @Autowired
    private RestTemplate restTemplate;

    private String extractErrorMessage(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jsonResponse);
            
            // Try to extract 'detail' field first
            if (node.has("detail")) {
                return node.get("detail").asText();
            }
            // Try to extract 'message' field
            if (node.has("message")) {
                return node.get("message").asText();
            }
            // Return the whole response if no specific field found
            return jsonResponse;
        } catch (Exception e) {
            // If parsing fails, return the original response
            return jsonResponse;
        }
    }

     public LakehouseTableCollectionResponseVO getLakehouseTables(String workspaceId, String lakehouseId) {
        LakehouseTableCollectionResponseVO vo = new LakehouseTableCollectionResponseVO();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("x-api-key", authToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tables")
                    .queryParam("workspaceId", workspaceId)
                    .queryParam("lakehouseId", lakehouseId)
                    .toUriString();

            ResponseEntity<LakehouseTableCollectionResponseVO> response = restTemplate.exchange(url, HttpMethod.GET,
                    requestEntity, LakehouseTableCollectionResponseVO.class
            );
			if(response.getStatusCode().is2xxSuccessful()) {
				if (response != null && response.hasBody()) {
					vo = response.getBody();
					vo.setResponseCode(response.getStatusCode().toString());
				} else {
					log.warn("Empty response received for workspaceId: {}, lakehouseId: {}", workspaceId, lakehouseId);
					vo.setResponseCode(response.getStatusCode().toString());
				}
			}
			vo.setResponseCode(response.getStatusCode().toString());

        } catch (HttpStatusCodeException e) {
            log.error("HTTP error occurred while fetching lakehouse tables for workspaceId: {}, lakehouseId: {}, status: {}, response: {}",
                    workspaceId, lakehouseId, e.getStatusCode(), e.getResponseBodyAsString());
            vo.setResponseCode(String.valueOf(e.getStatusCode().value()));
        } catch (Exception e) {
            log.error("Exception occurred while fetching lakehouse tables: {}", e.getMessage());
			vo.setResponseCode(String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        }

        return vo;
    }

	public LakehouseColumnCollectionResponseVO getTableSchema( String workspaceId, String lakehouseId, String schemaName, String tableName){
		LakehouseColumnCollectionResponseVO vo = new LakehouseColumnCollectionResponseVO();

		try{
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("x-api-key", authToken);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> requestEntity = new HttpEntity<>(headers);

			String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tables/schema")
					.queryParam("workspaceId", workspaceId)
					.queryParam("lakehouseId", lakehouseId)
					.queryParam("tableName", tableName)
					.queryParam("schemaName", schemaName)
					.toUriString();


			ResponseEntity<LakehouseColumnCollectionResponseVO> response = restTemplate.exchange(url, HttpMethod.GET,
					requestEntity, LakehouseColumnCollectionResponseVO.class
			);
			if(response.getStatusCode().is2xxSuccessful()) {
				
				if (response != null && response.hasBody()) {
					vo = response.getBody();
					vo.setResponseCode(response.getStatusCode().toString());
					log.info("Fetched table schema for workspaceId: {}, lakehouseId: {}, tableName: {}", workspaceId, lakehouseId, tableName);
				} else {
					log.warn("Empty response received for workspaceId: {}, lakehouseId: {}, tableName: {}", workspaceId, lakehouseId, tableName);
					vo.setResponseCode(response.getStatusCode().toString());
				}
			}
			vo.setResponseCode(response.getStatusCode().toString());

		} catch (HttpStatusCodeException e) {
			log.error("HTTP error occurred while fetching table schema for workspaceId: {}, lakehouseId: {}, tableName: {}, status: {}, response: {}",
					workspaceId, lakehouseId, tableName, e.getStatusCode(), e.getResponseBodyAsString());
			vo.setResponseCode(String.valueOf(e.getStatusCode().value()));
		} catch (Exception e) {
			log.error("Exception occurred while fetching table schema: {}", e.getMessage());
			vo.setResponseCode(String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR));
		}
		return vo;
	}

	
	public LakehouseObjectsResponseVO getLakehouseObjects( String workspaceId, String lakehouseId, String schemaName){
		LakehouseObjectsResponseVO vo = new LakehouseObjectsResponseVO();

		try{
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("x-api-key", authToken);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> requestEntity = new HttpEntity<>(headers);

			String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/lakehouse/objects")
					.queryParam("workspaceId", workspaceId)
					.queryParam("lakehouseId", lakehouseId)
					.queryParam("schemaName", schemaName)
					.toUriString();

			ResponseEntity<LakehouseObjectsResponseVO> response = restTemplate.exchange(url, HttpMethod.GET,
					requestEntity, LakehouseObjectsResponseVO.class
			);
			if(response.getStatusCode().is2xxSuccessful()) {
				
				if (response != null && response.hasBody()) {
					vo = response.getBody();
					vo.setResponseCode(String.valueOf(response.getStatusCode().value()));
					log.info("Fetched table schema for workspaceId: {}, lakehouseId: {}, tableName: {}", workspaceId, lakehouseId, schemaName);
				} else {
					log.warn("Empty response received for workspaceId: {}, lakehouseId: {}, tableName: {}", workspaceId, lakehouseId, schemaName);
					vo.setResponseCode(String.valueOf(response.getStatusCode().value()));
					vo.setErrorMessage("Empty response received");
				}
			} else {
				String errorMessage = "Error response received with status: " + response.getStatusCode();
				log.error("Error response received for workspaceId: {}, lakehouseId: {}, schemaName: {} with status: {}", workspaceId, lakehouseId, schemaName, response.getStatusCode());
				vo.setResponseCode(String.valueOf(response.getStatusCode().value()));
				vo.setErrorMessage(errorMessage);
			}

		} catch (HttpStatusCodeException e) {
			String errorResponse = e.getResponseBodyAsString();
			String errorMessage = extractErrorMessage(errorResponse);
			log.error("HTTP error occurred while fetching table schema: {} - {}", e.getStatusCode(), errorMessage);
			vo.setResponseCode(String.valueOf(e.getStatusCode().value()));
			vo.setErrorMessage(errorMessage);
		} catch (Exception e) {
			String errorMessage = "Exception occurred while fetching table schema: " + e.getMessage();
			log.error("Exception occurred while fetching table schema: {}", e.getMessage());
			vo.setResponseCode(String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR));
			vo.setErrorMessage(errorMessage);
		}
		return vo;
	}



}
