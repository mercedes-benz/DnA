package com.mb.dna.data.userprivilege.api.controller;

import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.mb.dna.data.api.controller.exceptions.GenericMessage;
import com.mb.dna.data.application.adapter.dataiku.DataikuClientConfig;
import com.mb.dna.data.userprivilege.api.dto.UserPrivilegeCollectionDto;
import com.mb.dna.data.userprivilege.service.UserPrivilegeService;

import io.micronaut.http.annotation.Controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Controller("/service")
public class UserPrivilegeServiceController {

	@Inject
	private UserPrivilegeService service;
	
	@Inject
	DataikuClientConfig dataikuClientConfig;
	
	
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
            @Parameter(description = "The id of the userdetails record that needs to be deleted", required = true) @PathParam("id") String id,
            @Parameter(description = "Header required to authenticate access of delete service api", required = true) @HeaderParam("x-api-key") String token) {
		String accessKey = dataikuClientConfig.getUserPrivilegesAccessKey();
		if(!accessKey.equalsIgnoreCase(token)) {
			return Response.status(Status.UNAUTHORIZED).entity(null).build();
		}
		boolean isExists = service.isExist(id);
		if(!isExists) {
			return Response.status(Status.NOT_FOUND).entity(null).build();
		}
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
            @RequestBody(description = "User records collection", required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserPrivilegeCollectionDto.class))) UserPrivilegeCollectionDto collection,
            @Parameter(description = "Header required to authenticate access of bulkadd service api", required = true) @HeaderParam("x-api-key") String token) {
		String accessKey = dataikuClientConfig.getUserPrivilegesAccessKey();
		if(!accessKey.equalsIgnoreCase(token)) {
			return Response.status(Status.UNAUTHORIZED).entity(null).build();
		}
		GenericMessage responseMsg = service.bulkAdd(collection);
		return Response.ok().entity(responseMsg).build();
	}
    
	
}
