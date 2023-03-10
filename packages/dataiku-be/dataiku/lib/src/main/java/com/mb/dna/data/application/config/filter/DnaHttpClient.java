package com.mb.dna.data.application.config.filter;

import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.dna.data.application.adapter.dna.DnaClientConfig;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.ssl.SslConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DnaHttpClient {

	@Inject
	HttpClient client;
	
	@Inject
	DnaClientConfig dnaClientConfig;
	
	public void verifyLogin(String jwt) {
		
		try {
			String url =  "https:" + dnaClientConfig.getUri() + dnaClientConfig.getVerifyLoginUri();
			System.out.println("url is "+ url);
			HttpClientConfiguration config = new DefaultHttpClientConfiguration();
			SslConfiguration sslConfig = new SslConfiguration();
			sslConfig.setEnabled(false);
			config.setSslConfiguration(sslConfig);
			HttpRequest<?> req = HttpRequest.POST(url, null).header("Accept", "application/json")
			.header("Content-Type", "application/json")
			.header("Authorization", jwt);
//			URL dnaVerifyLoginUrl = new URL(url);
//			HttpResponse<VerifyLoginResponseDto> response = HttpClient.create(dnaVerifyLoginUrl).toBlocking().exchange(req,VerifyLoginResponseDto.class);
			HttpResponse<VerifyLoginResponseDto> response = client.toBlocking().exchange(req,VerifyLoginResponseDto.class);
			VerifyLoginResponseDto responseBody = new VerifyLoginResponseDto();
			if(response!=null && response.getBody()!=null) {
				responseBody = response.getBody().get();
				
			}
//			Publisher<VerifyLoginResponseDto>  response = client.retrieve(req,VerifyLoginResponseDto.class);
//			Mono<VerifyLoginResponseDto> responseBody = Mono.from(response);
//			responseBody.subscribe(value -> System.out.println(value),error -> error.printStackTrace());
			ObjectMapper mapper = new ObjectMapper();
			System.out.println(mapper.writeValueAsString(responseBody));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
