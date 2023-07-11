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

package com.daimler.data.adapter.jupyter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Configuration
public class JupyterNotebookAdapter {

	private static Logger LOGGER = LoggerFactory.getLogger(JupyterNotebookAdapter.class);

	@Value("${jupyternotebook.baseuri}")
	private String baseUri;

	@Value("${jupyternotebook.token}")
	private String authToken;

	@Autowired
	RestTemplate restTemplate;

	public JupyterUserInfoDto getJupyterUserDetails(String userShortId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token " + authToken);
			HttpEntity entity = new HttpEntity<>(headers);
			String getUserUri = baseUri + "/" + userShortId;
			ResponseEntity<JupyterUserInfoDto> response = restTemplate.exchange(getUserUri, HttpMethod.GET, entity,
					JupyterUserInfoDto.class);
			if (response != null) {
				LOGGER.info("In getJupyterUserDetails, returning after getting valid response from Jupyter for user {}", userShortId);
				return response.getBody();
			} else
				LOGGER.info("In getJupyterUserDetails, returning null after getting response from Jupyter for user {}", userShortId);
			return null;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			LOGGER.error("Exception stacktrace for fetch user {} notebooks with message: {}",userShortId, sw.toString());
			return null;
		}
	}

	public List<JupyterUserInfoDto> createJupyterUser(String[] usersShortIds) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token " + authToken);
			JupyterCreateUsersReqDto reqDto = new JupyterCreateUsersReqDto();
			reqDto.setAdmin(false);
			reqDto.setUsernames(usersShortIds);
			HttpEntity<JupyterCreateUsersReqDto> entity = new HttpEntity<JupyterCreateUsersReqDto>(reqDto, headers);
			String createUserUri = baseUri;
			ResponseEntity<Object> response = restTemplate.exchange(createUserUri, HttpMethod.POST, entity,
					Object.class);
			if (response != null) {
				List<JupyterUserInfoDto> createdUsersInfo = new ArrayList<>();
				createdUsersInfo = (List<JupyterUserInfoDto>) response.getBody();
				LOGGER.info("In createJupyterUser, returning after getting response from Jupyter for shortIds {}", usersShortIds.toString());
				return createdUsersInfo;
			}
			LOGGER.info("In createJupyterUser, returning after getting no/null response from Jupyter for shortIds {}",usersShortIds.toString());
			return null;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			LOGGER.error("Exception stacktrace for create notebooks for user(s) {} with message: {}",usersShortIds.toString(), sw.toString());
			return null;
		}
	}

	public JupyterNotebookGenericResponse startJupyterUserNotebook(String userShortId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token " + authToken);
			HttpEntity entity = new HttpEntity<>(headers);
			String startUserNotebookUri = baseUri + "/" + userShortId + "/server";
			ResponseEntity<JupyterNotebookGenericResponse> response = restTemplate.exchange(startUserNotebookUri,
					HttpMethod.POST, entity, JupyterNotebookGenericResponse.class);
			if (response != null) {
				HttpStatus responseStatus = response.getStatusCode();
				if (responseStatus != null && responseStatus.is2xxSuccessful()) {
					JupyterNotebookGenericResponse startSuccessResponse = new JupyterNotebookGenericResponse("200",
							"Started Successfully");
					LOGGER.info(
							"In startJupyterUserNotebook, returning after getting starting notebook successfully for {} ",
							userShortId);
					return startSuccessResponse;
				}
				response.getBody();
			}
			LOGGER.info(
					"In startJupyterUserNotebook, returning after getting no/null response from starting notebook successfully for {} ",
					userShortId);
			return null;
		} catch (HttpClientErrorException e) {
			JupyterNotebookGenericResponse startClientErrorResponse = new JupyterNotebookGenericResponse("400",
					"Started Already");
			LOGGER.info("In startJupyterUserNotebook, {} notebook already started, returning", userShortId);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			LOGGER.error("Exception stacktrace for startJupyterUserNotebook for user(s) {} with message: {}",userShortId.toString(), sw.toString());
			return startClientErrorResponse;
		} catch (Exception e) {
			JupyterNotebookGenericResponse startServerErrorResponse = new JupyterNotebookGenericResponse("500",
					e.getMessage());
			LOGGER.error("In startJupyterUserNotebook, failed to start notebook {}", e.getMessage());
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			LOGGER.error("Exception stacktrace for startJupyterUserNotebook for user(s) {} with message: {}",userShortId.toString(), sw.toString());
			return startServerErrorResponse;
		}
	}

	public JupyterNotebookGenericResponse stopJupyterUserNotebook(String userShortId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "token " + authToken);
			HttpEntity entity = new HttpEntity<>(headers);
			String startUserNotebookUri = baseUri + "/" + userShortId + "/server";
			ResponseEntity<JupyterNotebookGenericResponse> response = restTemplate.exchange(startUserNotebookUri,
					HttpMethod.DELETE, entity, JupyterNotebookGenericResponse.class);
			if (response != null) {
				HttpStatus responseStatus = response.getStatusCode();
				if (responseStatus != null && responseStatus.is2xxSuccessful()) {
					JupyterNotebookGenericResponse startSuccessResponse = new JupyterNotebookGenericResponse("200",
							"Stopped Successfully");
					LOGGER.info("In stopJupyterUserNotebook, {} notebook stopped successfully, returning", userShortId);
					return startSuccessResponse;
				}
				response.getBody();
			}
			return null;
		} catch (HttpClientErrorException e) {
			JupyterNotebookGenericResponse startClientErrorResponse = new JupyterNotebookGenericResponse("400",
					"Stopped Already");
			LOGGER.info("In stopJupyterUserNotebook, {} notebook already stopped, returning", userShortId);
			return startClientErrorResponse;
		} catch (Exception e) {
			JupyterNotebookGenericResponse startServerErrorResponse = new JupyterNotebookGenericResponse("500",
					e.getMessage());
			LOGGER.error("In stopJupyterUserNotebook, failed to stop notebook {}", e.getMessage());
			return startServerErrorResponse;
		}
	}
}
