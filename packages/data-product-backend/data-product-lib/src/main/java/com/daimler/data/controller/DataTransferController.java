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

import com.daimler.data.api.datatransfer.DatatransfersApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.dataproduct.TransparencyVO;
import com.daimler.data.dto.datatransfer.*;
import com.daimler.data.service.datatransfer.DataTransferService;
import com.daimler.data.util.ConstantsUtility;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Api(value = "DataTransfer API", tags = { "datatransfers" })
@RequestMapping("/api")
public class DataTransferController implements DatatransfersApi {

	private static Logger LOGGER = LoggerFactory.getLogger(DataTransferController.class);

	@Autowired
	private DataTransferService dataTransferService;

	@Override
	@ApiOperation(value = "Add a new datatransfer provider form", nickname = "create", notes = "Adds a new non existing datatransfer provider form", response = DataTransferProviderResponseVO.class, tags={ "datatransfers", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success", response = DataTransferProviderResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/datatransfers/provider",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<DataTransferProviderResponseVO> create(@ApiParam(value = "Request Body that contains data required for creating a new datatransfer provider form" ,required=true )  @Valid @RequestBody DataTransferProviderRequestVO dataTransferProviderRequestVO){
		return dataTransferService.createDataTransferProvider(dataTransferProviderRequestVO.getData(), false);
	}

	@Override
	@ApiOperation(value = "Delete datatransfer for a given Id.", nickname = "delete", notes = "Delete datatransfer for a given identifier.", response = GenericMessage.class, tags={ "datatransfers", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/datatransfers/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> delete(@ApiParam(value = "DataTransfer ID to be deleted",required=true) @PathVariable("id") String id) {
		return dataTransferService.deleteDataTransfer(id);
	}

	@Override
	@ApiOperation(value = "Get all available datatransfers.", nickname = "getAll", notes = "Get all datatransfers. This endpoints will be used to get all valid available datatransfer records.", response = DataTransferCollection.class, tags={ "datatransfers", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = DataTransferCollection.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/datatransfers",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<DataTransferCollection> getAll(
			@ApiParam(value = "datatransfer ID to be fetched (send id's with comma separated eg: 'DTF-00019', 'DTF-00020'..)") @Valid @RequestParam(value = "datatransferIds", required = false) String datatransferIds,
			@ApiParam(value = "If true then sends datatransfer which are created by the logged In users") @Valid @RequestParam(value = "isCreator", required = false) Boolean isCreator,
			@ApiParam(value = "if true then sends dataproduct which are created by the logged In users") @Valid @RequestParam(value = "isProviderCreator", required = false) Boolean isProviderCreator,
			@ApiParam(value = "Filtering datatransfer based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
    		@ApiParam(value = "page number from which listing of datatransfers should start.") @Valid @RequestParam(value = "offset", required = false) Integer offset,
    		@ApiParam(value = "page size to limit the number of datatransfers.") @Valid @RequestParam(value = "limit", required = false) Integer limit,
    		@ApiParam(value = "Sort datatransfers by a given variable.", allowableValues = "dataTransferName, dataTransferId") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
    		@ApiParam(value = "Sort datatransfers based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		try {
			DataTransferCollection dataTransferCollection = new DataTransferCollection();

			int defaultLimit = 10;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			if (sortOrder != null && !sortOrder.equals("asc") && !sortOrder.equals("desc")) {
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			}
			if (sortOrder == null) {
				sortOrder = "asc";
			}

			String recordStatus = ConstantsUtility.OPEN;

			Long count = dataTransferService.getCount(published, recordStatus, datatransferIds, isCreator, isProviderCreator);
			if (count < offset)
				offset = 0;

			List<DataTransferVO> dataTransfers = dataTransferService.getAllWithFilters(published, offset, limit, sortBy,
					sortOrder, recordStatus, datatransferIds, isCreator,isProviderCreator);
			LOGGER.info("DataTransfers fetched successfully");
			if (!ObjectUtils.isEmpty(dataTransfers)) {
				dataTransferCollection.setTotalCount(count.intValue());
				dataTransferCollection.setRecords(dataTransfers);
				return new ResponseEntity<>(dataTransferCollection, HttpStatus.OK);
			} else {
				dataTransferCollection.setTotalCount(count.intValue());
				return new ResponseEntity<>(dataTransferCollection, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			LOGGER.error("Failed to fetch dataTransfers with exception {} ", e.getMessage());
			throw e;
		}
	}

	@Override
	 @ApiOperation(value = "Get datatransfer for a given Id.", nickname = "getById", notes = "Get datatransfer for a given identifier. This endpoints will be used to get a datatransfer for a given identifier.", response = DataTransferVO.class, tags={ "datatransfers", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = DataTransferVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/datatransfers/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<DataTransferVO> getById(@ApiParam(value = "datatransfer ID to be fetched",required=true) @PathVariable("id") String id){
		DataTransferVO dataTransferVO = dataTransferService.getById(id);
		if (dataTransferVO != null && !dataTransferVO.getRecordStatus().equalsIgnoreCase(ConstantsUtility.DELETED)) {
			LOGGER.info("DataTransfer with id {} fetched successfully", id);
			return new ResponseEntity<>(dataTransferVO, HttpStatus.OK);
		} else {
			LOGGER.debug("No DataTransfer {} found", id);
			return new ResponseEntity<>(new DataTransferVO(), HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@ApiOperation(value = "Get number of published datatransfers.", nickname = "getNumberOfPublishedDatatransfers", notes = "Get number of published datatransfers. This endpoints will be used to get number of available published  dataproducts records.", response = TransparencyVO.class, tags = {
			"datatransfers", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure", response = TransparencyVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datatransfers/transparency", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TransparencyVO> getNumberOfPublishedDatatransfers() {
		try {
			TransparencyVO transparencyVO = new TransparencyVO();
			Integer count = dataTransferService.getCountBasedPublishDatatransfer(true);
			transparencyVO.setCount(count);
			return new ResponseEntity<>(transparencyVO, HttpStatus.OK);
		}catch (Exception e){
			return new ResponseEntity<>(new TransparencyVO(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Update existing dataTransfer provider form", nickname = "update", notes = "Update an existing datatransfer provider form", response = DataTransferProviderResponseVO.class, tags={ "datatransfers", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success", response = DataTransferProviderResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/datatransfers/provider",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<DataTransferProviderResponseVO> update(@ApiParam(value = "Request Body that contains data required for updating datatransfer provider form" ,required=true )  @Valid @RequestBody DataTransferProviderRequestVO dataTransferProviderRequestVO) {
		return dataTransferService.updateDataTransferProvider(dataTransferProviderRequestVO.getData());
	}

	@Override
	@ApiOperation(value = "Update existing datatransfer consumer form", nickname = "update", notes = "Update an existing datatransfer consumer form", response = DataTransferConsumerResponseVO.class, tags={ "datatransfers", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success", response = DataTransferConsumerResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/datatransfers/consume",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    public ResponseEntity<DataTransferConsumerResponseVO> update(@ApiParam(value = "Request Body that contains data required for updating datatransfer consumer form" ,required=true )  @Valid @RequestBody DataTransferConsumerRequestVO dataTransferConsumerRequestVO){
		return dataTransferService.updateDataTransferConsumer(dataTransferConsumerRequestVO.getData(), false);
	}

}
