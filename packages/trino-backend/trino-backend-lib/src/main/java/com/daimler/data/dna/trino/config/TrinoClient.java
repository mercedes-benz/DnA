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


package com.daimler.data.dna.trino.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.TrinoQueryResponse;
import com.daimler.data.dto.TrinoResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TrinoClient {

	@Value("${trino.uri}")
	private String trinoBaseUri;
	
	@Value("${trino.user}")
	private String trinoUser;
	
	@Value("${trino.password}")
	private String trinoPassword;
	
	@Value("${trino.ssl}")
	private String trinoSSL;
	
	@Value("${trino.sslverification}")
	private String trinoSSLVerification;

	@Autowired
	RestTemplate restTemplate;
	
	private static String queryUri = "/v1/query";
	
	private static String statementUri = "/v1/statement";
	
	public List<TrinoQueryResponse> queryIds() throws Exception{
		List<TrinoQueryResponse> queryList = new ArrayList<>();
		try {
			String authStr = trinoUser+":"+trinoPassword;
		    String base64Creds = Base64.getEncoder().encodeToString(authStr.getBytes());
		    
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Basic " + base64Creds);

			HttpEntity entity = new HttpEntity<>(headers);
			String trinoQueryAllIdsURL = trinoBaseUri+queryUri;
			ResponseEntity<TrinoQueryResponse[]> response = restTemplate.exchange(trinoQueryAllIdsURL, HttpMethod.GET, entity, TrinoQueryResponse[].class);
			if (response != null && response.hasBody()) {
				TrinoQueryResponse[] results = response.getBody();
				queryList = Arrays.asList(results); 
				log.info("Successfully fetched all query ids using REST API from trino running at {}", trinoBaseUri);
			}
			return queryList;
		} catch (Exception e) {
			log.error("Failed while fetching all query ids using trino REST API with exception {}",e.getMessage());
			throw e;
		}
	}
	
	public TrinoResponse executeStatements(String statementString) throws Exception{
		TrinoResponse result = new TrinoResponse();
		try {
			String authStr = trinoUser+":"+trinoPassword;
		    String base64Creds = Base64.getEncoder().encodeToString(authStr.getBytes());
		    
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Basic " + base64Creds);

			HttpEntity<String> entity = new HttpEntity<String>(statementString,headers);
			String trinoExecuteStatementURL = trinoBaseUri+statementUri;
			ResponseEntity<TrinoResponse> response = restTemplate.exchange(trinoExecuteStatementURL, HttpMethod.POST, entity, TrinoResponse.class);
			if (response != null && response.hasBody()) {
				result = response.getBody();
				log.info("Successfully executed statement {} on trino running at {}", statementString, trinoBaseUri);
				if(result != null && result.getStats()!=null) {
					log.info("result for statement {}, id: {} and state {}",statementString, result.getId(), result.getStats().getState());
				}
			}
			return result;
		} catch (Exception e) {
			log.error("Failed while executing statement using trino REST API with exception {}",e.getMessage());
			throw e;
		}
	}
	
	
	public void executeStatments(String statementString) throws Exception{
		String url = "jdbc:trino://"+ trinoBaseUri.split("//")[1] + ":443";
		Properties properties = new Properties();
		properties.setProperty("user", trinoUser);
		properties.setProperty("password", trinoPassword);
		properties.setProperty("SSL", trinoSSL);
		properties.setProperty("SSLVerification", trinoSSLVerification);
		String sql = "";
		try {
			Connection connection = DriverManager.getConnection(url, properties);
			Statement statement = connection.createStatement();
		    sql = statementString; 
		    statement.executeUpdate(sql);
		    log.info("Executed statement: {} successfully",sql);
		}catch(Exception e) {
			log.error("Failed while executing statement {} using trino jdbc with exception {}",sql, e.getMessage());
			throw e;
		}
		
	}
	
}
