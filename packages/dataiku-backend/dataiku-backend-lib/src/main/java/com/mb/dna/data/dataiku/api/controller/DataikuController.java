package com.mb.dna.data.dataiku.api.controller;

import static io.micronaut.http.MediaType.APPLICATION_JSON;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.mb.dna.data.api.controller.exceptions.GenericMessage;
import com.mb.dna.data.application.adapter.dna.UserStore;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectCreateRequestDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectResponseDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectUpdateRequestDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectsCollectionDto;
import com.mb.dna.data.dataiku.service.DataikuService;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
                            schema = @Schema(implementation = DataikuProjectCreateRequestDto.class))) DataikuProjectCreateRequestDto request,
            @Parameter(description = "The id of the dataiku project to be fetched", required = true) @HeaderParam("Authentication") String token) {
		DataikuProjectResponseDto responseDto = new DataikuProjectResponseDto();
		//this.userStore.getUserInfo().getId()
		responseDto = service.createProject("BEALURI", request.getData());
		return Response.ok().entity(responseDto).build();
	}
	
	@Get(uri="/dataiku", produces= APPLICATION_JSON)
    @Operation(summary = "Api to get all dataiku project records from system",
            description = "Api to get all dataiku project records from system"
    )
    @ApiResponse(responseCode = "200", description = "Results found and returned",
            content = @Content(mediaType = "application/json"
            ,schema = @Schema(type="DataikuProjectsCollectionDto"))
    )
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "500", description = "Failed with internal server error")
    @Tag(name = "dataiku")
    public Response getAll(
    		@Parameter(description = "limit for records result size", required = false) @QueryParam("limit") int limit,
    		@Parameter(description = "offset for records result position", required = false) @QueryParam("offset") int offset,
    		@Parameter(description = "sortBy, possible values userId/profile", required = false) @QueryParam("sortBy") String sortBy,
    		@Parameter(description = "sortOrder, possible values asc/desc", required = false) @QueryParam("sortOrder") String sortOrder,
    		@Parameter(description = "searchTerm to filter by userId", required = false) @QueryParam("projectName") String projectName,
    		@Parameter(description = "The id of the dataiku project to be fetched", required = true) @HeaderParam("Authentication") String token
    		) {
		String userId = "BEALURI";//this.userStore.getUserInfo().getId();
		DataikuProjectsCollectionDto response = service.getAllDataikuProjects(userId, offset, limit, sortBy, sortOrder, projectName);
		if(response!=null && response.getData()!= null && !response.getData().isEmpty())
			return Response.ok().entity(response).build();
		else
			return Response.noContent().entity(response).build();
    }
	
	@PUT
    @Path("/dataiku/{id}")
    @Operation(summary = "Update dataiku project",
            description = "Update dataiku project")
	@ApiResponse(responseCode = "201", description = "Project updated",
				content = @Content(mediaType = "application/json"
			    ,schema = @Schema(type="DataikuProjectResponseDto")))
	@Tag(name = "dataiku")
    public Response updateProject(
            @RequestBody(description = "Data to update dataiku project", required = true,
                    content = @Content(
                            schema = @Schema(implementation = DataikuProjectUpdateRequestDto.class))) DataikuProjectUpdateRequestDto request,
            @Parameter(description = "The id of the dataiku project to be deleted", required = true) @PathParam("id") String id,
            @Parameter(description = "The id of the dataiku project to be fetched", required = true) @HeaderParam("Authentication") String token) {
		DataikuProjectResponseDto responseDto = new DataikuProjectResponseDto();
		responseDto = service.updateProject(id, request);
		return Response.ok().entity(responseDto).build();
	}
	
	@DELETE
    @Path("/dataiku/{id}")
    @Operation(summary = "Delete dataiku project",
            description = "Hard delete dataiku project details from the system")
    @ApiResponse(responseCode = "200", description = "dataiku project deteled",
    		content = @Content(mediaType = "application/json"
            ,schema = @Schema(type="GenericMessage")))
    @ApiResponse(responseCode = "400", description = "Invalid id supplied")
    @ApiResponse(responseCode = "404", description = "User not found")
	@Tag(name = "dataiku")
    public Response deleteDataiku(
            @Parameter(description = "The id of the dataiku project to be deleted", required = true) @PathParam("id") String id,
            @Parameter(description = "The id of the dataiku project to be fetched", required = true) @HeaderParam("Authentication") String token) {
		GenericMessage responseMsg = service.deleteById(id);
		return Response.ok().entity(responseMsg).build();
	}
    
	@GET
    @Path("/dataiku/{id}")
    @Operation(summary = "get dataiku project",
            description = "get dataiku project details from the system")
    @ApiResponse(responseCode = "200", description = "dataiku project fetched",
    		content = @Content(mediaType = "application/json"
            ,schema = @Schema(type="DataikuProjectResponseDto")))
    @ApiResponse(responseCode = "400", description = "Invalid id supplied")
    @ApiResponse(responseCode = "404", description = "User not found")
	@Tag(name = "dataiku")
    public Response fetchDataiku(
            @Parameter(description = "The id of the dataiku project to be fetched", required = true) @PathParam("id") String id,
            @Parameter(description = "The id of the dataiku project to be fetched", required = true) @HeaderParam("Authentication") String token) {
		DataikuProjectDto data = service.getById(id);
		DataikuProjectResponseDto responseDto = new DataikuProjectResponseDto();
		responseDto.setData(data);
		return Response.ok().entity(responseDto).build();
	}
	
}