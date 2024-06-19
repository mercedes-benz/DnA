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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
import com.mb.dna.datalakehouse.dto.DataLakeTableColumnDetailsVO;
import com.mb.dna.datalakehouse.dto.DatalakeTableVO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TrinoClient {

	@Value("${trino.uri}")
	private String trinoBaseUri;
	
	@Value("${trino.user}")
	private String trinoUser;
	
	@Value("${trino.host}")
	private String trinoHost;
	
	@Value("${trino.port}")
	private String trinoPort;
	
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
	
	public List<DataLakeTableColumnDetailsVO> showColumns(String catalogName, String schemaName, String tableName){
		List<DataLakeTableColumnDetailsVO> columns = new ArrayList<>();
		ResultSet results = null;
		String url = "jdbc:trino://"+ trinoHost + ":" + trinoPort ;
		Properties properties = new Properties();
		properties.setProperty("user", trinoUser);
		properties.setProperty("password", trinoPassword);
		properties.setProperty("SSL", trinoSSL);
		properties.setProperty("SSLVerification", trinoSSLVerification);
		String sql = "";
		try {
			Connection connection = DriverManager.getConnection(url, properties);
			Statement statement = connection.createStatement();
			String statementString = "show columns from " + catalogName + "." + schemaName + "." + tableName ;
		    sql = statementString; 
		    results = statement.executeQuery(sql);
		    log.info("Executed statement: {} successfully",sql);
			if(results!=null) {
				while(results.next()) {
					String tempColumnName = results.getString("Column");
					String tempColumnType = results.getString("Type");
					String tempColumnComment = results.getString("Comment");
					DataLakeTableColumnDetailsVO tempColumn = new DataLakeTableColumnDetailsVO();
					tempColumn.setColumnName(tempColumnName);
					tempColumn.setDataType(tempColumnType);
					tempColumn.setComment(tempColumnComment);
					columns.add(tempColumn);
				}
			}
		    connection.close();
		}catch(Exception e) {
			log.error("Failed while executing show columns statement, {} using trino jdbc with exception {}",sql, e.getMessage());
		}
		return columns;
	}
	
	public List<String> showSchemas(String catalogName, String schemaNamePattern){
		List<String> schemas = new ArrayList<>();
		ResultSet results = null;
		String url = "jdbc:trino://"+ trinoHost + ":" + trinoPort ;
		Properties properties = new Properties();
		properties.setProperty("user", trinoUser);
		properties.setProperty("password", trinoPassword);
		properties.setProperty("SSL", trinoSSL);
		properties.setProperty("SSLVerification", trinoSSLVerification);
		String sql = "";
		try {
			Connection connection = DriverManager.getConnection(url, properties);
			Statement statement = connection.createStatement();
			String statementString = "show schemas from " + catalogName + " like '" + schemaNamePattern + "'";
		    sql = statementString; 
		    results = statement.executeQuery(sql);
		    log.info("Executed statement: {} successfully",sql);
			if(results!=null) {
				while(results.next()) {
					String tempSchemaName = results.getString("Schema");
					schemas.add(tempSchemaName);
				}
			}
		    connection.close();
		}catch(Exception e) {
			log.error("Failed while executing statement {} using trino jdbc with exception {}",sql, e.getMessage());
		}
		return schemas;
	}
	
	public List<String> showTables(String catalogName, String schemaName, String tableNamePattern){
		List<String> schemas = new ArrayList<>();
		ResultSet results = null;
		String url = "jdbc:trino://"+ trinoHost + ":" + trinoPort ;
		Properties properties = new Properties();
		properties.setProperty("user", trinoUser);
		properties.setProperty("password", trinoPassword);
		properties.setProperty("SSL", trinoSSL);
		properties.setProperty("SSLVerification", trinoSSLVerification);
		String sql = "";
		try {
			Connection connection = DriverManager.getConnection(url, properties);
			Statement statement = connection.createStatement();
			String statementString = "show tables from " + catalogName + "." + schemaName + " like '" + tableNamePattern + "'";
		    sql = statementString; 
		    results = statement.executeQuery(sql);
		    log.info("Executed statement: {} successfully",sql);
			if(results!=null) {
				while(results.next()) {
					String tempSchemaName = results.getString("Table");
					schemas.add(tempSchemaName);
				}
			}
		    connection.close();
		}catch(Exception e) {
			log.error("Failed while executing statement {} using trino jdbc with exception {}",sql, e.getMessage());
		}
		return schemas;
	}
	
	public ResultSet queryStatements(String statementString) throws Exception{
		
		ResultSet results = null;
		String url = "jdbc:trino://"+ trinoHost + ":" + trinoPort ;
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
		    results = statement.executeQuery(sql);
		    log.info("Executed statement: {} successfully",sql);
		    List<String> schemas = new ArrayList<>();
			if(results!=null) {
				
				while(results.next()) {
					String tempSchemaName = results.getString("Schema");
					schemas.add(tempSchemaName);
				}
			}
		    connection.close();
		}catch(Exception e) {
			log.error("Failed while executing statement {} using trino jdbc with exception {}",sql, e.getMessage());
			throw e;
		}
		return results;
		
	}
	
	public void executeStatments(String statementString) throws Exception{
		String url = "jdbc:trino://"+ trinoHost + ":" + trinoPort ;
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
		    connection.close();
		}catch(Exception e) {
			log.error("Failed while executing statement {} using trino jdbc with exception {}",sql, e.getMessage());
			throw e;
		}
		
	}
	
}
