package com.daimler.data.application.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.daimler.data.dto.cdc.LakehouseTablesCollectionDto;
import com.daimler.data.dto.cdc.TableSchemaCollectionDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.daimler.data.service.cdc.CdcPushService;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CdcPushClient {

    @Value("${cdcPush.api.baseurl}")
    private String baseUrl;

    @Value("${cdcPush.api.key}")
    private String apiKey;

    @Autowired
    private RestTemplate proxyRestTemplate;


    @Autowired
    private RestTemplate restTemplate;

    public LakehouseTablesCollectionDto getLakehouseTables(String workspaceId, String lakehouseId) {
        LakehouseTablesCollectionDto responseDto = null;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("x-api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tables")
                    .queryParam("workspaceId", workspaceId)
                    .queryParam("lakehouseId", lakehouseId)
                    .toUriString();

            
            log.info("Building URL: {}", url);//remove

            ResponseEntity<LakehouseTablesCollectionDto> response = restTemplate.exchange(url, HttpMethod.GET,
                    requestEntity, LakehouseTablesCollectionDto.class
            );

            if (response != null && response.hasBody()) {
                responseDto = response.getBody();
                // log.info("Fetched lakehouse tables for workspaceId: {}, lakehouseId: {}", workspaceId, lakehouseId);
                // log.info("Full response body: {}", responseDto); 
            } else {
                log.warn("Empty response received for workspaceId: {}, lakehouseId: {}", workspaceId, lakehouseId);
            }

        } catch (Exception e) {
            log.error("Exception occurred while fetching lakehouse tables: {}", e.getMessage());
        }

        return responseDto;
    }


    public TableSchemaCollectionDto getTableSchema(String workspaceId, String lakehouseId, String tableName, String schemaName) {
        TableSchemaCollectionDto responseDto = null;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("x-api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tables/schema")
                    .queryParam("workspaceId", workspaceId)
                    .queryParam("lakehouseId", lakehouseId)
                    .queryParam("tableName", tableName);

            if (schemaName != null && !schemaName.isEmpty()) {
                uriBuilder.queryParam("schemaName", schemaName);
            }

            String url = uriBuilder.toUriString();

            log.info("Building URL for table schema: {}", url); //remove

            ResponseEntity<TableSchemaCollectionDto> response = restTemplate.exchange( url, HttpMethod.GET,
                    requestEntity, TableSchemaCollectionDto.class);

            if (response != null && response.hasBody()) {
                responseDto = response.getBody();
                log.info("Fetched table schema for tableName: {} in lakehouseId: {}", tableName, lakehouseId);
            } else {
                log.warn("Empty schema response for tableName: {} in lakehouseId: {}", tableName, lakehouseId);
            }

        } catch (Exception e) {
            log.error("Exception occurred while fetching table schema: {}", e.getMessage());
        }

        return responseDto;
    }
}
