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

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.customerJourneyPhase.CustomerJourneyPhasesApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.customerJourneyPhase.CustomerJourneyPhaseCollection;
import com.daimler.data.dto.customerJourneyPhase.CustomerJourneyPhaseRequestVO;
import com.daimler.data.dto.customerJourneyPhase.CustomerJourneyPhaseResponseVO;
import com.daimler.data.dto.customerJourneyPhase.CustomerJourneyPhaseVO;
import com.daimler.data.service.customerJourneyPhase.CustomerJourneyPhaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "CustomerJourneyPhase API", tags = { "customerJourneyPhases" })
@RequestMapping("/api")
@Slf4j
public class CustomerJourneyPhaseController implements CustomerJourneyPhasesApi {

	@Autowired
	private CustomerJourneyPhaseService customerJourneyPhaseService;

	@Override
	 @ApiOperation(value = "Adds a new customerJourneyPhases.", nickname = "create", notes = "Adds a new non existing customerJourneyPhases which is used in providing solution.", response = CustomerJourneyPhaseVO.class, tags={ "customerJourneyPhases", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = CustomerJourneyPhaseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/customerJourneyPhases",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<CustomerJourneyPhaseVO> create(@ApiParam(value = "Request Body that contains data required for creating a new customerJourneyPhase" ,required=true ) 
    @RequestBody CustomerJourneyPhaseRequestVO customerJourneyPhaseRequestVO) {

		CustomerJourneyPhaseVO requestcustomerJourneyPhaseVO = customerJourneyPhaseRequestVO.getData();
		try {
			CustomerJourneyPhaseVO existingCustomerJourneyPhaseVO = customerJourneyPhaseService.getByUniqueliteral("name", requestcustomerJourneyPhaseVO.getName());
			if (existingCustomerJourneyPhaseVO != null && existingCustomerJourneyPhaseVO.getName() != null)
				return new ResponseEntity<>(existingCustomerJourneyPhaseVO, HttpStatus.CONFLICT);
			requestcustomerJourneyPhaseVO.setId(null);
			CustomerJourneyPhaseVO customerJourneyPhaseVO = customerJourneyPhaseService.create(requestcustomerJourneyPhaseVO);
			if (customerJourneyPhaseVO != null && customerJourneyPhaseVO.getId() != null) {
				log.debug("customerJourneyPhase {} created successfully", requestcustomerJourneyPhaseVO.getName());
				return new ResponseEntity<>(customerJourneyPhaseVO, HttpStatus.CREATED);
			} else {
				log.debug("customerJourneyPhaseVO {} creation failed with unknown error ", requestcustomerJourneyPhaseVO.getName());
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			log.error("Failed to create customerJourneyPhase {} with exception {} ", requestcustomerJourneyPhaseVO.getName(),
					e.getLocalizedMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	
	@Override
	@ApiOperation(value = "Get all available customerJourneyPhases.", nickname = "getAll", notes = "Get all customerJourneyPhases. This endpoints will be used to Get all valid available customerJourneyPhases maintenance records.", response = CustomerJourneyPhaseCollection.class, tags={ "customerJourneyPhases", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = CustomerJourneyPhaseCollection.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/customerJourneyPhases",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<CustomerJourneyPhaseCollection> getAll() {
		final List<CustomerJourneyPhaseVO> customerJourneyPhases = customerJourneyPhaseService.getAll();		
		CustomerJourneyPhaseCollection customerJourneyPhaseCollection = new CustomerJourneyPhaseCollection();
		log.debug("Sending all customerJourneyPhases");
		if (customerJourneyPhases != null && customerJourneyPhases.size() > 0) {
			
			customerJourneyPhaseCollection.addAll(customerJourneyPhases);
			return new ResponseEntity<>(customerJourneyPhaseCollection, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(customerJourneyPhaseCollection, HttpStatus.NO_CONTENT);
		}
	}


	@Override
	public ResponseEntity<CustomerJourneyPhaseResponseVO> update(
			@Valid CustomerJourneyPhaseRequestVO customerJourneyPhaseRequestVO) {
		CustomerJourneyPhaseVO customerJourneyPhaseVO = customerJourneyPhaseRequestVO.getData();
		try {
			return customerJourneyPhaseService.updateCustomerJourneyPhase(customerJourneyPhaseVO);
		}catch(Exception e) {
			log.error("CustomerJourneyPhaseVO with id {} cannot be edited. Failed due to internal error {} ",
					customerJourneyPhaseVO.getId(), e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			CustomerJourneyPhaseResponseVO response = new CustomerJourneyPhaseResponseVO();
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@Override
	 @ApiOperation(value = "Deletes the customerJourneyPhase identified by given ID.", nickname = "delete", notes = "Deletes the customerJourneyPhase identified by given ID", response = GenericMessage.class, tags={ "customerJourneyPhases", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "Invalid id, record not found."),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/customerJourneyPhases/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(String id) {
		try {
			return customerJourneyPhaseService.deleteCustomerJourneyPhase(id);
		} catch (Exception e) {
			log.error("Failed while delete customerJourneyPhase {} with exception {}", id, e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription(
					"Failed to delete due to internal error. " + e.getMessage());
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
