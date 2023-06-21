package com.mb.dna.data.dataiku.api.controller;

import static io.micronaut.http.MediaType.APPLICATION_JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.mb.dna.data.api.controller.exceptions.GenericMessage;
import com.mb.dna.data.api.controller.exceptions.MessageDescription;
import com.mb.dna.data.application.adapter.dna.UserStore;
import com.mb.dna.data.assembler.DataikuAssembler;
import com.mb.dna.data.dataiku.api.dto.CollaboratorDetailsDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectCreateRequestDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectResponseDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectSummaryCollectionDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectSummaryDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectUpdateRequestDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectsCollectionDto;
import com.mb.dna.data.dataiku.service.DataikuService;
import com.mb.dna.data.userprivilege.api.dto.UserPrivilegeResponseDto;
import com.mb.dna.data.userprivilege.service.UserPrivilegeService;

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
import lombok.extern.slf4j.Slf4j;

@Controller("/api")
@Slf4j
public class DataikuController {
	
	@Inject
	UserStore userStore;
	
	@Inject
	DataikuService service;
	
	@Inject
	DataikuAssembler assembler;
	
	@Inject
	UserPrivilegeService userPrivilegeService;
	
	
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
		try {
		DataikuProjectResponseDto responseDto = new DataikuProjectResponseDto();
		DataikuProjectDto requestedData = request.getData();
		responseDto.setData(requestedData);
		GenericMessage responseMsg = new GenericMessage();
		responseMsg.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		String userId = this.userStore.getUserInfo().getId();
		UserPrivilegeResponseDto ownerDetails = userPrivilegeService.getByShortId(userId);
		if(ownerDetails==null || !ownerDetails.getCanCreate()) {
			MessageDescription errMsg = new MessageDescription("User or privileges not found, cannot create dataiku project");
			log.error("User {} or privileges not found, cannot create dataiku project", userId);
			errors.add(errMsg);
			responseMsg.setErrors(errors);
			responseMsg.setWarnings(warnings);
			responseDto.setResponse(responseMsg);
			return Response.status(Status.UNAUTHORIZED).entity(responseDto).build();
		}
		List<CollaboratorDetailsDto> collaborators = requestedData.getCollaborators();
		List<UserPrivilegeResponseDto> collabPrivilegeDetails = new ArrayList<>();
		if(collaborators!= null && !collaborators.isEmpty()) {
			for(CollaboratorDetailsDto collab : collaborators) {
				UserPrivilegeResponseDto collabDetails = userPrivilegeService.getByShortId(collab.getUserId());
				if(collabDetails==null || !collabDetails.getCanCreate()) {
					MessageDescription errMsg = new MessageDescription("Collaborator " + collab.getUserId() + " or privileges not found, cannot add to dataiku project");
					log.error("Collaborator {} or privileges not found, cannot create dataiku project", userId);
					errors.add(errMsg);
					responseMsg.setErrors(errors);
					responseMsg.setWarnings(warnings);
					responseDto.setResponse(responseMsg);
					return Response.status(Status.BAD_REQUEST).entity(responseDto).build();
				}else {
					collabPrivilegeDetails.add(collabDetails);
				}
			}
		}
        
		String projectName = requestedData.getProjectName();
		String cloudProfile = requestedData.getCloudProfile();
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
        Matcher matcher = pattern.matcher(projectName);
        boolean isNameWithSpecialCharacters = matcher.find();
		if(projectName==null || projectName.isBlank() || projectName.isEmpty() || "".equalsIgnoreCase(projectName)
				|| projectName.length()>22 || isNameWithSpecialCharacters) {
			MessageDescription errMsg = new MessageDescription("Bad request. Project name should be non null value of length less than 22 without any special characters.");
			log.error("Bad request. Project name {} should not be empty, or having any special character and should not exceed more than 22 characters.", projectName);
			errors.add(errMsg);
			responseMsg.setErrors(errors);
			responseMsg.setWarnings(warnings);
			responseDto.setResponse(responseMsg);
			return Response.status(Status.BAD_REQUEST).entity(responseDto).build();
		}
		DataikuProjectDto existingDataikuProject = service.getByProjectName(projectName,cloudProfile);
		if(existingDataikuProject!=null && projectName.equalsIgnoreCase(existingDataikuProject.getProjectName())) {
			MessageDescription errMsg = new MessageDescription("Conflict. Project with name " + projectName + " already exists");
			log.error("Conflict. Project with name {} already exists with cloudprofile {} ", projectName, cloudProfile);
			errors.add(errMsg);
			responseMsg.setErrors(errors);
			responseMsg.setWarnings(warnings);
			responseDto.setResponse(responseMsg);
			return Response.status(Status.CONFLICT).entity(responseDto).build();
		}
		boolean isExisting = service.checkExistingProject(projectName, cloudProfile);
		if(isExisting) {
			MessageDescription errMsg = new MessageDescription("Conflict. Project with name " + projectName + " already exists");
			log.error("Conflict. Project with name {} already exists at {} ", projectName, cloudProfile);
			errors.add(errMsg);
			responseMsg.setErrors(errors);
			responseMsg.setWarnings(warnings);
			responseDto.setResponse(responseMsg);
			return Response.status(Status.CONFLICT).entity(responseDto).build();
		}
		requestedData.setProjectName(projectName.toUpperCase());
		responseDto = service.createProject(userId, requestedData,ownerDetails,collabPrivilegeDetails);
		return Response.ok().entity(responseDto).build();
		}catch(Exception e) {
			log.error("Failed at create dataiku project, unhandled exception {}", e.getMessage());
			return null;
		}
	}
	
	@Get(uri="/dataiku", produces= APPLICATION_JSON)
    @Operation(summary = "Api to get all dataiku project records from system",
            description = "Api to get all dataiku project records from system"
    )
    @ApiResponse(responseCode = "200", description = "Results found and returned",
            content = @Content(mediaType = "application/json"
            ,schema = @Schema(type="DataikuProjectSummaryCollectionDto"))
    )
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "500", description = "Failed with internal server error")
    @Tag(name = "dataiku")
    public Response getAll(
    		@Parameter(description = "limit for records result size",allowEmptyValue= true, required = false) @QueryParam("limit") int limit,
    		@Parameter(description = "offset for records result position",allowEmptyValue= true, required = false) @QueryParam("offset") int offset,
    		@Parameter(description = "sortBy, possible values projectName/createdOn",allowEmptyValue= true, required = false) @QueryParam("sortBy") String sortBy,
    		@Parameter(description = "sortOrder, possible values asc/desc",allowEmptyValue= true, required = false) @QueryParam("sortOrder") String sortOrder,
    		@Parameter(description = "searchTerm to filter by projectName",allowEmptyValue= true, required = false) @QueryParam("projectName") String projectName
    		) {
		String userId = this.userStore.getUserInfo().getId();
		DataikuProjectSummaryCollectionDto summaryCollectionDto = new DataikuProjectSummaryCollectionDto();
		DataikuProjectsCollectionDto response = service.getAllDataikuProjects(userId, offset, limit, sortBy, sortOrder, projectName);
		if(response!=null && response.getData()!= null && !response.getData().isEmpty()) {
			
			List<DataikuProjectDto> records = response.getData();
			List<DataikuProjectSummaryDto>  summaryRecords = new ArrayList<>();
			summaryRecords = records.stream().map(n -> assembler.toProjectDetails(n,userId)).collect(Collectors.toList());
			summaryCollectionDto.setData(summaryRecords);
			summaryCollectionDto.setTotalCount(response.getTotalCount());
			return Response.ok().entity(summaryCollectionDto).build();
		}
		else
			return Response.noContent().entity(summaryCollectionDto).build();
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
            @Parameter(description = "The id of the dataiku project to be updated", required = true) @PathParam("id") String id) {
		DataikuProjectResponseDto responseDto = new DataikuProjectResponseDto();
		responseDto.setData(null);
		GenericMessage responseMsg = new GenericMessage();
		responseMsg.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		DataikuProjectDto existingDataikuProject = service.getById(id);
		String userId = this.userStore.getUserInfo().getId();
		List<CollaboratorDetailsDto> collaborators = request.getData().getCollaborators();
		List<UserPrivilegeResponseDto> collabPrivilegeDetails = new ArrayList<>();
		if(existingDataikuProject!=null && id.equalsIgnoreCase(existingDataikuProject.getId())){
			List<CollaboratorDetailsDto> collabs = existingDataikuProject.getCollaborators();
			Optional<CollaboratorDetailsDto> record = collabs.stream().filter(x-> userId.equalsIgnoreCase(x.getUserId()) && "Administrator".equalsIgnoreCase(x.getPermission())).findAny();
	        if (!record.isPresent()) {
				MessageDescription errMsg = new MessageDescription("Forbidden, can only be updated by user with Administrator access to the project");
				log.error("Forbidden. Only user with Administrator access of the project {} can update details. Current user {} ", id, userId);
				errors.add(errMsg);
				responseMsg.setErrors(errors);
				responseMsg.setWarnings(warnings);
				return Response.status(Status.FORBIDDEN).entity(responseMsg).build();
			}
			
			if(collaborators!= null && !collaborators.isEmpty()) {
				for(CollaboratorDetailsDto collab : collaborators) {
					UserPrivilegeResponseDto collabDetails = userPrivilegeService.getByShortId(collab.getUserId());
					if(collabDetails==null || !collabDetails.getCanCreate()) {
						MessageDescription errMsg = new MessageDescription("Collaborator " + collab.getUserId() + " or privileges not found, cannot add to dataiku project");
						log.error("Collaborator {} or privileges not found, cannot update dataiku project", userId);
						errors.add(errMsg);
						responseMsg.setErrors(errors);
						responseMsg.setWarnings(warnings);
						responseDto.setResponse(responseMsg);
						return Response.status(Status.BAD_REQUEST).entity(responseDto).build();
					}else {
						collabPrivilegeDetails.add(collabDetails);
					}
				}
			}
			
		}else {
			MessageDescription errMsg = new MessageDescription(" Project with id " + id + " does not exists");
			log.error("Not Found. Project with id {} does not exists", id);
			errors.add(errMsg);
			responseMsg.setErrors(errors);
			responseMsg.setWarnings(warnings);
			return Response.status(Status.NOT_FOUND).entity(responseMsg).build();
		}
		responseDto = service.updateProject(id, request,collabPrivilegeDetails);
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
            @Parameter(description = "The id of the dataiku project to be deleted", required = true) @PathParam("id") String id) {
		GenericMessage responseMsg = new GenericMessage();
		responseMsg.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		DataikuProjectDto existingDataikuProject = service.getById(id);
		String userId = this.userStore.getUserInfo().getId();
		if(existingDataikuProject!=null && id.equalsIgnoreCase(existingDataikuProject.getId())){
			List<CollaboratorDetailsDto> collabs = existingDataikuProject.getCollaborators();
			Optional<CollaboratorDetailsDto> record = collabs.stream().filter(x-> userId.equalsIgnoreCase(x.getUserId()) && "Administrator".equalsIgnoreCase(x.getPermission())).findAny();
	        if (!record.isPresent()) {
				MessageDescription errMsg = new MessageDescription("Forbidden, Project can only be deleted by creator");
				log.error("Forbidden. Only Users with Administrator access can delete the project {}. Current user {} ", id, userId);
				errors.add(errMsg);
				responseMsg.setErrors(errors);
				responseMsg.setWarnings(warnings);
				return Response.status(Status.FORBIDDEN).entity(responseMsg).build();
			}
		}else {
			MessageDescription errMsg = new MessageDescription(" Project with id " + id + " does not exists");
			log.error("Not Found. Project with id {} does not exists", id);
			errors.add(errMsg);
			responseMsg.setErrors(errors);
			responseMsg.setWarnings(warnings);
			return Response.status(Status.NOT_FOUND).entity(responseMsg).build();
		}
		responseMsg = service.deleteById(id,existingDataikuProject);
		return Response.ok().entity(responseMsg).build();
	}
	
	
	@DELETE
    @Path("/dataiku/{cloudprofile}/{projectname}")
    @Operation(summary = "Delete dataiku project",
            description = "Hard delete dataiku project details from the system")
    @ApiResponse(responseCode = "200", description = "dataiku project deteled",
    		content = @Content(mediaType = "application/json"
            ,schema = @Schema(type="GenericMessage")))
    @ApiResponse(responseCode = "400", description = "Invalid id supplied")
    @ApiResponse(responseCode = "404", description = "User not found")
	@Tag(name = "dataiku")
    public Response deleteDataikuByCloudProfileAndProjectName(
            @Parameter(description = "The cloudProfile of the dataiku project to be deleted", required = true) @PathParam("cloudprofile") String cloudprofile, 
            @Parameter(description = "The name of the dataiku project to be deleted", required = true) @PathParam("projectname") String projectname) {
		GenericMessage responseMsg = new GenericMessage();
		responseMsg.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		cloudprofile = "eXtollo".equalsIgnoreCase(cloudprofile) ? cloudprofile : "onPremise";
		DataikuProjectDto existingDataikuProject = service.getByProjectName(projectname, cloudprofile);
		String userId = this.userStore.getUserInfo().getId();
		if(existingDataikuProject!=null && projectname.equalsIgnoreCase(existingDataikuProject.getProjectName())){
			List<CollaboratorDetailsDto> collabs = existingDataikuProject.getCollaborators();
			Optional<CollaboratorDetailsDto> record = collabs.stream().filter(x-> userId.equalsIgnoreCase(x.getUserId()) && "Administrator".equalsIgnoreCase(x.getPermission())).findAny();
	        if (!record.isPresent()) {
				MessageDescription errMsg = new MessageDescription("Forbidden, Project can only be deleted by creator");
				log.error("Forbidden. Only Users with Administrator access can delete the project {}. Current user {} ", projectname, userId);
				errors.add(errMsg);
				responseMsg.setErrors(errors);
				responseMsg.setWarnings(warnings);
				return Response.status(Status.FORBIDDEN).entity(responseMsg).build();
			}
		}else {
			MessageDescription errMsg = new MessageDescription(" Project with name " + projectname + " does not exists");
			log.error("Not Found. Project with name {} does not exists", projectname);
			errors.add(errMsg);
			responseMsg.setErrors(errors);
			responseMsg.setWarnings(warnings);
			return Response.status(Status.NOT_FOUND).entity(responseMsg).build();
		}
		String id = existingDataikuProject.getId();
		responseMsg = service.deleteById(id,existingDataikuProject);
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
    public Response fetchDataikuById(
            @Parameter(description = "The id of the dataiku project to be fetched", required = true) @PathParam("id") String id) {
		String userId = this.userStore.getUserInfo().getId();
		DataikuProjectDto data = service.getById(id);
		if(data!=null && id.equalsIgnoreCase(data.getId())) {
			CollaboratorDetailsDto collabUser = data.getCollaborators().stream().filter(collab -> userId.equalsIgnoreCase(collab.getUserId()))
					  .findAny().orElse(null);
			if(!(userId.equalsIgnoreCase(data.getCreatedBy()) || (collabUser!=null && userId.equalsIgnoreCase(collabUser.getUserId())))){
				return Response.status(Status.FORBIDDEN).entity(null).build();
			}
		}else {
			return Response.status(Status.NOT_FOUND).entity(null).build();
		}
		DataikuProjectResponseDto responseDto = new DataikuProjectResponseDto();
		responseDto.setData(data);
		return Response.ok().entity(responseDto).build();
	}
	
	@GET
    @Path("/dataiku/{cloudprofile}/{projectname}")
    @Operation(summary = "get dataiku project",
            description = "get dataiku project details from the system based on cloudprofile, projectname")
    @ApiResponse(responseCode = "200", description = "dataiku project fetched",
    		content = @Content(mediaType = "application/json"
            ,schema = @Schema(type="DataikuProjectResponseDto")))
    @ApiResponse(responseCode = "400", description = "Invalid id supplied")
    @ApiResponse(responseCode = "404", description = "User not found")
	@Tag(name = "dataiku")
    public Response fetchDataikuByProjectName(
    		@Parameter(description = "The cloudprofile of the dataiku details to be fetched", required = true) @PathParam("cloudprofile") String cloudprofile,
            @Parameter(description = "The projectname of the dataiku details to be fetched", required = true) @PathParam("projectname") String projectname) {
		String userId = this.userStore.getUserInfo().getId();
		cloudprofile = "eXtollo".equalsIgnoreCase(cloudprofile) ? cloudprofile : "onPremise";
		DataikuProjectDto data = service.getByProjectName(projectname, cloudprofile);
		if(data!=null && projectname.equalsIgnoreCase(data.getProjectName())) {
			CollaboratorDetailsDto collabUser = data.getCollaborators().stream().filter(collab -> userId.equalsIgnoreCase(collab.getUserId()))
					  .findAny().orElse(null);
			if(!(userId.equalsIgnoreCase(data.getCreatedBy()) || (collabUser!=null && userId.equalsIgnoreCase(collabUser.getUserId())))){
				return Response.status(Status.FORBIDDEN).entity(null).build();
			}
		}else {
			return Response.status(Status.NOT_FOUND).entity(null).build();
		}
		DataikuProjectResponseDto responseDto = new DataikuProjectResponseDto();
		responseDto.setData(data);
		return Response.ok().entity(responseDto).build();
	}
	
}

