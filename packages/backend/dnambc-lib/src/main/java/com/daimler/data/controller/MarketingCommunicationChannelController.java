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
import java.util.Comparator;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.marketingCommunicationChannel.MarketingCommunicationChannelsApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.marketingCommunicationChannel.MarketingCommunicationChannelCollection;
import com.daimler.data.dto.marketingCommunicationChannel.MarketingCommunicationChannelRequestVO;
import com.daimler.data.dto.marketingCommunicationChannel.MarketingCommunicationChannelVO;
import com.daimler.data.service.marketingCommunicationChannel.MarketingCommunicationChannelService;
import com.daimler.data.dto.marketingCommunicationChannel.MarketingCommunicationChannelResponseVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Marketing CommunicationChannel API", tags = { "marketingCommunicationChannels" })
@RequestMapping("/api")
@Slf4j
public class MarketingCommunicationChannelController implements MarketingCommunicationChannelsApi {

	@Autowired
	private MarketingCommunicationChannelService marketingCommunicationChannelService;

	@Override
	@ApiOperation(value = "Adds a new marketingCommunicationChannels.", nickname = "create", notes = "Adds a new non existing marketingCommunicationChannels which is used in providing solution.", response = MarketingCommunicationChannelVO.class, tags={ "marketingCommunicationChannels", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = MarketingCommunicationChannelVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/marketingCommunicationChannels",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<MarketingCommunicationChannelVO> create(@ApiParam(value = "Request Body that contains data required for creating a new marketingCommunicationChannel" ,required=true )
    @RequestBody MarketingCommunicationChannelRequestVO marketingCommunicationChannelRequestVO) {

		MarketingCommunicationChannelVO requestMarketingCommunicationChannelVO = marketingCommunicationChannelRequestVO.getData();
		try {
			MarketingCommunicationChannelVO existingMarketingCommunicationChannelVO = marketingCommunicationChannelService.getByUniqueliteral("name", requestMarketingCommunicationChannelVO.getName());
			if (existingMarketingCommunicationChannelVO != null && existingMarketingCommunicationChannelVO.getName() != null)
				return new ResponseEntity<>(existingMarketingCommunicationChannelVO, HttpStatus.CONFLICT);
			requestMarketingCommunicationChannelVO.setId(null);
			MarketingCommunicationChannelVO marketingCommunicationChannelVO = marketingCommunicationChannelService.create(requestMarketingCommunicationChannelVO);
			if (marketingCommunicationChannelVO != null && marketingCommunicationChannelVO.getId() != null) {
				log.debug("MarketingCommunicationChannel {} created successfully", requestMarketingCommunicationChannelVO.getName());
				return new ResponseEntity<>(marketingCommunicationChannelVO, HttpStatus.CREATED);
			} else {
				log.debug("MarketingCommunicationChannel {} creation failed with unknown error ", requestMarketingCommunicationChannelVO.getName());
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			log.error("Failed to create MarketingCommunicationChannel {} with exception {} ", requestMarketingCommunicationChannelVO.getName(),
					e.getLocalizedMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@Override
	@ApiOperation(value = "Get all available marketingCommunicationChannels.", nickname = "getAll", notes = "Get all marketingCommunicationChannels. This endpoints will be used to Get all valid available marketingCommunicationChannels maintenance records.", response = MarketingCommunicationChannelCollection.class, tags={ "marketingCommunicationChannels", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = MarketingCommunicationChannelCollection.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/marketingCommunicationChannels",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<MarketingCommunicationChannelCollection> getAll(
			@ApiParam(value = "Sort marketingCommunicationChannels based on the given order, example asc,desc", allowableValues = "asc, desc") @RequestParam(value = "sortOrder", required = false) String sortOrder) {

		final List<MarketingCommunicationChannelVO> marketingCommunicationChannels = marketingCommunicationChannelService.getAll();		
		MarketingCommunicationChannelCollection marketingCommunicationChannelCollection = new MarketingCommunicationChannelCollection();
		log.debug("Sending all marketingCommunicationChannels");
		if (marketingCommunicationChannels != null && marketingCommunicationChannels.size() > 0) {
			if (sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
				marketingCommunicationChannels.sort(Comparator.comparing(MarketingCommunicationChannelVO :: getName, String.CASE_INSENSITIVE_ORDER));
			}
			if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
				marketingCommunicationChannels.sort(Comparator.comparing(MarketingCommunicationChannelVO :: getName, String.CASE_INSENSITIVE_ORDER).reversed());
			}
			
			marketingCommunicationChannelCollection.addAll(marketingCommunicationChannels);
			return new ResponseEntity<>(marketingCommunicationChannelCollection, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(marketingCommunicationChannelCollection, HttpStatus.NO_CONTENT);
		}
	
	}

	@ApiOperation(value = "Update the marketingCommunicationChannel", nickname = "update", notes = "Update the marketingCommunicationChannel", response = MarketingCommunicationChannelResponseVO.class, tags={ "marketingCommunicationChannels", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully updated.", response = MarketingCommunicationChannelResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/marketingCommunicationChannels",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
	public ResponseEntity<MarketingCommunicationChannelResponseVO> update(
			@Valid MarketingCommunicationChannelRequestVO marketingCommunicationChannelRequestVO) {
		MarketingCommunicationChannelVO marketingCommunicationChannelVO = marketingCommunicationChannelRequestVO.getData();
		try {
			return marketingCommunicationChannelService.updateMarketingCommunicationChannel(marketingCommunicationChannelVO);
		}catch(Exception e) {
			log.error("MarketingCommunicationChannel with id {} cannot be edited. Failed due to internal error {} ",
					marketingCommunicationChannelVO.getId(), e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			MarketingCommunicationChannelResponseVO response = new MarketingCommunicationChannelResponseVO();
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}

	@Override
	public ResponseEntity<GenericMessage> delete(String id) {
		try {
			return marketingCommunicationChannelService.deleteMarketingCommunicationChannel(id);
		} catch (Exception e) {
			log.error("Failed while delete MarketingCommunicationChannel {} with exception {}", id, e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription(
					"Failed to delete due to internal error. " + e.getMessage());
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
