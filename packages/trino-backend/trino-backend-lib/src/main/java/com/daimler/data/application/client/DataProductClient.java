package com.daimler.data.application.client;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.dataproduct.DataProductCollection;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataProductClient {

	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private RestTemplate restTemplate;
	    
	@Value("${dataproduct.baseUri}")
	private String dataProductBaseUri;
	
	private static final String MYDATAPRODUCTS_PATH = "/api/dataproducts/bookmarked";
	
	public DataProductCollection getMyDataProducts() {
		try {
			HttpHeaders headers = new HttpHeaders();
			String userinfo = httpRequest.getHeader("dna-request-userdetails");
            headers.set("Accept", "application/json");
            headers.set("dna-request-userdetails", userinfo);
            headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String getMyDataProductsUrl = dataProductBaseUri + MYDATAPRODUCTS_PATH;
			ResponseEntity<DataProductCollection> response = restTemplate.exchange(getMyDataProductsUrl, HttpMethod.GET,requestEntity, DataProductCollection.class);
			if (response.hasBody() && response.getBody()!=null && response.getStatusCode().is2xxSuccessful()) {
					log.info("Successfully fetched my dataproducts for the user");
					return response.getBody();
				}
			else {
					log.info("Failed to get proper response from fetch my dataproducts for the user");
					return null;
				}
			}
		catch(Exception e) {
				log.error("Failed while getting  my dataproducts for the user with exception {}", e.getMessage());
			}
			return null;

	}
	
}
