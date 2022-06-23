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

package com.daimler.data.controller.trino;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.trino.TrinoApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dna.trino.service.TrinioService;
import com.daimler.data.dto.trino.ParquetUploadRequestVO;
import com.daimler.data.dto.trino.ParquetUploadResponseVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Parquet Reader and Trino Services API", tags = { "trino" })
@RequestMapping("/api")
@Slf4j
public class TrinoController implements TrinoApi{
	
	@Autowired 
	private TrinioService service;
	
	@Override
	@ApiOperation(value = "Parquet upload to process and execute statements in trino", nickname = "upload", notes = "Parquet upload to process and execute statements in trino", response = ParquetUploadResponseVO.class, tags={ "trino", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of succes", response = ParquetUploadResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = ParquetUploadResponseVO.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/parquet",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<ParquetUploadResponseVO> upload(@ApiParam(value = "Request Body that contains location of parquet file in s3 minio buckets" ,required=true )  @Valid @RequestBody ParquetUploadRequestVO parquetUploadRequestVO){
		return service.uploadParquet(parquetUploadRequestVO.getSourceBucket(), parquetUploadRequestVO.getSourceParquetPath(), parquetUploadRequestVO.getSchemaName(), parquetUploadRequestVO.getTableName());
	}

}
