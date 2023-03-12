package com.mb.dna.data.application.adapter.dataiku;

import java.util.ArrayList;
import java.util.List;

import com.mb.dna.data.controller.exceptions.GenericMessage;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DataikuClientImpl implements DataikuClient {

	@Inject
	HttpClient client;
	
	@Inject
	DataikuClientConfig dataikuClientConfig;
	
//	getuser/connected
//	adduser
//	updateuser
	
//	Create DnA Preconfigured Project
	
//	update scenario
//	run scenario
	
	
	public List<DataikuUserDto> getDataikuUsers(){
		List<DataikuUserDto> responseBody = new ArrayList<>();
		String url =  "https:" + dataikuClientConfig.getBaseuri() + dataikuClientConfig.getUsersUri();
		HttpRequest<?> req = HttpRequest.GET(url).header("Accept", "application/json")
		.header("Content-Type", "application/json")
		.header("Authorization", "Basic "+dataikuClientConfig.getAuth());
		HttpResponse<List> response = client.toBlocking().exchange(req,List.class);
		if(response!=null && response.getBody()!=null) {
			responseBody = response.getBody().get();
		}
		return responseBody;
	}
	
	public GenericMessage addDataikuUser(DataikuUserDto userRequest) {
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("SUCCESS");
		responseMessage.setErrors(new ArrayList<>());
		responseMessage.setWarnings(new ArrayList<>());
		DataikuUserResponseDto responseBody = new DataikuUserResponseDto();
		String url =  "https:" + dataikuClientConfig.getBaseuri() + dataikuClientConfig.getUsersUri();
		HttpRequest<DataikuUserDto> req = HttpRequest.POST(url,userRequest).header("Accept", "application/json")
		.header("Content-Type", "application/json")
		.header("Authorization", "Basic "+dataikuClientConfig.getAuth());
		HttpResponse<DataikuUserResponseDto> response = client.toBlocking().exchange(req,DataikuUserResponseDto.class);
		if(response!=null && response.getBody()!=null) {
			responseBody = response.getBody().get();
			if(responseBody!=null) {
				if(responseBody.getErrorType()!=null || responseBody.getMessage()!=null) {
					
				}
			}
		}else {
			
		}
		
		return responseMessage;
	}
}
