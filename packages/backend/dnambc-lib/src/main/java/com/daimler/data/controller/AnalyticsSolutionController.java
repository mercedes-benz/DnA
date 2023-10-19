package com.daimler.data.controller;

import com.daimler.data.api.analyticsSolution.AnalyticsSolutionsApi;
import com.daimler.data.dto.analyticsSolution.AnalyticsSolutionCollection;
import com.daimler.data.dto.analyticsSolution.AnalyticsSolutionVO;
import com.daimler.data.service.analyticsSolution.AnalyticsSolutionService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;

@RestController
@Api(value = "Analytics Solution API", tags = { "analyticsSolutions" })
@RequestMapping("/api")
@Slf4j
public class AnalyticsSolutionController implements AnalyticsSolutionsApi {
    @Autowired
    private AnalyticsSolutionService analyticsSolutionService;
    @Override
    @ApiOperation(value = "Get all available analytics solutions.", nickname = "getAll", notes = "Get all analytics solutions. This endpoints will be used to Get all valid available analytics solutions maintenance records.", response = AnalyticsSolutionCollection.class, tags={ "analyticsSolutions", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure", response = AnalyticsSolutionCollection.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/analyticsSolutions",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.GET)
    public ResponseEntity<AnalyticsSolutionCollection> getAll(@ApiParam(value = "Sort analytics solutions based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder){
        final List<AnalyticsSolutionVO> analyticsSolutions = analyticsSolutionService.getAll();
        AnalyticsSolutionCollection analyticsSolutionCollection = new AnalyticsSolutionCollection();
        if (analyticsSolutions != null && analyticsSolutions.size() > 0) {
            if (sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
                analyticsSolutions.sort(Comparator.comparing(AnalyticsSolutionVO :: getName, String.CASE_INSENSITIVE_ORDER));
            }
            if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
                analyticsSolutions.sort(Comparator.comparing(AnalyticsSolutionVO :: getName, String.CASE_INSENSITIVE_ORDER).reversed());
            }
            analyticsSolutionCollection.addAll(analyticsSolutions);
            log.debug("Returning all available analytics solutions");
            return new ResponseEntity<>(analyticsSolutionCollection, HttpStatus.OK);
        } else {
            log.debug("No languages found, returning empty");
            return new ResponseEntity<>(analyticsSolutionCollection, HttpStatus.NO_CONTENT);
        }

    }

}
