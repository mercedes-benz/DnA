package com.mb.dna.data.application.adapter.dna;


import org.json.JSONObject;

import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;


@Client("${dna.uri}")
@Header(name = "Accept", value = "application/json") 
@Header(name = "Content-Type", value = "application/json") 
public interface DnaClient {
	
	@Header(name = "dna-request-userdetails", value = "{userinfo}")
	@Post("${dna.verifyLoginUri}")
	JSONObject verifyLogin(String userinfo);

}