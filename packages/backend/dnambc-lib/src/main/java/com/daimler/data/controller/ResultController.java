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

import com.daimler.data.api.result.ResultsApi;
import com.daimler.data.assembler.ResultAssembler;
import com.daimler.data.dto.result.ResultCollection;
import com.daimler.data.dto.result.ResultVO;
import com.daimler.data.service.result.ResultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Api(value = "Result API",tags = {"results"})
@RequestMapping("/api")
public class ResultController
        implements ResultsApi {


    @Autowired
    private ResultService resultService;
    
    @Autowired
    private ResultAssembler resultAssembler;

    @Override
    @ApiOperation(value = "Get all available results.", nickname = "getAll", notes = "Get all results. This endpoints will be used to Get all valid available results maintenance records.", response = ResultCollection.class, tags = {"results",})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully completed fetching all results", response = ResultCollection.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found"),
            @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/results",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<ResultCollection> getAll() {
        final List<ResultVO> resultsVo = resultService.getAll();
        ResultCollection resultCollection = new ResultCollection();
        if (resultsVo != null && resultsVo.size() > 0) {
        	List<ResultVO> filteredResultsVO = resultAssembler.filterOldResults(resultsVo);
        	resultCollection.addAll(filteredResultsVO);
            return new ResponseEntity<>(resultCollection, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(resultCollection, HttpStatus.NO_CONTENT);
        }
    }


}
