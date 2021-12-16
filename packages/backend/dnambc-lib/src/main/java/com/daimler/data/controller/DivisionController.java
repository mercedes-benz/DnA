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

import com.daimler.data.api.divisions.DivisionsApi;
import com.daimler.data.api.divisions.SubdivisionsApi;
import com.daimler.data.dto.divisions.DivisionCollection;
import com.daimler.data.dto.divisions.DivisionVO;
import com.daimler.data.dto.divisions.SubdivisionCollection;
import com.daimler.data.dto.divisions.SubdivisionVO;
import com.daimler.data.service.division.DivisionService;
import io.swagger.annotations.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

@RestController
@Api(value = "Division API", tags = { "divisions", "subdivisions" })
@RequestMapping("/api")
public class DivisionController implements DivisionsApi, SubdivisionsApi {

    private static Logger LOGGER = LoggerFactory.getLogger(DivisionController.class);

    @Autowired
    private DivisionService divisionService;

    @Override
    @ApiOperation(value = "Get all available divisions.", nickname = "getAll", notes = "Get all divisions. This endpoints will be used to Get all valid available divisions maintenance records.", response = DivisionCollection.class, tags = {
            "divisions", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully completed fetching all divisions", response = DivisionCollection.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/divisions", produces = { "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<DivisionCollection> getAll(
            @ApiParam(value = "Ids of the division for which sub-divisions are to be fetched") @Valid @RequestParam(value = "ids", required = false) List<String> ids) {
        LOGGER.trace("Processing getAll");
        try {
            DivisionCollection divisionCollection = new DivisionCollection();
            LOGGER.info("Fetching Divisions for given Ids:{}", ids);
            List<DivisionVO> divisions = divisionService.getDivisionsByIds(ids);
            if (!ObjectUtils.isEmpty(divisions)) {
                divisionCollection.addAll(divisions);
                return new ResponseEntity<>(divisionCollection, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(divisionCollection, HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            LOGGER.error("Exception Occured: {}", e.getMessage());
            throw e;
        }
    }

    @ApiOperation(value = "Get all available subdivisions for a given division id.", nickname = "getById", notes = "Get all subdivisions. This endpoints will be used to Get all valid available subdivisions for a given division-id maintenance records.", response = SubdivisionCollection.class, tags = {
            "subdivisions", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns message of succes or failure", response = SubdivisionCollection.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found"),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/subdivisions/{id}", produces = { "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<SubdivisionCollection> getById(
            @ApiParam(value = "Id of the division for which sub-divisions are tobe fetched", required = true) @PathVariable("id") String id) {
        final List<SubdivisionVO> subdivisions = divisionService.getSubDivisionsById(id);
        SubdivisionCollection subdivisionCollection = new SubdivisionCollection();
        if (subdivisions != null && subdivisions.size() > 0) {
            subdivisionCollection.addAll(subdivisions);
            return new ResponseEntity<>(subdivisionCollection, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(subdivisionCollection, HttpStatus.NO_CONTENT);
        }
    }

}
