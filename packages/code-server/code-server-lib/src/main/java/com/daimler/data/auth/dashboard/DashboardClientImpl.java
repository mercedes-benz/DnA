package com.daimler.data.auth.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.auth.client.AttachApiAuthoriserPluginRequestVO;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.web.client.RestTemplate;
import lombok.extern.java.Log;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DashboardClientImpl implements DashboardClient {

	@Value("${dashboard.uri}")
	private String dashboardBaseUri;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	HttpServletRequest httpRequest;

	private static final String CREATE_DEPARTMENT = "/api/departments";

	@Override
	public GenericMessage createDepartment(String value) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();

		try {
			HttpHeaders headers = new HttpHeaders();
			String userinfo = httpRequest.getHeader("dna-request-userdetails");
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("dna-request-userdetails", userinfo);

			String addDepartmentValue = dashboardBaseUri + CREATE_DEPARTMENT;

			// Create JSON data object
			JSONObject jsonData = new JSONObject();
			jsonData.put("name", value);

			// Create request body
			JSONObject requestBody = new JSONObject();
			requestBody.put("data", jsonData);

			// Convert request body to JSON string
			String requestBodyStr = requestBody.toString();

			// Create HttpEntity with the JSON string and headers
			HttpEntity<String> entity = new HttpEntity<>(requestBodyStr, headers);
			ResponseEntity<String> addDetartment = restTemplate.exchange(addDepartmentValue, HttpMethod.POST, entity,
					String.class);

			if (addDetartment != null && addDetartment.getStatusCode() != null) {
				if (addDetartment.getStatusCode().is2xxSuccessful()) {
					status = "SUCCESS";
					log.info("Successfully added department: {} ", value);
				} else {
					log.info("Warnings while adding department: {} HTTP status code: {}", value,
							addDetartment.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from add department API: " + addDetartment.getBody()
							+ " Response Code is: " + addDetartment.getStatusCodeValue());
					warnings.add(warning);
				}
			}
		} catch (Exception e) {
			log.error("Failed to add department: {}. Exception: {}.", value, e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Error occurred while adding department: " + value + " with exception: " + e.getMessage());
			errors.add(error);
		}

		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}

	@Override
	public GenericMessage getDepartment(String departmentName) {
		GenericMessage response = new GenericMessage();
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		String departmentsJson = "";
		String status = "FAILED";
		try {
			HttpHeaders headers = new HttpHeaders();
			String userinfo = httpRequest.getHeader("dna-request-userdetails");
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("dna-request-userdetails", userinfo);
			String getDepartmentsUri = dashboardBaseUri + CREATE_DEPARTMENT;

			// Create HttpEntity with headers
			HttpEntity<String> entity = new HttpEntity<>(headers);

			ResponseEntity<String> departmentsResponse = restTemplate.exchange(getDepartmentsUri, HttpMethod.GET,
					entity, String.class);

			if (departmentsResponse != null && departmentsResponse.getStatusCode() != null) {
				if (departmentsResponse.getStatusCode().is2xxSuccessful()) {
					departmentsJson = departmentsResponse.getBody();
					status = this.checkDepartment(departmentsJson, departmentName);
					log.info("Successfully retrieved departments");
				} else {
					log.warn("Warnings while retrieving departments, HTTP status code: {}",
							departmentsResponse.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from get departments API: " + departmentsResponse.getBody()
							+ " Response Code is: " + departmentsResponse.getStatusCodeValue());
					warnings.add(warning);
				}
			}
		} catch (Exception e) {
			log.error("Failed to retrieve departments with exception: {}.", e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Error occurred while retrieving departments with exception: " + e.getMessage());
			errors.add(error);
		}

		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}

	private String checkDepartment(String departmentsJson, String departmentName) {
		String status = "FAILED";
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(departmentsJson);
			ArrayNode dataNode = (ArrayNode) rootNode.path("data");

			boolean departmentFound = false;

			if (departmentName != null && !departmentName.isEmpty()) {
				ArrayNode filteredNode = mapper.createArrayNode();

				for (JsonNode node : dataNode) {
					if (node.path("name").asText().equalsIgnoreCase(departmentName)) {
						filteredNode.add(node);
						departmentFound = true;
					}
				}
				((ObjectNode) rootNode).set("data", filteredNode);
			}

			departmentsJson = rootNode.toString();
			status = departmentFound ? "SUCCESS" : "FAILED";
		} catch (Exception e) {
			log.error("Failed to checking department is present or not with exception: {}.", e.getMessage());
		}
		return status;
	}

}
