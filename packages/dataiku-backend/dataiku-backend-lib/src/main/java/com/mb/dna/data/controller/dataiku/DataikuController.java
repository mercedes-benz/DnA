package com.mb.dna.data.controller.dataiku;

import static io.micronaut.http.MediaType.APPLICATION_JSON;

import com.mb.dna.data.application.adapter.dna.UserInfo;
import com.mb.dna.data.application.adapter.dna.UserStore;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Controller("/api/dataiku")
public class DataikuController {
	
	@Inject
	UserStore userStore;
	
	@Get(uri="/sample", produces= APPLICATION_JSON)
    @Operation(summary = "Sample test api",
            description = "Sample test api description"
    )
    @ApiResponse(responseCode = "200", description = "Sample test api found and returned",
            content = @Content(mediaType = "application/json"
            ,schema = @Schema(type="UserInfo"))
    )
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "500", description = "Remote error, server is going nuts")
    @Tag(name = "dataiku")
    public UserInfo index() {
    		return this.userStore.getUserInfo();
    }
    
}