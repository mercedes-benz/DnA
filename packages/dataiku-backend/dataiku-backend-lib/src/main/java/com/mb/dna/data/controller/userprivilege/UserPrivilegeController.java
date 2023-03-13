package com.mb.dna.data.controller.userprivilege;

import static io.micronaut.http.MediaType.APPLICATION_JSON;

import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.mb.dna.data.controller.exceptions.GenericMessage;
import com.mb.dna.data.service.UserPrivilegeService;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Controller("/api")
public class UserPrivilegeController {

	@Inject
	private UserPrivilegeService service;
	
	@Get(uri="/userprivilege", produces= APPLICATION_JSON)
    @Operation(summary = "Api to get all user records with profile details",
            description = "Api to get all user records with profile details, to check whether user has privileges to create dataiku project in system"
    )
    @ApiResponse(responseCode = "200", description = "Results found and returned",
            content = @Content(mediaType = "application/json"
            ,schema = @Schema(type="UserPrivilegeCollectionDto"))
    )
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "500", description = "Failed with internal server error")
    @Tag(name = "userprivileges")
    public Response getAll(
    		@Parameter(description = "limit for records result size", required = false) @QueryParam("limit") int limit,
    		@Parameter(description = "offset for records result position", required = false) @QueryParam("offset") int offset,
    		@Parameter(description = "sortBy, possible values userId/profile", required = false) @QueryParam("sortBy") String sortBy,
    		@Parameter(description = "sortOrder, possible values asc/desc", required = false) @QueryParam("sortOrder") String sortOrder,
    		@Parameter(description = "searchTerm to filter by userId", required = false) @QueryParam("searchTerm") String searchTerm
    		) {
		UserPrivilegeCollectionDto response = service.getAllUsersProfileDetail(limit, offset, sortBy, sortOrder, searchTerm);
		if(response!=null && response.getData()!= null && !response.getData().isEmpty())
			return Response.ok().entity(response).build();
		else
			return Response.noContent().entity(response).build();
    }
	
	@DELETE
    @Path("/userprivilege/{id}")
    @Operation(summary = "Delete user",
            description = "Hard delete an user details record from the system")
    @ApiResponse(responseCode = "200", description = "user deteled",
    		content = @Content(mediaType = "application/json"
            ,schema = @Schema(type="GenericMessage")))
    @ApiResponse(responseCode = "400", description = "Invalid id supplied")
    @ApiResponse(responseCode = "404", description = "User not found")
	@Tag(name = "userprivileges")
    public Response deleteUser(
            @Parameter(description = "The id of the userdetails record that needs to be deleted", required = true) @PathParam("id") String id) {
		GenericMessage responseMsg = service.deleteById(id);
		return Response.ok().entity(responseMsg).build();
	}
	
	@POST
    @Path("/userprivilege/bulk")
    @Operation(summary = "Create/Update user records in bulk",
            description = "Create/Update user records in bulk.")
	@ApiResponse(responseCode = "200", description = "user deteled",
				content = @Content(mediaType = "application/json"
			    ,schema = @Schema(type="GenericMessage")))
	@Tag(name = "userprivileges")
    public Response bulkProcessing(
//    		@Parameter(in = ParameterIn.HEADER, name = "Authorization")
//            @HeaderParam(HttpHeaders.AUTHORIZATION) String token,
    		@Schema(required = true, description = "the authentication token " +
    	            "provided after initially authenticating to the application") @HeaderParam("Authorization") String token,
            @RequestBody(description = "User records collection", required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserPrivilegeCollectionDto.class))) UserPrivilegeCollectionDto collection) {
		GenericMessage responseMsg = service.bulkAdd(collection);
		return Response.ok().entity(responseMsg).build();
	}
    
	
}
