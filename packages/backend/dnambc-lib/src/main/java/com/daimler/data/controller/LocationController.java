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

import com.daimler.data.api.location.LocationsApi;
import com.daimler.data.dto.location.LocationCollection;
import com.daimler.data.dto.location.LocationVO;
import com.daimler.data.service.location.LocationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(value = "Location API", tags = { "locations" })
@RequestMapping("/api")
@Slf4j
public class LocationController implements LocationsApi {

	@Autowired
	private LocationService locationServices;

	@Override
	@ApiOperation(value = "Get all available locations.", nickname = "getAll", notes = "Get all locations. This endpoints will be used to Get all valid available locations maintenance records.", response = LocationCollection.class, tags = {
			"locations", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = LocationCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/locations", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<LocationCollection> getAll() {
		final List<LocationVO> locations = locationServices.getAllSortedByUniqueLiteralAsc("name");
		LocationCollection locationCollection = new LocationCollection();
		if (locations != null && locations.size() > 0) {
			locationCollection.addAll(locations);
			log.debug("Returning all available locations");
			return new ResponseEntity<>(locationCollection, HttpStatus.OK);
		} else {
			log.debug("No locations found, returning empty");
			return new ResponseEntity<>(locationCollection, HttpStatus.NO_CONTENT);
		}
	}

}
