package com.mb.dna.data.dataiku.api.controller;

import static io.micronaut.http.MediaType.APPLICATION_JSON;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.mb.dna.data.application.adapter.dna.UserInfo;
import com.mb.dna.data.application.adapter.dna.UserStore;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectCreateRequestDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectResponseDto;
import com.mb.dna.data.dataiku.service.DataikuService;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Controller("/api")
public class DataikuController {
	
	@Inject
	UserStore userStore;
	
	@Inject
	DataikuService service;
	
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
	
	@POST
    @Path("/dataiku")
    @Operation(summary = "Create dataiku project",
            description = "Create dataiku project")
	@ApiResponse(responseCode = "201", description = "Project created",
				content = @Content(mediaType = "application/json"
			    ,schema = @Schema(type="DataikuProjectResponseDto")))
	@Tag(name = "dataiku")
    public Response createProject(
            @RequestBody(description = "Data to create dataiku project", required = true,
                    content = @Content(
                            schema = @Schema(implementation = DataikuProjectCreateRequestDto.class))) DataikuProjectCreateRequestDto request) {
		DataikuProjectResponseDto responseDto = new DataikuProjectResponseDto();
		responseDto = service.createProject(this.userStore.getUserInfo().getId(), request.getData());
		return Response.ok().entity(responseDto).build();
	}
    
}