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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.daimler.data.dto.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.api.storage.StorageApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.service.storage.StorageService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Dna Storage API", tags = { "storage" })
@RequestMapping("/api")
@SuppressWarnings(value = "unused")
public class StorageController implements StorageApi {

	private static Logger LOGGER = LoggerFactory.getLogger(StorageController.class);

	@Autowired
	private StorageService storageService;

	@Autowired
	private UserStore userStore;

	@Override
	@ApiOperation(value = "Create new Bucket", nickname = "createBucket", notes = "New Bucket will be created with this api", response = BucketResponseWrapperVO.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure ", response = BucketResponseWrapperVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = BucketResponseWrapperVO.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<BucketResponseWrapperVO> createBucket(
			@ApiParam(value = "Request Body that contains data to create a new bucket", required = true) @Valid @RequestBody BucketRequestVO bucketRequestVO) {
		return storageService.createBucket(bucketRequestVO.getData());
	}

	@Override
	@ApiOperation(value = "Upload object to Bucket", nickname = "objectUpload", notes = "File will be uploaded to Bucket with this api", response = BucketResponseWrapperVO.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure ", response = BucketResponseWrapperVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = BucketResponseWrapperVO.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets/{bucketName}/upload", produces = { "application/json" }, consumes = {
			"multipart/form-data" }, method = RequestMethod.POST)
	public ResponseEntity<BucketResponseWrapperVO> objectUpload(
			@ApiParam(value = "The file to upload.") @Valid @RequestPart(value = "file", required = true) MultipartFile file,
			@ApiParam(value = "Bucket name where file to be uploaded.", required = true) @PathVariable("bucketName") String bucketName,
			@NotNull @ApiParam(value = "Bucket path where file to be uploaded.", required = true) @Valid @RequestParam(value = "prefix", required = true) String prefix) {
		return storageService.objectUpload(file, bucketName, prefix);
	}

	@Override
	@ApiOperation(value = "get Buckets", nickname = "getAllBuckets", notes = "Get bucket with this api", response = BucketCollectionVO.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure ", response = BucketCollectionVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = BucketCollectionVO.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<BucketCollectionVO> getAllBuckets(
		@ApiParam(value = "page size to limit the number of solutions, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit,
		@ApiParam(value = "Sort users based on given column, example created on, last modified on") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
		@ApiParam(value = "Sort users based on given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder,
		@ApiParam(value = "page number from which listing of solutions should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset) 
	{
		int defaultLimit = 20;
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
		return storageService.getAllBuckets(limit, sortBy, sortOrder, offset);
	}

	@Override
	@ApiOperation(value = "Get Objects associated with Bucket name.", nickname = "getBucketObjects", notes = "Get Objects for a given Bucket.", response = BucketObjectResponseWrapperVO.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = BucketObjectResponseWrapperVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets/{bucketName}/objects", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<BucketObjectResponseWrapperVO> getBucketObjects(
			@ApiParam(value = "Bucket name for which object to be fetch.", required = true) @PathVariable("bucketName") String bucketName,
			@ApiParam(value = "Path for which object need to be fetch.") @Valid @RequestParam(value = "prefix", required = false) String prefix) {
		return storageService.getBucketObjects(bucketName, prefix);
	}

	@Override
	@ApiOperation(value = "Get Objects associated with Bucket name.", nickname = "getObjectContent", notes = "Get Objects for a given Bucket.", response = ByteArrayResource.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = ByteArrayResource.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets/{bucketName}/objects/metadata", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<ByteArrayResource> getObjectContent(
			@ApiParam(value = "Bucket name for which object to be fetch.", required = true) @PathVariable("bucketName") String bucketName,
			@NotNull @ApiParam(value = "Object path for which metadata need to be fetch.", required = true) @Valid @RequestParam(value = "prefix", required = true) String prefix) {
		return storageService.getObjectContent(bucketName, prefix);
	}

	@Override
	@ApiOperation(value = "Refresh user access.", nickname = "userRefresh", notes = "Refresh user credential.", response = UserRefreshWrapperVO.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = UserRefreshWrapperVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets/user/refresh", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<UserRefreshWrapperVO> userRefresh(
			@ApiParam(value = "UserId for which credentials to be refreshed.") @Valid @RequestParam(value = "userId", required = false) String userId) {
		return storageService.userRefresh(userId);
	}

	@Override
	@ApiOperation(value = "Get connection details.", nickname = "getConnection", notes = "Get connection details of given path for user.", response = UserRefreshWrapperVO.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = UserRefreshWrapperVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@GetMapping(path = "/buckets/{bucketName}/connect", produces = { "application/json" }, consumes = {
			"application/json" })
	public ResponseEntity<ConnectionResponseWrapperVO> getConnection(
			@ApiParam(value = "Bucket name for which details to be fetch.", required = true) @PathVariable("bucketName") String bucketName,
			@ApiParam(value = "UserId for which credentials to be fetched.") @Valid @RequestParam(value = "userId", required = false) String userId,
			@ApiParam(value = "Bucket Path for which connection uri be fetch.") @Valid @RequestParam(value = "prefix", required = false) String prefix) {
		return storageService.getConnection(bucketName, userId, prefix);
	}

	@Override
    @ApiOperation(value = "Refresh cache.", nickname = "cacheRefresh", notes = "Refresh cache.", response = GenericMessage.class, tags={ "storage", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of succes or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/buckets/cache/refresh",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<GenericMessage> cacheRefresh() {
		return storageService.cacheRefresh();
	}

	@Override
	@ApiOperation(value = "Delete object of bucket.", nickname = "deleteBucketObjects", notes = "Delete Object of bucket in given path.", response = GenericMessage.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets/{bucketName}/objects", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteBucketObjects(
			@ApiParam(value = "Bucket name in which object to be deleted.", required = true) @PathVariable("bucketName") String bucketName,
			@NotNull @ApiParam(value = "Path of object which need to be deleted.", required = true) @Valid @RequestParam(value = "prefix", required = true) String prefix) {
		return storageService.deleteBucketObjects(bucketName, prefix);
	}

	@Override
	@ApiOperation(value = "Delete bucket.", nickname = "deleteBucket", notes = "Delete bucket identified by bucketName.", response = GenericMessage.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets/{bucketName}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteBucket(
			@ApiParam(value = "Bucket name which need to be deleted.", required = true) @PathVariable("bucketName") String bucketName,
			@ApiParam(value = "If requested data from live(Production) or training dataiku environment", defaultValue = "true") @Valid @RequestParam(value = "live", required = false, defaultValue = "true") Boolean live) {

		return storageService.deleteBucket(bucketName, live);
	}


	@Override
	@ApiOperation(value = "Delete bucket.", nickname = "deleteBucketCascade", notes = "Delete bucket identified by bucketName.", response = GenericMessage.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/v1/buckets/{bucketName}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteBucketCascade(
			@ApiParam(value = "Bucket name which need to be deleted.", required = true) @PathVariable("bucketName") String bucketName,
			@ApiParam(value = "If requested data from live(Production) or training dataiku environment", defaultValue = "true") @Valid @RequestParam(value = "live", required = false, defaultValue = "true") Boolean live) {

		return storageService.deleteBucketCascade(bucketName, live);
	}

	@Override
	@ApiOperation(value = "is BucketPresent.", nickname = "isBucketPresent", notes = "True if the bucket exists.", response = GenericMessage.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets/{bucketName}/present", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<BucketPresentVO> isBucketPresent(String bucketName, Boolean live) {
		BucketPresentVO bucketPresentVO = new BucketPresentVO();
		GenericMessage message = new GenericMessage();
		bucketPresentVO.isBucketPresent(storageService.isBucketPresent(bucketName));
		message.setSuccess("SUCCESS");
		bucketPresentVO.setResponse(message);
		return new ResponseEntity<>(bucketPresentVO, HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "Update existing Bucket", nickname = "updateBucket", notes = "Bucket will be updated with this api", response = BucketResponseWrapperVO.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure ", response = BucketResponseWrapperVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = BucketResponseWrapperVO.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<BucketResponseWrapperVO> updateBucket(
			@ApiParam(value = "Request Body that contains data to update an existing bucket", required = true) @Valid @RequestBody BucketRequestVO bucketRequestVO) {
		return storageService.updateBucket(bucketRequestVO.getData());
	}

	@Override
    @ApiOperation(value = "Get bucket by name.", nickname = "getByBucketName", notes = "Get bucket identified by bucketName.", response = BucketVo.class, tags={ "storage", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of succes or failure", response = BucketResponseVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/buckets/{bucketName}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<BucketVo> getByBucketName(@ApiParam(value = "Bucket name which need to be fetched.",required=true) @PathVariable("bucketName") String bucketName) {
		return storageService.getByBucketName(bucketName);
	}

	@Override
    @ApiOperation(value = "MIgrate buckets to storage db.", nickname = "bucketMigrate", notes = "MIgrate buckets.", response = GenericMessage.class, tags={ "storage", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of succes or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
	@GetMapping(path = "/buckets/migrate",
        produces = { "application/json" }, 
        consumes = { "application/json" })
	public ResponseEntity<GenericMessage> bucketMigrate() {
		return storageService.bucketMigrate();
	}

	@Override
	@ApiOperation(value = "Create dataiku connection for bucket", nickname = "createDataikuConnection", notes = "Connection for bucket with dataiku projects will get created by this api", response = GenericMessage.class, tags = {
			"storage", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure ", response = GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad Request", response = BucketResponseWrapperVO.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@PostMapping(path = "/buckets/dataiku/connect", produces = { "application/json" }, consumes = {
			"application/json" })
	public ResponseEntity<GenericMessage> createDataikuConnection(
			@ApiParam(value = "Request Body that contains data to create connection for bucket with dataiku projects.", required = true) @Valid @RequestBody ConnectionRequestVO connectionRequestVO,
			@ApiParam(value = "If requested data from live(Production) or training environment", defaultValue = "true") @Valid @RequestParam(value = "live", required = false, defaultValue="true") Boolean live) {
		return storageService.createDataikuConnection(connectionRequestVO.getData(), live);
	}

}
