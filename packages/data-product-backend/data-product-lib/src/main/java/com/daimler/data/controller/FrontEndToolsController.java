package com.daimler.data.controller;

import com.daimler.data.api.dataproductlov.FrontEndToolsApi;
import com.daimler.data.dto.dataproductlov.FrontEndToolsCollection;
import com.daimler.data.dto.dataproductlov.FrontEndToolsVO;
import com.daimler.data.service.dataproductlov.FrontEndToolsService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(value = "frontEndTools", tags = {"frontEndTools"})
@RequestMapping(value = "/api")
@RestController
public class FrontEndToolsController implements FrontEndToolsApi {

    @Autowired
    private FrontEndToolsService frontEndToolsService;

    private static Logger log = LoggerFactory.getLogger(FrontEndToolsController.class);

    @ApiOperation(value = "Get all available frontEndTools.", nickname = "getAll", notes = "Get all frontEndTools. This endpoints will be used to Get all valid available frontEndTools maintenance records.", response = FrontEndToolsCollection.class, tags = {"frontEndTools",})
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Returns message of succes or failure", response = FrontEndToolsCollection.class), @ApiResponse(code = 204, message = "Fetch complete, no content found."), @ApiResponse(code = 400, message = "Bad request."), @ApiResponse(code = 401, message = "Request does not have sufficient credentials."), @ApiResponse(code = 403, message = "Request is not authorized."), @ApiResponse(code = 405, message = "Method not allowed"), @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/frontEndTools", produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.GET)

    public ResponseEntity<FrontEndToolsCollection> getAll(@ApiParam(value = "Sort frontEndTools by a given variable like name", allowableValues = "name") @Valid @RequestParam(value = "sortBy", required = false) String sortBy, @ApiParam(value = "Sort frontEndTools based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder, @ApiParam(value = "page number from which listing of frontEndTools should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset, @ApiParam(value = "size to limit the number of frontEndTools, Example 10") @Valid @RequestParam(value = "limit", required = false) Integer limit) {

        try {
            int defaultLimit = 10;
            if (offset == null || offset < 0) offset = 0;
            if (limit == null || limit < 0) {
                limit = defaultLimit;
            }
            Long count = frontEndToolsService.getCount();
            List<FrontEndToolsVO> frontEndTools = frontEndToolsService.getFrontEndTools(offset, limit, sortOrder);
            FrontEndToolsCollection frontEndToolsCollection = new FrontEndToolsCollection();
            frontEndToolsCollection.setTotalCount(count.intValue());
            if (frontEndTools != null && frontEndTools.size() > 0) {
                frontEndToolsCollection.setData(frontEndTools);
                log.debug("Returning available frontEndTools");
                return new ResponseEntity<>(frontEndToolsCollection, HttpStatus.OK);
            } else {
                log.debug("No frontEndTools available, returning empty");
                return new ResponseEntity<>(frontEndToolsCollection, HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            log.error("Exception Occured: {}", e.getMessage());
            throw e;
        }


    }

    @ApiOperation(value = "get the frontEndTools  by given ID.", nickname = "getByID", notes = "get the frontEndTools  by given ID", response = FrontEndToolsCollection.class, tags = {"frontEndTools",})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Returns message of success or failure", response = FrontEndToolsCollection.class), @ApiResponse(code = 400, message = "Bad request"), @ApiResponse(code = 401, message = "Request does not have sufficient credentials."), @ApiResponse(code = 403, message = "Request is not authorized."), @ApiResponse(code = 404, message = "Invalid id, record not found."), @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/frontEndTools/{id}", produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.GET)
    public ResponseEntity<FrontEndToolsVO> getByID(@ApiParam(value = "Id of the front-end tools", required = true) @PathVariable("id") String id) {

        FrontEndToolsVO frontEndToolsVO = null;
        if (StringUtils.hasText(id)) {
            FrontEndToolsVO existingVO = frontEndToolsService.getById(id);
            if (existingVO != null && existingVO.getId() != null) {
                frontEndToolsVO = existingVO;
            }
        }
        if (frontEndToolsVO != null) {
            log.info("Front-End Tools {} fetched successfully", id);
            return new ResponseEntity<>(frontEndToolsVO, HttpStatus.OK);
        } else {
            log.info("No Front-End Tools {} found", id);
            return new ResponseEntity<>(new FrontEndToolsVO(), HttpStatus.NO_CONTENT);
        }
    }

}
