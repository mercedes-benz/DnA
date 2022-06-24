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

package com.daimler.data.dna.trino.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dna.trino.config.MinioConfig;
import com.daimler.data.dna.trino.config.ParquetReaderClient;
import com.daimler.data.dna.trino.config.TrinoClient;
import com.daimler.data.dto.Parquet;
import com.daimler.data.dto.TrinoResponse;
import com.daimler.data.dto.TrinoResponseStats;
import com.daimler.data.dto.trino.ExecuteStatementResponseVO;
import com.daimler.data.dto.trino.ParquetUploadResponseVO;
import com.daimler.data.dto.trino.TrinoCreateResponseVO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TrinioServiceImpl implements TrinioService {

	@Value("${minio.trino.bucketName}")
	private String minioTrinoBucketName;
	
	@Autowired
	private MinioConfig minioConfig;

	@Autowired
	private TrinoClient trinoClient;
	
	@Value("${trino.catalog}")
	private String trinoCatalog;
	
	@Autowired
	private ParquetReaderClient parquetReader;
	
	private static String createSchema = "CREATE SCHEMA IF NOT EXISTS ";
	
	@Override
	public ResponseEntity<ParquetUploadResponseVO> uploadParquet(String sourceBucketName, String parquetObjectPath, String schemaName, String tableName) {
		ParquetUploadResponseVO responseVO = new ParquetUploadResponseVO();
		TrinoCreateResponseVO dataVO = new TrinoCreateResponseVO();
		GenericMessage message = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		
		Parquet tempParquet = new Parquet();
		
		String newRandFolder = UUID.randomUUID().toString();
		String newPathPrefix = "";
		String fileName = "";
		if(parquetObjectPath !=null && !"".equalsIgnoreCase(parquetObjectPath)) {
			String[] paths = parquetObjectPath.split("/");
			int i = 0;
			fileName = paths[paths.length-1];
	          while(i<paths.length-1){
	        	  newPathPrefix = newPathPrefix  + paths[i] + "/";
	              i++;
	          }
		}
		String newParquetObjectPath = "";
		if(newPathPrefix!=null && !"".equalsIgnoreCase(newPathPrefix) && newPathPrefix.contains("/"))
			newParquetObjectPath = "/" + newPathPrefix +  "PublishedParquet-" + newRandFolder + "-" + schemaName + "." + tableName +"/" + fileName;
		else
			newParquetObjectPath = "/" + "PublishedParquet-" + newRandFolder + "-" + schemaName + "." + tableName +"/" + fileName;
		try {
			minioConfig.moveObject(sourceBucketName, parquetObjectPath, sourceBucketName, newParquetObjectPath);
			log.info("Parquet file copied from {} to {} successfully",sourceBucketName+"/"+parquetObjectPath, minioTrinoBucketName + "/" + newParquetObjectPath );
		}catch(Exception e) {
			MessageDescription copyException = new MessageDescription();
			String copyExceptionMessage = e.getMessage();
			copyException.setMessage(copyExceptionMessage);
			errors.add(copyException);
			message.setSuccess("Failed while copying parquet file to new location");
			log.error("Failed while copying parquet file to new location, for given file at bucket {} and path {} . Exception is {}", 
					sourceBucketName, parquetObjectPath, e.getMessage());
			message.setErrors(errors);
			responseVO.setData(dataVO);
			responseVO.setMessage(message);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		String s3aFormatParquetPath = "s3a://"+sourceBucketName+newParquetObjectPath;
		try {
			tempParquet = parquetReader.getParquetData(s3aFormatParquetPath);
		}catch(Exception e) {
			MessageDescription readException = new MessageDescription();
			String readExceptionMessage = e.getMessage();
			readException.setMessage(readExceptionMessage);
			errors.add(readException);
			message.setSuccess("Failed while reading parquet file.");
			log.error("Failed while reading parquet file at location {} with exception {}", s3aFormatParquetPath, e.getMessage());
			message.setErrors(errors);
			responseVO.setData(dataVO);
			responseVO.setMessage(message);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		String createSchemaStatement = createSchema + schemaName;
		try {
			TrinoResponse trinoSchemaCreateResponse = trinoClient.executeStatements(createSchemaStatement);
			if(trinoSchemaCreateResponse!=null) {
				ExecuteStatementResponseVO schemaCreateResponse = new ExecuteStatementResponseVO();
				schemaCreateResponse.setId(trinoSchemaCreateResponse.getId());
				schemaCreateResponse.setInfoUrl(trinoSchemaCreateResponse.getInfoUri());
				TrinoResponseStats createSchemaResponseStats = trinoSchemaCreateResponse.getStats();
				if(createSchemaResponseStats!= null)
					schemaCreateResponse.setState(createSchemaResponseStats.getState());
				dataVO.setCreateSchemaResult(schemaCreateResponse);
				responseVO.setData(dataVO);
				message.setSuccess("Successfully copied parquet file, read the metadata and created schema and tables");
				message.setErrors(null);
				message.setWarnings(null);
				responseVO.setMessage(message);
				return new ResponseEntity<>(responseVO, HttpStatus.OK);
			}
		}catch(Exception e) {
			MessageDescription readException = new MessageDescription();
			String readExceptionMessage = e.getMessage();
			readException.setMessage(readExceptionMessage);
			errors.add(readException);
			message.setSuccess("Failed while executing create schema statement at trino");
			log.error("Failed while executing create schema statement {} at trino, with exception {}", createSchemaStatement, e.getMessage());
			message.setErrors(errors);
			responseVO.setData(dataVO);
			responseVO.setMessage(message);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return null;
	}

}
