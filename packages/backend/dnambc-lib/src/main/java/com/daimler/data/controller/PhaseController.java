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

import com.daimler.data.api.phase.PhasesApi;
import com.daimler.data.dto.phase.PhaseCollection;
import com.daimler.data.dto.phase.PhaseVO;
import com.daimler.data.service.phase.PhaseService;
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

import javax.validation.Valid;
import java.util.List;

@RestController
@Api(value = "Phase API", tags = { "phases" })
@RequestMapping("/api")
@Slf4j
public class PhaseController implements PhasesApi {

	@Autowired
	private PhaseService phaseService;

	@Override
	public ResponseEntity<Void> createPhase(@Valid PhaseVO phaseVO) {
		return new ResponseEntity<>(null, HttpStatus.NOT_IMPLEMENTED);
	}

	@Override
	@ApiOperation(value = "Get all available phases.", nickname = "getAll", notes = "Get all phases. This endpoints will be used to Get all valid available phases maintenance records.", response = PhaseCollection.class, tags = {
			"phases", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all phases", response = PhaseCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/phases", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<PhaseCollection> getAll() {
		final List<PhaseVO> phasesVO = phaseService.getAll();
		PhaseCollection phaseCollection = new PhaseCollection();
		if (phasesVO != null && phasesVO.size() > 0) {
			phaseCollection.addAll(phasesVO);
			log.debug("Returning all available phases");
			return new ResponseEntity<>(phaseCollection, HttpStatus.OK);
		} else {
			log.debug("No phases available, returning empty");
			return new ResponseEntity<>(phaseCollection, HttpStatus.NO_CONTENT);
		}
	}

}
