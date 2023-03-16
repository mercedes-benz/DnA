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
import java.util.Optional;
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
import com.daimler.data.dto.TrinoQueryResponse;
import com.daimler.data.dto.trino.ExecuteStatementResponseVO;
import com.daimler.data.dto.trino.ParquetUploadResponseVO;
import com.daimler.data.dto.trino.TrinoCreateResponseVO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TrinioServiceImpl implements TrinioService {
	
	@Autowired
	private MinioConfig minioConfig;

	@Value("${trino.uri}")
	private String trinoBaseUri;
	
	@Autowired
	private TrinoClient trinoClient;
	
	@Value("${trino.catalog}")
	private String trinoCatalog;
	
	@Autowired
	private ParquetReaderClient parquetReader;
	
	private static String createSchema = "CREATE SCHEMA IF NOT EXISTS ";
	private static String dropSchema = "DROP SCHEMA IF EXISTS ";
	private static String createTable = "CREATE TABLE IF NOT EXISTS ";
	
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
			newParquetObjectPath = "/" + newPathPrefix +  "PublishedParquet-" + newRandFolder + "-" + schemaName + "." + tableName;
		else
			newParquetObjectPath = "/" + "PublishedParquet-" + newRandFolder + "-" + schemaName + "." + tableName;
		final String externalLocation = "s3a://"+sourceBucketName+newParquetObjectPath;
		String publishedFolderPath = newParquetObjectPath;
		newParquetObjectPath = newParquetObjectPath +"/" + fileName;
		String s3aFormatParquetPath = "s3a://"+sourceBucketName+newParquetObjectPath;
		String createTableStatement = createTable + trinoCatalog + "." + schemaName + "." + tableName + " (";	
		try {
			tempParquet = parquetReader.getParquetData("s3a://"+sourceBucketName+"/"+parquetObjectPath);
			createTableStatement = parquetReader.getCreateTableStatement(tempParquet, createTableStatement);
			log.info("Successfully read parquet file at location {}",s3aFormatParquetPath);
		}catch(Exception e) {
			MessageDescription readParquetException = new MessageDescription();
			String readParquetExceptionMessage = "Error while reading parquet file, Please upload a valid parquet file with proper schema";
					//e.getMessage();
			readParquetException.setMessage(readParquetExceptionMessage);
			errors.add(readParquetException);
			message.setSuccess("Failed while reading parquet file.");
			log.error("Failed while reading parquet file at location {} with exception {}", s3aFormatParquetPath, e.getMessage());
			message.setErrors(errors);
			responseVO.setData(dataVO);
			responseVO.setMessage(message);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		try {
			minioConfig.moveObject(sourceBucketName, parquetObjectPath, sourceBucketName, newParquetObjectPath);
			log.info("Parquet file moved from {} to {} successfully",sourceBucketName+"/"+parquetObjectPath, newParquetObjectPath );
		}catch(Exception e) {
			MessageDescription copyException = new MessageDescription();
			String copyExceptionMessage = "Error while moving parquet file to new auto generated folder";
					//e.getMessage();
			copyException.setMessage(copyExceptionMessage);
			errors.add(copyException);
			message.setSuccess("Failed while moving parquet file to new location");
			log.error("Failed while moving parquet file to new location, for given file at bucket {} and path {} . Exception is {}", 
					sourceBucketName, parquetObjectPath, e.getMessage());
			message.setErrors(errors);
			responseVO.setData(dataVO);
			responseVO.setMessage(message);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		String createSchemaStatement = createSchema + trinoCatalog + "." + schemaName + " WITH (location = '" + externalLocation + "')";
		String dropSchemaStatement = dropSchema + trinoCatalog + "." + schemaName;
		try {
			trinoClient.executeStatments(createSchemaStatement);
			log.info("Successfully created Schema named {} at catalog {}", schemaName, trinoCatalog);
		}catch(Exception e) {
			MessageDescription createSchemaException = new MessageDescription();
			String createSchemaExceptionMessage = "Error while executing create schema statement";
					//e.getMessage();
			createSchemaException.setMessage(createSchemaExceptionMessage);
			errors.add(createSchemaException);
			message.setSuccess("Failed while executing create schema statement at trino");
			log.error("Failed while executing create schema statement {} at trino, with exception {}", createSchemaStatement, e.getMessage());
			message.setErrors(errors);
			responseVO.setData(dataVO);
			responseVO.setMessage(message);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		try {
			if(createTableStatement!= null && !createTableStatement.isBlank() && !createTableStatement.isEmpty()) {
				createTableStatement += " WITH ( format='PARQUET', external_location = '" +  externalLocation + "')";
				trinoClient.executeStatments(createTableStatement);
				log.info("Successfully executed create table statement");
			}else {
				log.info("Unable to generate create table statement for {}. Hence, didnt execute any create table statement.", trinoCatalog + "." + schemaName + "." + tableName);
				throw new Exception("Failed to generate create table statement");
			}
		}catch(Exception e) {
			try {
				trinoClient.executeStatments(dropSchemaStatement);
			} catch (Exception ex) {
				MessageDescription dropSchemaException = new MessageDescription();
				String dropSchemaExceptionMessage = "Error executing Drop schema statement to rollback.";
						//ex.getMessage();
				dropSchemaException.setMessage(dropSchemaExceptionMessage);
				errors.add(dropSchemaException);
				message.setSuccess("Failed while executing create table statement at trino, after schema creation. Revert created schema failed");
				log.error("Failed while executing drop schema statement {} at trino. Revert created schema failed with exception {}", dropSchemaStatement, ex.getMessage());
				message.setErrors(errors);
				responseVO.setData(dataVO);
				responseVO.setMessage(message);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			MessageDescription createTableException = new MessageDescription();
			String createTableExceptionMessage = "Error executing create table statement at trino.";
					//e.getMessage();
			createTableException.setMessage(createTableExceptionMessage);
			errors.add(createTableException);
			message.setSuccess("Failed while executing create table statement at trino, after schema creation. Schema creation rolledback.");
			log.error("Failed while executing create table statement {} at trino, with exception {}. Create schema rolledback. ", createTableStatement, e.getMessage());
			message.setErrors(errors);
			responseVO.setData(dataVO);
			responseVO.setMessage(message);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		try {
			List<TrinoQueryResponse> queries = trinoClient.queryIds();
			log.info("Fetched all query ids from trino");
			if(queries !=null && !queries.isEmpty()) {
				Optional<TrinoQueryResponse> createSchemaResponseOptional = queries.stream().filter(n->createSchemaStatement.equalsIgnoreCase(n.getQuery())).findFirst();
				if(createSchemaResponseOptional!=null && createSchemaResponseOptional.get()!= null && createSchemaResponseOptional.get().getQueryId()!=null) {
					TrinoQueryResponse createSchemaResponse = createSchemaResponseOptional.get();
					String createSchemaId = createSchemaResponse.getQueryId();
					String createSchemaQueryInfoUrl = trinoBaseUri + "/ui/query.html?" + createSchemaId;
					ExecuteStatementResponseVO schemaCreateSchemaResponse = new ExecuteStatementResponseVO();
					schemaCreateSchemaResponse.setId(createSchemaId);
					schemaCreateSchemaResponse.setInfoUrl(createSchemaQueryInfoUrl);
					schemaCreateSchemaResponse.setState(createSchemaResponse.getState());
					dataVO.setCreateSchemaResult(schemaCreateSchemaResponse);
					log.info("Found query id for create schema, id set to response");
				}
				final String createTableStatementFinal = createTableStatement;
				Optional<TrinoQueryResponse> createTableResponseOptional = queries.stream().filter(n->createTableStatementFinal.equalsIgnoreCase(n.getQuery())).findFirst();
				if(createTableResponseOptional!=null && createTableResponseOptional.get()!=null && createTableResponseOptional.get().getQueryId()!=null) {
					TrinoQueryResponse createTableResponse = createTableResponseOptional.get();
					String createTableId = createTableResponse.getQueryId();
					String createTableQueryInfoUrl = trinoBaseUri + "/ui/query.html?" + createTableId;
					ExecuteStatementResponseVO schemaCreateTableResponse = new ExecuteStatementResponseVO();
					schemaCreateTableResponse.setId(createTableId);
					schemaCreateTableResponse.setInfoUrl(createTableQueryInfoUrl);
					schemaCreateTableResponse.setState(createTableResponse.getState());
					dataVO.setCreateTableResult(schemaCreateTableResponse);
					log.info("Found query id for create table, id set to response");
				}
			}
		}catch(Exception e) {
			MessageDescription queryWarning = new MessageDescription();
			String queryIdsExceptionMessage = e.getMessage();
			queryWarning.setMessage("Failed to fetch queryId for create schema and table statements with exception");
			warnings.add(queryWarning);
			message.setSuccess("Successfully copied parquet file, read the metadata and created schema and tables");
			log.warn("Failed to fetch queryId for create schema {} statement with exception {}", schemaName, queryIdsExceptionMessage);
			message.setErrors(null);
			message.setWarnings(warnings);
			responseVO.setData(dataVO);
			responseVO.setPublishedFolderPath(publishedFolderPath);
			responseVO.setMessage(message);
			return new ResponseEntity<>(responseVO, HttpStatus.OK);
		}
		
		message.setSuccess("Successfully copied parquet file, read the metadata and created schema and tables");
		message.setErrors(null);
		message.setWarnings(null);
		responseVO.setPublishedFolderPath(publishedFolderPath);
		responseVO.setData(dataVO); 
		responseVO.setMessage(message);
		return new ResponseEntity<>(responseVO, HttpStatus.OK);
	}

}
