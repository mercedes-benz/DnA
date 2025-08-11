package com.daimler.data.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.cdcPush.CdcPushApi;
import com.daimler.data.dto.cdcPush.LakehouseTableCollectionVO;
import com.daimler.data.dto.cdcPush.TableSchemaResponseVO;
import com.daimler.data.service.cdc.CdcPushService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import javax.validation.constraints.NotNull;

@RestController
@Api(value = "Forecast APIs")
@RequestMapping("/api")
@Slf4j
public class CdcPushController implements CdcPushApi {

    @Autowired
	private CdcPushService service;

    @Override
    @ApiOperation(value = "Get tables for a given lakehouse", nickname = "getLakehouseTables", notes = "Get all tables for a given Fabric lakehouse under a workspace.", response = LakehouseTableCollectionVO.class, tags={ "cdc-push", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of tables in the lakehouse", response = LakehouseTableCollectionVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, but no tables found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have valid credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed."),
        @ApiResponse(code = 500, message = "Internal server error.") })
    @RequestMapping(value = "/cdc-push/tables",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<LakehouseTableCollectionVO> getLakehouseTables(@NotNull @ApiParam(value = "", required = true) @Valid @RequestParam(value = "workspaceId", required = true) String workspaceId,@NotNull @ApiParam(value = "", required = true) @Valid @RequestParam(value = "lakehouseId", required = true) String lakehouseId){
        LakehouseTableCollectionVO response = service.getLakehouseTables(workspaceId, lakehouseId);
        if (response != null && response.getRecords() != null && !response.getRecords().isEmpty()) {
            return new ResponseEntity<>(response, HttpStatus.OK); 
        }

        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT); 
    }


    @Override
    @ApiOperation(value = "Get table schema", nickname = "getTableSchema", notes = "Get column schema for a table inside a Fabric lakehouse.", response = TableSchemaResponseVO.class, tags={ "cdc-push", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully fetched table schema.", response = TableSchemaResponseVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, but no schema found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have valid credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed."),
        @ApiResponse(code = 500, message = "Internal server error.") })
    @RequestMapping(value = "/cdc-push/tables/schema",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<TableSchemaResponseVO> getTableSchema(@NotNull @ApiParam(value = "", required = true) @Valid @RequestParam(value = "workspaceId", required = true) String workspaceId,@NotNull @ApiParam(value = "", required = true) @Valid @RequestParam(value = "lakehouseId", required = true) String lakehouseId,@NotNull @ApiParam(value = "", required = true) @Valid @RequestParam(value = "tableName", required = true) String tableName,@ApiParam(value = "") @Valid @RequestParam(value = "schemaName", required = false) String schemaName){
        TableSchemaResponseVO responseVO = service.getTableSchema(workspaceId, lakehouseId, tableName, schemaName);

        if (responseVO != null && responseVO.getData() != null &&
                responseVO.getData().getColumns() != null && !responseVO.getData().getColumns().isEmpty()) {
            return ResponseEntity.ok(responseVO);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
    }
}
