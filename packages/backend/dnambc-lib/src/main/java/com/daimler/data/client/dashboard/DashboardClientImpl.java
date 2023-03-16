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

package com.daimler.data.client.dashboard;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.divisions.DivisionVO;
import com.daimler.data.dto.solution.SolutionVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Component
public class DashboardClientImpl implements DashboardClient {

	private Logger LOGGER = LoggerFactory.getLogger(DashboardClientImpl.class);

	@Autowired
	ServletRequest servletRequest;

	@Autowired
	HttpServletRequest httpRequest;

	@Value("${dashboard.uri}")
	private String dashboardBaseUri;

	private static final String DELETE_DIVISION = "/api/divisions/";

	private static final String DELETE_DATASOURCE = "/api/datasource/";

	private static final String UPDATE_DIVISION = "/api/divisions";
	
	private static final String UPDATE_DEPARTMENT = "/api/departments";

	@Autowired
	RestTemplate restTemplate;

	@Override
	public String deleteDivisionFromEachReport(String id) {
		String status = "";
		String jwt = httpRequest.getHeader("Authorization");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		headers.set("Authorization", jwt);
		String dashboardUri = dashboardBaseUri + DELETE_DIVISION + id;
		HttpEntity entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(dashboardUri, HttpMethod.DELETE, entity, String.class);
		if (response != null && response.hasBody()) {
			status = response.getStatusCode().toString();
		}
		return status;
	}

	@Override
	public String deleteDataSourceFromEachReport(String dataSource) {
		String status = "";
		String jwt = httpRequest.getHeader("Authorization");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		headers.set("Authorization", jwt);
		String dashboardUri = dashboardBaseUri + DELETE_DATASOURCE + dataSource;
		HttpEntity entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(dashboardUri, HttpMethod.DELETE, entity, String.class);
		if (response != null && response.hasBody()) {
			status = response.getStatusCode().toString();
		}
		return status;
	}

	@Override
	public String updateDivisionFromEachReport(DivisionVO vo) {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		JSONObject finalObject = null;
		try {
			String json = ow.writeValueAsString(vo);
			JSONObject obj = new JSONObject(json);
			finalObject = new JSONObject();
			finalObject.put("data", obj);
		} catch (JsonProcessingException e) {
			LOGGER.error("Error while processing json");
		}
		String status = "";
		String jwt = httpRequest.getHeader("Authorization");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		headers.set("Authorization", jwt);
		String dashboardUri = dashboardBaseUri + UPDATE_DIVISION;
		HttpEntity entity = new HttpEntity<>(finalObject.toString(), headers);
		ResponseEntity<String> response = restTemplate.exchange(dashboardUri, HttpMethod.PUT, entity, String.class);
		if (response != null && response.hasBody()) {
			status = response.getStatusCode().toString();
		}
		return status;
	}

	@Override
	public String updateDepartments(SolutionVO vo) {
		UpdateDepartmentRequestWrapperDto reqWrapperDto = new UpdateDepartmentRequestWrapperDto();
		UpdateDepartmentRequestDto reqDto = new UpdateDepartmentRequestDto();
		reqDto.setName(vo.getDepartment());
		reqWrapperDto.setData(reqDto);		
		String status = "";
		String jwt = httpRequest.getHeader("Authorization");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		headers.set("Authorization", jwt);
		String dashboardUri = dashboardBaseUri + UPDATE_DEPARTMENT;
		HttpEntity entity = new HttpEntity<>(reqWrapperDto, headers);	
		try {
			LOGGER.info("Calling dashboard client url from DashboardClientImpl");
			ResponseEntity<String> response = restTemplate.exchange(dashboardUri, HttpMethod.POST, entity, String.class);
			LOGGER.info("Called dashboard client from DashboardClientImpl");
			if (response != null && response.hasBody()) {
				status = response.getStatusCode().toString();
			}
			return status;

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return status= e.getMessage();			
		}
		

	}
}
