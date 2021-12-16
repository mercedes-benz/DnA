/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.controller;

import com.daimler.data.api.projectstatus.ProjectStatusesApi;
import com.daimler.data.dto.projectstatus.ProjectStatusCollection;
import com.daimler.data.dto.projectstatus.ProjectStatusVO;
import com.daimler.data.service.projectstatus.ProjectStatusService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(value = "ProjectStatus API", tags = {"projectstatuses"})
@RequestMapping("/api")
public class ProjectStatusController
        implements ProjectStatusesApi {


    @Autowired
    private ProjectStatusService projectStatusService;

    @Override
    @ApiOperation(value = "Get all available project-statuses.", nickname = "getAll", notes = "Get all project-statuses. This endpoints will be used to Get all valid available project-statuses maintenance records.", response = ProjectStatusCollection.class, tags={ "projectstatuses", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of succes or failure", response = ProjectStatusCollection.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/project-statuses",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.GET)
    public ResponseEntity<ProjectStatusCollection> getAll() {
        final List<ProjectStatusVO> projectStatusesVo = projectStatusService.getAll();
        ProjectStatusCollection projectstatusCollection = new ProjectStatusCollection();
        if (projectStatusesVo != null && projectStatusesVo.size() > 0) {
            projectstatusCollection.addAll(projectStatusesVo);
            return new ResponseEntity<>(projectstatusCollection, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(projectstatusCollection, HttpStatus.NO_CONTENT);
        }
    }


    @Override
    @ApiOperation(value = "Get available project-status for a given project-status id.", nickname = "getById", notes = "Get project-status. This endpoints will be used to Get valid available project-status for a given project-status-id maintenance records.", response = ProjectStatusVO.class, tags = {"projectstatuses",})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of success or failure", response = ProjectStatusVO.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
            @ApiResponse(code = 403, message = "Request is not authorized."),
            @ApiResponse(code = 405, message = "Method not allowed"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/project-statuses/{id}",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.GET)
    public ResponseEntity<ProjectStatusVO> getById(@ApiParam(value = "Id of the project-status for which details are to be fetched", required = true) @PathVariable("id") String id) {
        final ProjectStatusVO projectstatusVO = projectStatusService.getById(id);
        if (projectstatusVO != null)
            return new ResponseEntity<>(projectstatusVO, HttpStatus.OK);
        else
            return new ResponseEntity<>(projectstatusVO, HttpStatus.NO_CONTENT);
    }


}
