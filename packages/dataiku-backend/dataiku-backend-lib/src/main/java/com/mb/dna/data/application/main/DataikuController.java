package com.mb.dna.data.application.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.dna.data.application.config.filter.UserStore;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;

@Controller("/api/dataiku")
public class DataikuController {

	
	@Inject
	UserStore userStore;
	
    @Get(uri="/sample", produces="text/plain")
    public String index() {
    	try {
    		ObjectMapper mapper = new ObjectMapper();
    		String output = mapper.writeValueAsString(this.userStore.getUserInfo());
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
        return "Example Response of currentUser" + this.userStore.getUserInfo().getId();
    }
    
}