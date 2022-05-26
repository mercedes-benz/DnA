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

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.lov.LovApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.dto.lov.ClassificationVO;
import com.daimler.data.dto.lov.ClassificationVOCollection;
import com.daimler.data.service.lov.LovService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Dna Storage LOV API", tags = { "lov" })
@RequestMapping("/api")
@SuppressWarnings(value = "unused")
public class LOVController implements LovApi {

	private static Logger logger = LoggerFactory.getLogger(LOVController.class);

	@Autowired
	private LovService lovService;

	@Autowired
	private UserStore userStore;

	@Override
	@ApiOperation(value = "Get all classification types.", nickname = "getAllclassificationType", notes = "Get all classification types. This endpoints will be used to Get all valid available classification types.", response = ClassificationVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = ClassificationVOCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@GetMapping(path = "/classifications", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<ClassificationVOCollection> getAllclassificationType(@Valid String orderBy) {
		final List<ClassificationVO> classificationsVO = lovService.getAllClassificationType(orderBy);
		ClassificationVOCollection classificationVOCollection = new ClassificationVOCollection();
		if (!ObjectUtils.isEmpty(classificationsVO)) {
			classificationVOCollection.setData(classificationsVO);
			logger.info("Returning all available classification types");
			return new ResponseEntity<>(classificationVOCollection, HttpStatus.OK);
		} else {
			logger.debug("No classification types available, returning empty");
			return new ResponseEntity<>(classificationVOCollection, HttpStatus.NO_CONTENT);
		}
	}


}
