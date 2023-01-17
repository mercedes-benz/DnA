package com.daimler.data.controller;

import com.daimler.data.api.dataproductlov.PlatformsApi;
import com.daimler.data.dto.dataproductlov.PlatformCollection;
import com.daimler.data.dto.dataproductlov.PlatformVO;
import com.daimler.data.service.dataproductlov.PlatformService;
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

@Api(value = "platforms", tags = {"platforms"})
@RequestMapping(value = "/api")
@RestController
public class PlatformController implements PlatformsApi {

    @Autowired
    private PlatformService platformService;

    private static Logger log = LoggerFactory.getLogger(PlatformController.class);

    @ApiOperation(value = "Get all available platforms.", nickname = "getAll", notes = "Get all platforms. This endpoints will be used to Get all valid available platforms maintenance records.", response = PlatformCollection.class, tags = {"platforms",})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = PlatformCollection.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."), @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/platforms",
    produces = {"application/json"},
    consumes = {"application/json"},
    method = RequestMethod.GET)
    public ResponseEntity<PlatformCollection> getAll(@ApiParam(value = "Sort platforms by a given variable like name", allowableValues = "name") @Valid @RequestParam(value = "sortBy", required = false) String sortBy, @ApiParam(value = "Sort platforms based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder, @ApiParam(value = "page number from which listing of platforms should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset, @ApiParam(value = "size to limit the number of platforms, Example 10") @Valid @RequestParam(value = "limit", required = false) Integer limit) {

        try {
            int defaultLimit = 10;
            if (offset == null || offset < 0) offset = 0;
            if (limit == null || limit < 0) {
                limit = defaultLimit;
            }
            Long count = platformService.getCount();
            List<PlatformVO> platforms = platformService.getPlatforms(offset, limit, sortOrder);
            PlatformCollection platformCollection = new PlatformCollection();
            platformCollection.setTotalCount(count.intValue());
            if (platforms != null && platforms.size() > 0) {
                platformCollection.setData(platforms);
                log.debug("Returning available platforms");
                return new ResponseEntity<>(platformCollection, HttpStatus.OK);
            } else {
                log.debug("No platforms available, returning empty");
                return new ResponseEntity<>(platformCollection, HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            log.error("Exception Occured: {}", e.getMessage());
            throw e;
        }


    }

    @ApiOperation(value = "get the platforms  by given ID.", nickname = "getByID", notes = "get the platforms  by given ID", response = PlatformCollection.class, tags = {"platforms",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = PlatformCollection.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "Invalid id, record not found."),
        @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/platforms/{id}",
    produces = {"application/json"},
    consumes = {"application/json"},
    method = RequestMethod.GET)
    public ResponseEntity<PlatformVO> getByID(@ApiParam(value = "Id of the platform", required = true) @PathVariable("id") String id) {

        PlatformVO platformVO = null;
        if (StringUtils.hasText(id)) {
            PlatformVO existingVO = platformService.getById(id);
            if (existingVO != null && existingVO.getId() != null) {
                platformVO = existingVO;
            }
        }
        if (platformVO != null) {
            log.info("Platform {} fetched successfully", id);
            return new ResponseEntity<>(platformVO, HttpStatus.OK);
        } else {
            log.info("No Platform {} found", id);
            return new ResponseEntity<>(new PlatformVO(), HttpStatus.NO_CONTENT);
        }
    }

}
