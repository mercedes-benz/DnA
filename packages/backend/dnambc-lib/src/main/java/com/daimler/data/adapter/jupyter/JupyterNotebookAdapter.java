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

import java.util.ArrayList;
import java.util.List;

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
		 headers.set("Authorization", "token "+ authToken);
		 HttpEntity entity = new HttpEntity<>(headers);
		 String getUserUri = baseUri + "/" + userShortId;
		 ResponseEntity<JupyterUserInfoDto> response = restTemplate.exchange(getUserUri, HttpMethod.GET, entity, JupyterUserInfoDto.class);
		 if(response!=null) {
			 return response.getBody();
		 }
		 else
			 return null;
		 }catch(Exception e) {
			 return null;
		 }
	 }
	 
	 
	 public List<JupyterUserInfoDto> createJupyterUser(String[] usersShortIds) {
		 try {
		 HttpHeaders headers = new HttpHeaders();
		 headers.set("Accept", "application/json");
		 headers.set("Content-Type", "application/json");
		 headers.set("Authorization", "token "+ authToken);
		 JupyterCreateUsersReqDto reqDto = new JupyterCreateUsersReqDto();
		 reqDto.setAdmin(false);
		 reqDto.setUsernames(usersShortIds);
		 HttpEntity<JupyterCreateUsersReqDto> entity = new HttpEntity<JupyterCreateUsersReqDto>(reqDto,headers);
		 String createUserUri = baseUri;
		 ResponseEntity<Object> response = restTemplate.exchange(createUserUri, HttpMethod.POST, entity, Object.class);
		 if(response!=null) {
			 	List<JupyterUserInfoDto> createdUsersInfo = new ArrayList<>();
			 	createdUsersInfo = (List<JupyterUserInfoDto>) response.getBody();
			 	return createdUsersInfo;
		 }
		 return null;
		 }catch(Exception e) {
			 return null;
		 }
	 }
	 
	 
	 public JupyterNotebookGenericResponse startJupyterUserNotebook(String userShortId) {
		 try {
		 HttpHeaders headers = new HttpHeaders();
		 headers.set("Accept", "application/json");
		 headers.set("Content-Type", "application/json");
		 headers.set("Authorization", "token "+ authToken);
		 HttpEntity entity = new HttpEntity<>(headers);
		 String startUserNotebookUri = baseUri + "/" + userShortId + "/server";
		 ResponseEntity<JupyterNotebookGenericResponse> response = restTemplate.exchange(startUserNotebookUri, HttpMethod.POST, entity, JupyterNotebookGenericResponse.class);
		 if(response!=null) {
			 HttpStatus responseStatus = response.getStatusCode();
			 if(responseStatus != null && responseStatus.is2xxSuccessful())
			 {
				 JupyterNotebookGenericResponse startSuccessResponse = new JupyterNotebookGenericResponse("200","Started Successfully");
				 return startSuccessResponse;
			 }
			 	 response.getBody();
		 }
		 return null;
		 }catch(HttpClientErrorException e) {
			 JupyterNotebookGenericResponse startClientErrorResponse = new JupyterNotebookGenericResponse("400","Started Already");
			 return startClientErrorResponse;
		 }catch(Exception e) {
			 JupyterNotebookGenericResponse startServerErrorResponse = new JupyterNotebookGenericResponse("500",e.getMessage());
			 return startServerErrorResponse;
		 }
	 }
	 
	 public JupyterNotebookGenericResponse stopJupyterUserNotebook(String userShortId) {
		 try {
		 HttpHeaders headers = new HttpHeaders();
		 headers.set("Accept", "application/json");
		 headers.set("Content-Type", "application/json");
		 headers.set("Authorization", "token "+ authToken);
		 HttpEntity entity = new HttpEntity<>(headers);
		 String startUserNotebookUri = baseUri + "/" + userShortId + "/server";
		 ResponseEntity<JupyterNotebookGenericResponse> response = restTemplate.exchange(startUserNotebookUri, HttpMethod.DELETE, entity, JupyterNotebookGenericResponse.class);
		 if(response!=null) {
			 HttpStatus responseStatus = response.getStatusCode();
			 if(responseStatus != null && responseStatus.is2xxSuccessful())
			 {
				 JupyterNotebookGenericResponse startSuccessResponse = new JupyterNotebookGenericResponse("200","Stopped Successfully");
				 return startSuccessResponse;
			 }
			 	 response.getBody();
		 }
		 return null;
		 }catch(HttpClientErrorException e) {
			 JupyterNotebookGenericResponse startClientErrorResponse = new JupyterNotebookGenericResponse("400","Stopped Already");
			 return startClientErrorResponse;
		 }catch(Exception e) {
			 JupyterNotebookGenericResponse startServerErrorResponse = new JupyterNotebookGenericResponse("500",e.getMessage());
			 return startServerErrorResponse;
		 }
	 }
}
