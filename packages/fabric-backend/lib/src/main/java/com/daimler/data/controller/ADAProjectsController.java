package com.daimler.data.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.adaProjects.AdaProjectsApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVO;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsCollectionVO;
import com.daimler.data.dto.adaProjects.CreateADAProjectResponseVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(tags = "ADA Projects APIs")
@RequestMapping("/api/fabric-workspaces/")
public class ADAProjectsController implements AdaProjectsApi{
    

    @Override
    @ApiOperation(value = "Create a new ADA Project", nickname = "createADAProject", notes = "This can only be done by the logged in user.", response = CreateADAProjectResponseVO.class, tags={ "adaProjects", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "ADA Project Details created", response = CreateADAProjectResponseVO.class),
        @ApiResponse(code = 400, message = "Invalid input"),
        @ApiResponse(code = 404, message = "ADA Project Details not found") })
    @RequestMapping(value = "/ada/project",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<CreateADAProjectResponseVO> createADAProject(@ApiParam(value = "ADA Project object that needs to be created" ,required=true )  @Valid @RequestBody ADAProjectDetailsVO body){

        return new ResponseEntity<>(new CreateADAProjectResponseVO(), HttpStatus.CREATED);
    }


    @Override
    @ApiOperation(value = "Delete an ADA Project", nickname = "deleteADAProject", notes = "Delete an ADA Project by Id", response = GenericMessage.class, tags={ "adaProjects", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "ADA Project deleted successfully", response = GenericMessage.class),
        @ApiResponse(code = 404, message = "ADA Project not found") })
    @RequestMapping(value = "/ada/project/{projectId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> deleteADAProject(@ApiParam(value = "ID of ADA Project to delete",required=true) @PathVariable("projectId") String projectId) {

        return new ResponseEntity<>(new GenericMessage("ADA Project deleted successfully"), HttpStatus.OK);
    }


    @Override
    @ApiOperation(value = "Get ADA Project by ID", nickname = "getADAProjectById", notes = "Returns a single ADA Project", response = ADAProjectDetailsVO.class, tags={ "adaProjects", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = ADAProjectDetailsVO.class),
        @ApiResponse(code = 404, message = "ADA Project not found") })
    @RequestMapping(value = "/ada/project/{projectId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<ADAProjectDetailsVO> getADAProjectById(@ApiParam(value = "ID of ADA Project to return",required=true) @PathVariable("projectId") String projectId) {

        return new ResponseEntity<>(new ADAProjectDetailsVO(), HttpStatus.OK);
    }


    @Override
    @ApiOperation(value = "Get all ADA Projects", nickname = "getAllADAProjects", notes = "Get all ADA Projects with pagination and sorting options", response = ADAProjectDetailsCollectionVO.class, tags={ "adaProjects", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns paginated list of ADA Projects", response = ADAProjectDetailsCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found"),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials"),
        @ApiResponse(code = 403, message = "Request is not authorized"),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/ada/projects",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<ADAProjectDetailsCollectionVO> getAllADAProjects(@ApiParam(value = "Page number from which listing of ADA Projects should start. Example: 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,@ApiParam(value = "Page size to limit the number of ADA Projects. Example: 15") @Valid @RequestParam(value = "limit", required = false) Integer limit) {

        return new ResponseEntity<>(new ADAProjectDetailsCollectionVO(), HttpStatus.OK);
    }

    @Override
    @ApiOperation(value = "Update an existing ADA Project", nickname = "updateADAProject", notes = "Update an existing ADA Project by Id", response = CreateADAProjectResponseVO.class, tags={ "adaProjects", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "ADA Project updated successfully", response = CreateADAProjectResponseVO.class),
        @ApiResponse(code = 400, message = "Invalid input"),
        @ApiResponse(code = 404, message = "ADA Project not found") })
    @RequestMapping(value = "/ada/project/{projectId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<CreateADAProjectResponseVO> updateADAProject(@ApiParam(value = "ID of ADA Project to update",required=true) @PathVariable("projectId") String projectId,@ApiParam(value = "ADA Project object with updated values" ,required=true )  @Valid @RequestBody ADAProjectDetailsVO body) {

        return new ResponseEntity<>(new CreateADAProjectResponseVO(), HttpStatus.OK);
    }
}
