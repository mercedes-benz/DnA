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

import com.daimler.data.api.datavolume.DatavolumesApi;
import com.daimler.data.dto.datavolume.DataVolumeCollection;
import com.daimler.data.dto.datavolume.DataVolumeVO;
import com.daimler.data.service.datavolume.DataVolumeService;
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

import java.util.Comparator;
import java.util.List;

@RestController
@Api(value = "DataVolume API", tags = { "datavolumes" })
@RequestMapping("/api")
@Slf4j
public class DataVolumeController implements DatavolumesApi {

	@Autowired
	private DataVolumeService datavolumeService;

	@Override
	@ApiOperation(value = "Get all available datavolumes.", nickname = "getAll", notes = "Get all datavolumes. This endpoints will be used to Get all valid available datavolumes maintenance records.", response = DataVolumeCollection.class, tags = {
			"datavolumes", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = DataVolumeCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datavolumes", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<DataVolumeCollection> getAll() {
		final List<DataVolumeVO> datavolumes = datavolumeService.getAll();
		Comparator<DataVolumeVO> comparator = Comparator.comparing(DataVolumeVO::getId);
		datavolumes.sort(comparator);
		DataVolumeCollection datavolumeCollection = new DataVolumeCollection();
		if (datavolumes != null && datavolumes.size() > 0) {
			datavolumeCollection.addAll(datavolumes);
			log.debug("Returning all available datavolumes");
			return new ResponseEntity<>(datavolumeCollection, HttpStatus.OK);
		} else {
			log.debug("No datavolumes found, returning empty");
			return new ResponseEntity<>(datavolumeCollection, HttpStatus.NO_CONTENT);
		}
	}

}
