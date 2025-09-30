package com.daimler.data.controller;

import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import com.daimler.data.api.workspace.buildDeploy.CodeServerBuildDeployServiceApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
import com.daimler.data.db.json.BuildAudit;
import com.daimler.data.db.json.DeploymentAudit;
import com.daimler.data.db.repo.workspace.WorkSpaceCodeServerBuildDeployRepository;
import com.daimler.data.db.repo.workspace.WorkspaceCustomBuildDeployRepo;

import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import com.daimler.data.dto.workspace.buildDeploy.ManageBuildRequestDto;
import com.daimler.data.service.workspace.WorkspaceService;
import com.daimler.data.util.ConstantsUtility;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.CreatedByVO;
import com.daimler.data.dto.workspace.InitializeWorkspaceResponseVO;
import com.daimler.data.dto.workspace.buildDeploy.BuildAuditVO;
import com.daimler.data.dto.workspace.buildDeploy.DeploymentAuditVO;
import com.daimler.data.dto.workspace.buildDeploy.LogsListResponseVO;
import com.daimler.data.dto.workspace.buildDeploy.CodeServerBuildDeployVO;
import com.daimler.data.dto.workspace.buildDeploy.VersionListResponseVO;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.BuildDeployAssembler;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@Api(value = "Recipe API", tags = { "code-server-build-deploy-service" })
@RequestMapping("/api")
@Slf4j
public class BuildDeployController implements CodeServerBuildDeployServiceApi {

    @Autowired
    private WorkspaceService service;

    @Autowired
    private UserStore userStore;

    @Autowired
	 private WorkSpaceCodeServerBuildDeployRepository buildDeployRepo;

     @Autowired
	 private BuildDeployAssembler buildDeployAssembler;

     @Autowired
	 private WorkspaceCustomBuildDeployRepo buildDeployCustomRepo;

    @Override
    @ApiOperation(value = "Build workspace Project for a given Id.", nickname = "buildWorkspaceProject", notes = "build workspace Project for a given identifier.", response = GenericMessage.class, tags = {
            "code-server-build-deploy-service", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/{id}/build", produces = { "application/json" }, consumes = {
            "application/json" }, method = RequestMethod.POST)
    public ResponseEntity<GenericMessage> buildWorkspaceProject(
            @ApiParam(value = "Workspace ID to be fetched", required = true) @PathVariable("id") String id,
            @ApiParam(value = "request body for the project to be built", required = true) @Valid @RequestBody ManageBuildRequestDto buildRequestDto) {
       try{
                boolean isPrivateRecipe = false;
        CreatedByVO currentUser = this.userStore.getVO();
        String userId = currentUser != null ? currentUser.getId() : "";
        CodeServerWorkspaceVO vo = service.getById(userId, id);
        CodeServerWorkspaceVO ownerVo = null;
        if (vo == null || vo.getWorkspaceId() == null) {
            log.debug("No workspace found, returning empty");
            GenericMessage emptyResponse = new GenericMessage();
            List<MessageDescription> errors = new ArrayList<>();
            MessageDescription msg = new MessageDescription();
            msg.setMessage("No workspace found for given id and the user");
            errors.add(msg);
            emptyResponse.setErrors(errors);
            return new ResponseEntity<>(emptyResponse, HttpStatus.NOT_FOUND);
        }
        if (!vo.getProjectDetails().getProjectOwner().getId().equals(vo.getWorkspaceOwner().getId())) {
            ownerVo = service.getByProjectName(vo.getProjectDetails().getProjectOwner().getId(),
                    vo.getProjectDetails().getProjectName());
        } else {
            ownerVo = vo;
        }

        List<String> authorizedUsers = new ArrayList<>();
        if (vo.getProjectDetails() != null && vo.getProjectDetails().getProjectOwner() != null) {
            String owner = vo.getProjectDetails().getProjectOwner().getId();
            authorizedUsers.add(owner);
        }
        if (vo.getProjectDetails().getProjectCollaborators() != null
                && !vo.getProjectDetails().getProjectCollaborators().isEmpty()) {
            List<String> collabUsers = vo.getProjectDetails().getProjectCollaborators().stream().map(n -> n.getId())
                    .collect(Collectors.toList());
            authorizedUsers.addAll(collabUsers);
        }
        if (!authorizedUsers.contains(userId)) {
            MessageDescription notAuthorizedMsg = new MessageDescription();
            notAuthorizedMsg.setMessage(
                    "Not authorized to build project for workspace. User does not have privileges.");
            GenericMessage errorMessage = new GenericMessage();
            errorMessage.addErrors(notAuthorizedMsg);
            log.info("User {} cannot build project for workspace {}, insufficient privileges.", userId,
                    vo.getWorkspaceId());
            return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
        }
        if (vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase().startsWith("public")
                || vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().equalsIgnoreCase("default")) {
            MessageDescription invalidTypeMsg = new MessageDescription();
            invalidTypeMsg.setMessage(
                    "Invalid type, cannot build this type of recipe");
            GenericMessage errorMessage = new GenericMessage();
            errorMessage.addErrors(invalidTypeMsg);
            log.info("User {} cannot build project of recipe {} for workspace {}, invalid type.", userId,
                    vo.getProjectDetails().getRecipeDetails().getRecipeId().name(), vo.getWorkspaceId());
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
        if (vo.getProjectDetails().getRecipeDetails().getRecipeId().toString().toLowerCase().startsWith("private")) {
            isPrivateRecipe = true;
            // buildRequestDto.setRepo(vo.getProjectDetails().getRecipeDetails().getRepodetails());
        }
        String environment = "int";
        String branch = "main";
        if (buildRequestDto != null && !"int".equalsIgnoreCase(buildRequestDto.getEnvironment())) {
            environment = "prod";
        }
        if (buildRequestDto != null && buildRequestDto.getBranch() != null) {
            branch = buildRequestDto.getBranch();
        }
        String intDeployStatus = "";
		String prodDeployStatus = "";
		String intBuildStatus = "";
		String prodBuildStatus = "";
        intBuildStatus = vo.getProjectDetails().getIntBuildDetails().getLastBuildStatus();
        prodBuildStatus = vo.getProjectDetails().getProdBuildDetails().getLastBuildStatus();
        intDeployStatus = vo.getProjectDetails().getIntDeploymentDetails().getLastDeploymentStatus();
		prodDeployStatus = vo.getProjectDetails().getProdDeploymentDetails().getLastDeploymentStatus();

           if (intBuildStatus != null && intBuildStatus.equalsIgnoreCase("BUILD_REQUESTED")) {
               MessageDescription invalidTypeMsg = new MessageDescription();
               invalidTypeMsg.setMessage(
                       "cannot build workspace since it is already in BUILD_REQUESTED state");
               GenericMessage errorMessage = new GenericMessage();
               errorMessage.addErrors(invalidTypeMsg);
               log.info("User {} cannot build project of recipe {} for workspace {}, since it is alredy in BUILD_REQUESTED state.", userId,
                       vo.getProjectDetails().getRecipeDetails().getRecipeId().name(), vo.getWorkspaceId());
               return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
           }else if (intDeployStatus != null && (intDeployStatus.equalsIgnoreCase("DEPLOY_REQUESTED") || intDeployStatus.equalsIgnoreCase("APPROVAL_PENDING"))) {
            MessageDescription invalidTypeMsg = new MessageDescription();
            invalidTypeMsg.setMessage(
                    "cannot deploy workspace since it is already in "+intDeployStatus+" state");
            GenericMessage errorMessage = new GenericMessage();
            errorMessage.addErrors(invalidTypeMsg);
            log.info("User {} cannot deploy project of recipe {} for workspace {}, since it is alredy in {} state.", userId,
                    vo.getProjectDetails().getRecipeDetails().getRecipeId().name(), vo.getWorkspaceId(),intDeployStatus);
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
           if (prodBuildStatus != null && prodBuildStatus.equalsIgnoreCase("BUILD_REQUESTED") ) {
               MessageDescription invalidTypeMsg = new MessageDescription();
               invalidTypeMsg.setMessage(
                       "cannot build workspace since it is already in BUILD_REQUESTED state");
               GenericMessage errorMessage = new GenericMessage();
               errorMessage.addErrors(invalidTypeMsg);
               log.info("User {} cannot build project of recipe {} for workspace {}, since it is alredy in BUILD_REQUESTED state.", userId,
                       vo.getProjectDetails().getRecipeDetails().getRecipeId().name(), vo.getWorkspaceId());
               return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
           }else if (prodDeployStatus != null && (prodDeployStatus.equalsIgnoreCase("DEPLOY_REQUESTED") || prodDeployStatus.equalsIgnoreCase("APPROVAL_PENDING"))) {
            MessageDescription invalidTypeMsg = new MessageDescription();
            invalidTypeMsg.setMessage(
                    "cannot deploy workspace since it is already in "+prodDeployStatus+" state");
            GenericMessage errorMessage = new GenericMessage();
            errorMessage.addErrors(invalidTypeMsg);
            log.info("User {} cannot deploy project of recipe {} for workspace {}, since it is alredy in {} state.", userId,
                    vo.getProjectDetails().getRecipeDetails().getRecipeId().name(), vo.getWorkspaceId(),prodDeployStatus);
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
        
        String projectName = vo.getProjectDetails() != null ? vo.getProjectDetails().getProjectName() : null;
        List<BuildAudit> auditLogs = new ArrayList<>();
        CodeServerBuildDeployNsql optionalBuildDeployEntity = buildDeployCustomRepo.findByProjectName(projectName);
        log.info("optionalBuildDeployEntity for project {}: {}", projectName, optionalBuildDeployEntity);
        if (optionalBuildDeployEntity != null && optionalBuildDeployEntity.getData() != null) {
             log.info("Int Build Audit Logs for project {}: {}", projectName, optionalBuildDeployEntity.getData().getIntBuildAuditLogs());
             log.info("Prod Build Audit Logs for project {}: {}", projectName, optionalBuildDeployEntity.getData().getProdBuildAuditLogs());
            if ("int".equalsIgnoreCase(environment)
                    && optionalBuildDeployEntity.getData().getIntBuildAuditLogs() != null) {
                auditLogs = optionalBuildDeployEntity.getData().getIntBuildAuditLogs();
            } else if ("prod".equalsIgnoreCase(environment)
                    && optionalBuildDeployEntity.getData().getProdBuildAuditLogs() != null) {
                auditLogs = optionalBuildDeployEntity.getData().getProdBuildAuditLogs();
            }
        }
        if (auditLogs == null) {
            auditLogs = new ArrayList<>();
        }
        log.info("Workspace ID: {}", vo.getWorkspaceId());
        log.info("Environment: {}", environment);
        log.info("Audit logs size: {}", auditLogs != null ? auditLogs.size() : 0);
        long retainedCount = auditLogs.stream()
                .filter(b -> "BUILD_SUCCESS".equalsIgnoreCase(b.getBuildStatus()))
                .filter(b -> !b.isImageDeleted())
                .count();
        log.info("Retained successful builds (not deleted): {}", retainedCount);
        log.info("Audit logs size: {}", auditLogs.size());
        
        if (retainedCount >= 10) {
             log.info("retained cound>10");
            MessageDescription invalidMsg = new MessageDescription();
            invalidMsg.setMessage("Build not allowed: There are already " + retainedCount +
                    " successful builds with images retained. Please delete older images before triggering a new build.");
            GenericMessage errorMessage = new GenericMessage();
            errorMessage.addErrors(invalidMsg);
            log.info("User {} attempted to build workspace {} but retained image limit reached ({} builds).",
                    userId, vo != null ? vo.getWorkspaceId() : "UNKNOWN", retainedCount);
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
         
        String lastBuildType = "build";
         log.info("calling build service:");
        GenericMessage responseMsg = service.buildWorkSpace(userId,id,branch,buildRequestDto,isPrivateRecipe,environment,lastBuildType);
				 log.info("User {} build workspace {} project {}", userId, vo.getWorkspaceId(),
						 vo.getProjectDetails().getRecipeDetails().getRecipeId().name());
			if("FAILED".equalsIgnoreCase(responseMsg.getSuccess())){
				return new ResponseEntity<>(responseMsg, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			 return new ResponseEntity<>(responseMsg, HttpStatus.OK);
    } catch (Exception e) {
        log.error("Failed to build workspace {}, with exception {}", id, e.getLocalizedMessage());
        MessageDescription exceptionMsg = new MessageDescription("Failed to build due to internal error.");
        GenericMessage errorMessage = new GenericMessage();
        errorMessage.addErrors(exceptionMsg);
        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }   
    }

    @Override
    @ApiOperation(value = "Get build logs for a given project name.", nickname = "getBuildLogsByProjectName", notes = "Get build logs for a given project name.", response = LogsListResponseVO.class, tags={ "code-server-build-deploy-service", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = LogsListResponseVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspace/logs/{projectName}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<LogsListResponseVO> getBuildLogsByProjectName(@ApiParam(value = "Workspace projectName to be fetched",required=true) @PathVariable("projectName") String projectName) {
        List<MessageDescription> errors = new ArrayList<>();
        LogsListResponseVO response = new LogsListResponseVO();
        List<MessageDescription> warnings = new ArrayList<>();
        response.setData(null);
        response.setErrors(errors);
        response.setWarnings(warnings);
        response.setSuccess("FAILED");
        CodeServerBuildDeployVO data = null;
        try {
            CodeServerBuildDeployNsql optionalBuildDeployentity =  buildDeployCustomRepo.findByProjectName(projectName);	
					if(optionalBuildDeployentity != null){
                        data = buildDeployAssembler.toVo(optionalBuildDeployentity);
                    }else{
                        MessageDescription msg = new MessageDescription();
                        msg.setMessage("No build logs found for given project name");
                        warnings.add(msg);
				        response.setWarnings(warnings);                        
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }
            if(null != data){
                response.setData(data);
                response.setSuccess("SUCCESS");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }else{
                MessageDescription exceptionMsg = new MessageDescription("Failed to get logs due to internal error.");
                errors.add(exceptionMsg);
				response.setErrors(errors);
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            log.error("Failed to get logs with exception {}", e.getLocalizedMessage());
        MessageDescription exceptionMsg = new MessageDescription("Failed to build due to internal error.");
        errors.add(exceptionMsg);
				response.setErrors(errors);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @ApiOperation(value = "Get build version for a given project name.", nickname = "getBuildVersionByProjectName", notes = "Get build version for a given project name.", response = VersionListResponseVO.class, tags={ "code-server-build-deploy-service", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = VersionListResponseVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspace/buildVersion/{projectName}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<VersionListResponseVO> getBuildVersionByProjectName(@ApiParam(value = "Workspace projectName to be fetched",required=true) @PathVariable("projectName") String projectName){
        List<MessageDescription> errors = new ArrayList<>();
        VersionListResponseVO response = new VersionListResponseVO();
        List<MessageDescription> warnings = new ArrayList<>();
        response.setIntBuildVersions(null);
        response.setProdBuildVersions(null);
        response.setErrors(errors);
        response.setWarnings(warnings);
        response.setSuccess("FAILED");
        try {

            CodeServerBuildDeployNsql optionalBuildDeployentity =  buildDeployCustomRepo.findByProjectName(projectName);	
					if(optionalBuildDeployentity != null){
                        response = service.getBuildVersion(projectName);
                    }else{
                        MessageDescription msg = new MessageDescription();
                        msg.setMessage("No build version found for given project name");
                        warnings.add(msg);
				        response.setWarnings(warnings);                        
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }
            if(null != response){
                response.setSuccess("SUCCESS");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }else{
                MessageDescription exceptionMsg = new MessageDescription("Failed to get logs due to internal error.");
                errors.add(exceptionMsg);
				response.setErrors(errors);
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            
        } catch (Exception e) {
            log.error("Failed to get logs with exception {}", e.getLocalizedMessage());
        MessageDescription exceptionMsg = new MessageDescription("Failed to build due to internal error.");
        errors.add(exceptionMsg);
				response.setErrors(errors);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @ApiOperation(value = "delete build version from workspace projectt.", nickname = "deleteBuild", notes = "delete build version from workspace projectt", response = GenericMessage.class, tags={ "code-server-build-deploy-service", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/workspaces/{projectName}/build/{version}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> deleteBuild(@ApiParam(value = "Workspace Project to be fetched",required=true) @PathVariable("projectName") String projectName,@ApiParam(value = "version to be deleted",required=true) @PathVariable("version") String version){
          List<MessageDescription> errors = new ArrayList<>();
           List<MessageDescription> warnings = new ArrayList<>();
          GenericMessage response = new GenericMessage();
          response.setSuccess("FAILED");
        try {
            CodeServerBuildDeployNsql optionalBuildDeployentity =  buildDeployCustomRepo.findByProjectName(projectName);	
					if(optionalBuildDeployentity != null ){
                         List<BuildAudit> builds = new ArrayList<>();
                         List<DeploymentAudit> deploymentAuditLogs = new ArrayList<>();
                         String env = "int";
                        if(version.startsWith("int")){
                            builds = optionalBuildDeployentity.getData().getIntBuildAuditLogs();
                            deploymentAuditLogs = optionalBuildDeployentity.getData().getIntDeploymentAuditLogs();
                            // deploymentAuditLogs.
                        }else if(version.startsWith("prod")){
                            env = "prod";
                             builds = optionalBuildDeployentity.getData().getProdBuildAuditLogs();
                             deploymentAuditLogs = optionalBuildDeployentity.getData().getProdDeploymentAuditLogs();
                        }
                        

                        if(!deploymentAuditLogs.isEmpty()){
                            List<DeploymentAudit> sortedList = deploymentAuditLogs.stream().filter(i -> i.getDeploymentStatus().equalsIgnoreCase("DEPLOYED"))
                        .sorted(Comparator.comparing(DeploymentAudit::getDeployedOn).reversed())
                        .collect(Collectors.toList());
                        if(!sortedList.isEmpty() && sortedList.get(0).getVersion().equalsIgnoreCase(version)){
                            MessageDescription msg = new MessageDescription();
                            msg.setMessage("Given version "+version+" is currently deployed,Please deploy with different build and try deleting later");
                            warnings.add(msg);
				            response.setWarnings(warnings);                        
                            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                        }  
                            

                        }

                        if(builds.stream().anyMatch( i -> (i.getVersion().equalsIgnoreCase(version) && !i.isImageDeleted()))){
                            response = service.deleteBuild(projectName,version);
                        }else{
                            MessageDescription msg = new MessageDescription();
                            msg.setMessage("No build version found or build version is already deleted!! for given project name");
                            warnings.add(msg);
				            response.setWarnings(warnings);                        
                            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                        }
                        
                    }else{
                        MessageDescription msg = new MessageDescription();
                        msg.setMessage("No Project found with gven name");
                        warnings.add(msg);
				        response.setWarnings(warnings);                        
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
            if(null != response){
                response.setSuccess("SUCCESS");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }else{
                MessageDescription exceptionMsg = new MessageDescription("Failed to get logs due to internal error.");
                errors.add(exceptionMsg);
				response.setErrors(errors);
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            } 
        } catch (Exception e) {
            log.error("Failed to get logs with exception {}", e.getLocalizedMessage());
            MessageDescription exceptionMsg = new MessageDescription("Failed to build due to internal error.");
            errors.add(exceptionMsg);
			response.setErrors(errors);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
