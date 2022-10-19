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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.classification.ClassificationsApi;
import com.daimler.data.dto.classification.ClassificationVOCollection;
import com.daimler.data.service.classification.ClassificationService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Classifications", tags = { "classifications" })
@RequestMapping("/api")
public class ClassificationController implements ClassificationsApi {

	@Autowired
	private ClassificationService classificationService;

	@Override
	@ApiOperation(value = "Get all classification types.", nickname = "getAll", notes = "Get all classification types. This endpoints will be used to Get all valid available classification types.", response = ClassificationVOCollection.class, tags = {
			"classifications", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = ClassificationVOCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/classifications", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ClassificationVOCollection> getAll() {
		return classificationService.getAllClassifications();
	}

}
