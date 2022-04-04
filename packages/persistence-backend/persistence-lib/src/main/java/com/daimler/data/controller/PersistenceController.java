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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.api.persistence.PersistenceApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.persistence.BucketCollectionVO;
import com.daimler.data.dto.persistence.BucketObjectResponseWrapperVO;
import com.daimler.data.dto.persistence.BucketRequestVO;
import com.daimler.data.dto.persistence.BucketResponseWrapperVO;
import com.daimler.data.dto.persistence.UserRefreshWrapperVO;
import com.daimler.data.service.persistence.PersistenceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Persistence API", tags = { "persistence" })
@RequestMapping("/api")
@SuppressWarnings(value = "unused")
public class PersistenceController implements PersistenceApi {

	private static Logger LOGGER = LoggerFactory.getLogger(PersistenceController.class);

	@Autowired
	private PersistenceService persistenceService;

	@Autowired
	private UserStore userStore;

	@Override
	@ApiOperation(value = "Create new Bucket", nickname = "createBucket", notes = "New Bucket will be created with this api", response = BucketResponseWrapperVO.class, tags = {
			"persistence", })
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
		return persistenceService.createBucket(bucketRequestVO.getData());
	}

	@Override
	@ApiOperation(value = "Upload object to Bucket", nickname = "objectUpload", notes = "File will be uploaded to Bucket with this api", response = BucketResponseWrapperVO.class, tags = {
			"persistence", })
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
		return persistenceService.objectUpload(file, bucketName, prefix);
	}

	@Override
	@ApiOperation(value = "get Buckets", nickname = "getAllBuckets", notes = "Get bucket with this api", response = BucketCollectionVO.class, tags = {
			"persistence", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure ", response = BucketCollectionVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = BucketCollectionVO.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<BucketCollectionVO> getAllBuckets() {
		return persistenceService.getAllBuckets();
	}

	@Override
	@ApiOperation(value = "Get Objects associated with Bucket name.", nickname = "getBucketObjects", notes = "Get Objects for a given Bucket.", response = BucketObjectResponseWrapperVO.class, tags = {
			"persistence", })
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
		return persistenceService.getBucketObjects(bucketName, prefix);
	}

	@Override
	@ApiOperation(value = "Get Objects associated with Bucket name.", nickname = "getObjectContent", notes = "Get Objects for a given Bucket.", response = ByteArrayResource.class, tags = {
			"persistence", })
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
		return persistenceService.getObjectContent(bucketName, prefix);
	}

	@Override
	@ApiOperation(value = "Refresh user access.", nickname = "userRefresh", notes = "Refresh user credential.", response = UserRefreshWrapperVO.class, tags = {
			"persistence", })
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
		return persistenceService.userRefresh(userId);
	}

	@Override
	@ApiOperation(value = "Get connection details.", nickname = "getConnection", notes = "Get connection details of given path for user.", response = UserRefreshWrapperVO.class, tags = {
			"persistence", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = UserRefreshWrapperVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets/{bucketName}/connect", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<UserRefreshWrapperVO> getConnection(
			@ApiParam(value = "Bucket name for which details to be fetch.", required = true) @PathVariable("bucketName") String bucketName,
			@ApiParam(value = "UserId for which credentials to be fetched.") @Valid @RequestParam(value = "userId", required = false) String userId,
			@ApiParam(value = "Bucket Path for which connection uri be fetch.") @Valid @RequestParam(value = "prefix", required = false) String prefix) {
		return persistenceService.getConnection(bucketName, userId, prefix);
	}

	@Override
    @ApiOperation(value = "Refresh cache.", nickname = "cacheRefresh", notes = "Refresh cache.", response = GenericMessage.class, tags={ "persistence", })
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
		return persistenceService.cacheRefresh();
	}

	@Override
	@ApiOperation(value = "Delete object of bucket.", nickname = "deleteBucketObjects", notes = "Delete Object of bucket in given path.", response = GenericMessage.class, tags = {
			"persistence", })
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
		return persistenceService.deleteBucketObjects(bucketName, prefix);
	}

	@Override
	@ApiOperation(value = "Delete bucket.", nickname = "deleteBucket", notes = "Delete bucket identified by bucketName.", response = GenericMessage.class, tags = {
			"persistence", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of succes or failure", response = GenericMessage.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/buckets/{bucketName}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteBucket(
			@ApiParam(value = "Bucket name which need to be deleted.", required = true) @PathVariable("bucketName") String bucketName) {
		return persistenceService.deleteBucket(bucketName);
	}

}
