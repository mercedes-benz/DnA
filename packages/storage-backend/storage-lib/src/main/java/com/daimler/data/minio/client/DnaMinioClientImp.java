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

package com.daimler.data.minio.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.application.config.MinioConfig;
import com.daimler.data.application.config.UserInfoDto;
import com.daimler.data.application.config.UserInfoWrapperDto;
import com.daimler.data.application.config.VaultConfig;
import com.daimler.data.dto.ErrorDTO;
import com.daimler.data.dto.McListBucketCollectionDto;
import com.daimler.data.dto.MinioGenericResponse;
import com.daimler.data.dto.MinioObjectMetadata;
import com.daimler.data.dto.MinioObjectMetadataCollection;
import com.daimler.data.dto.storage.BucketObjectVO;
import com.daimler.data.dto.storage.ObjectMetadataVO;
import com.daimler.data.dto.storage.PermissionVO;
import com.daimler.data.dto.storage.UserVO;
import com.daimler.data.util.ConstantsUtility;
import com.daimler.data.util.PolicyUtility;
import com.daimler.data.util.RedisCacheUtil;
import com.daimler.data.util.StorageUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.admin.MinioAdminClient;
import io.minio.admin.UserInfo;
import io.minio.admin.UserInfo.Status;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;

@Component
public class DnaMinioClientImp implements DnaMinioClient {

	private Logger LOGGER = LoggerFactory.getLogger(DnaMinioClientImp.class);

	@Value("${minio.version}")
	private String minioPolicyVersion;

	@Value("${minio.endpoint}")
	private String minioBaseUri;

	@Value("${minio.accessKey}")
	private String minioAdminAccessKey;

	@Value("${minio.secretKey}")
	private String minioAdminSecretKey;

	@Value("${storage.connect.host}")
	private String storageConnectHost;

	@Value("${storage.httpMethod}")
	private String storageHttpMethod;
	
	@Value("${storage.mc.timeout.alias}")
	private String storageMCAliasCmdTimeout;
	
	@Value("${storage.mc.timeout.listpolicies}")
	private String storageMCListPoliciesCmdTimeout;
	
	@Value("${storage.mc.timeout.listbuckets}")
	private String storageMCListBucketsCmdTimeout;
	
	@Value("${storage.mc.commandkeyword}")
	private String storageMCcommandKey;

	@Autowired
	private MinioConfig minioConfig;
	
	@Autowired
	private VaultConfig vaultConfig;
	
	@Autowired
	private RedisCacheUtil cacheUtil;

	@Override
	public MinioGenericResponse createBucket(String bucketName) {
		MinioGenericResponse minioResponse = new MinioGenericResponse();
		List<String> policies;
		try {
			MinioClient minioClient = minioConfig.getMinioClient();
			// Make Bucket
			LOGGER.debug("Making new bucket: {}", bucketName);
			minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

			LOGGER.debug("Creating policies for bucket:{}", bucketName);
			policies = new ArrayList<>();
			
			//Setting resource for full bucket path
			String resource = "arn:aws:s3:::" + bucketName + "/*";
			
			//action to access bucket and corresponding files & directory
			String action = "";
			
			//Setting policy effect as allow
			String effect = "Allow";
			
			//Statement Id 
			String sid = "";

			LOGGER.debug("Creating READ policy");
			//Creating READ policy eg: bucket1_READ 
			String policyName = bucketName + "_" + ConstantsUtility.READ;
			//Setting action as view all bucket contents
			action = "s3:ListBucket,s3:GetObject,s3:GetBucketLocation";
			createBucketPolicy(policyName, minioPolicyVersion, resource, action, effect, sid);
			policies.add(policyName);

			LOGGER.debug("Creating READWRITE(RW) policy");
			// Create READWRITE(RW):{read+write+delete} policy eg:bucket1_RW
			policyName = bucketName + "_" + ConstantsUtility.READWRITE;
			//Setting action as view, edit & delete all bucket contents
			//action = "s3:ListBucket,s3:GetObject,s3:PutObject,s3:DeleteObject";
			action = "*";
			createBucketPolicy(policyName, minioPolicyVersion, resource, action, effect, sid);
			policies.add(policyName);

			// Create DELETE(DEL) policY
//			policyName = bucketName + "_" + ConstantsUtility.DELETE;
//			action = "s3:ListBucket,s3:GetObject,s3:DeleteObject";
//			CreateBucketPolicy(policyName, minioPolicyVersion, resource, action, effect, sid);
//			policies.add(policyName);

			LOGGER.info("Bucket created successfully.");
			minioResponse.setStatus(ConstantsUtility.SUCCESS);
			minioResponse.setPolicies(policies);

		} catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException
				| MinioException e) {
			LOGGER.error("DNA-MINIO-ERR-002::Error occurred while making new bucket: {}", e.getMessage());
			minioResponse.setStatus(ConstantsUtility.FAILURE);
			minioResponse.setErrors(Arrays.asList(new ErrorDTO(null, "Error occurred while making new bucket: " + bucketName)));
		}

		return minioResponse;
	}

	@Override
	public Boolean isBucketExists(String bucketName) {
		Boolean isBucketExists = null;
		try {
			MinioClient minioClient = minioConfig.getMinioClient();
			LOGGER.debug("Connecting to minio client to get bucket status for:{}", bucketName);
			isBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			LOGGER.error("Error occurred while validating bucket:{}", e.getMessage());
		}
		return isBucketExists;
	}

	@Override
	public MinioGenericResponse objectUpload(String userId, MultipartFile uploadfile, String bucketName,
			String prefix) {
		MinioGenericResponse minioResponse = new MinioGenericResponse();
		try {
			LOGGER.info("Fetching secrets from vault for user:{}", userId);
			String userSecretKey = vaultConfig.validateUserInVault(userId);
			if(StringUtils.hasText(userSecretKey)) {
				LOGGER.debug("Fetch secret from vault successfull for user:{}",userId);
				MinioClient minioClient = MinioClient.builder().endpoint(minioBaseUri).credentials(userId, userSecretKey)
						.build();
				ByteArrayInputStream bais = new ByteArrayInputStream(uploadfile.getBytes());

				LOGGER.debug("Uploading object to minio..{}", uploadfile.getOriginalFilename());
				minioClient.putObject(PutObjectArgs.builder().bucket(bucketName)
						.object(prefix + "/" + uploadfile.getOriginalFilename()).stream(bais, bais.available(), -1)
						.build());
				bais.close();
				LOGGER.info("Success from minio object upload..{}", uploadfile.getOriginalFilename());
				minioResponse.setStatus(ConstantsUtility.SUCCESS);
				minioResponse.setHttpStatus(HttpStatus.OK);
			}else {
				LOGGER.debug("Fetch secret from vault failed for user:{}",userId);
				minioResponse.setErrors(Arrays.asList(new ErrorDTO(null, "Fetch secret from vault failed for user:"+userId)));
				minioResponse.setStatus(ConstantsUtility.FAILURE);
				minioResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
			}

		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			LOGGER.error("DNA-MINIO-ERR-003::Error occured while uploading object to minio: {}", e.getMessage());
			minioResponse.setErrors(Arrays.asList(new ErrorDTO(null, "Error occured while uploading object to minio ")));
			minioResponse.setStatus(ConstantsUtility.FAILURE);
			minioResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return minioResponse;
	}
	
	@Override
	public McListBucketCollectionDto getAllBucketsUsingMC(String userId, boolean isAdmin) {
		McListBucketCollectionDto getBucketResponse = new McListBucketCollectionDto();
		try {
			String userSecretKey ="";
			if(isAdmin) {
				//Setting minio admin credentials
				userId = minioAdminAccessKey;
				userSecretKey = minioAdminSecretKey;
			}else {
				LOGGER.info("Fetching secrets from vault for user:{}", userId);
				userSecretKey = vaultConfig.validateUserInVault(userId);
			}
			if (StringUtils.hasText(userSecretKey)) {
				LOGGER.info("Fetch secret from vault successfull for user:{}", userId);
				boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
				ObjectMapper mapper = new ObjectMapper();
				ProcessBuilder firstBuilder = new ProcessBuilder();
				ProcessBuilder secondBuilder = new ProcessBuilder();
	
				String url = storageHttpMethod + storageConnectHost;
				String env = "storagebeminioclient ";
				String flag = "--insecure";
				String firstCommand = "mc alias set " + env + url + " " + userId + " " + userSecretKey + " " + flag;
				String secondCommand = "mc ls " + env 
						+ " --json " 
						+ flag;
	
				if (isWindows) {
					firstBuilder = new ProcessBuilder("cmd.exe", "/c", firstCommand);
					secondBuilder = new ProcessBuilder("cmd.exe", "/c", secondCommand);
				} else {
					firstBuilder = new ProcessBuilder(storageMCcommandKey, "-c", firstCommand);
					secondBuilder = new ProcessBuilder(storageMCcommandKey, "-c", secondCommand);
				}
	
//				firstBuilder.redirectErrorStream(true);
//				Process p2 = firstBuilder.start();
//				BufferedReader r2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
//				
//				LOGGER.info("minio mc client alias command output is {}",r2.readLine());
//	
//				if(!p2.waitFor(Integer.parseInt(storageMCAliasCmdTimeout), TimeUnit.SECONDS)) {
//				    p2.destroy();
//				}
				
				Process p = secondBuilder.start();
				LOGGER.info("Started mc command to list buckets");
				BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
	
//				if(!p.waitFor(Integer.parseInt(storageMCListBucketsCmdTimeout), TimeUnit.SECONDS)) {
//				    p.destroy();
//				}
				
				LOGGER.info("mc command to list buckets executed, started reading response");
				
				String line;
				String prefix = "{\"data\":[";
				String suffix = "]}";
				String data = "";
				while (true) {
					line = r.readLine();
					if (line == null) {
						break;
					} else {
						data = data.concat(line).concat(",");
					}
				}
				
				LOGGER.info("finished reading response from mc list buckets");
								
				data = prefix.concat(data.substring(0, data.length() - 1)).concat(suffix);
				LOGGER.debug("Policies data from minio to update cache is {} ", data);
				McListBucketCollectionDto listBucketCollectionDto = mapper.readValue(data, McListBucketCollectionDto.class);
				LOGGER.info("Success from minio list bucket for user:{}", userId);
				getBucketResponse.setData(listBucketCollectionDto.getData());
				getBucketResponse.setStatus(ConstantsUtility.SUCCESS);
				getBucketResponse.setHttpStatus(HttpStatus.OK);
				
				r.close();
				p.destroy();
			} else {
					LOGGER.info("User:{} not available in vault.", userId);
					getBucketResponse.setErrors(Arrays.asList(new ErrorDTO(null, "User:" + userId + " not available.")));
					getBucketResponse.setStatus(ConstantsUtility.SUCCESS);
					getBucketResponse.setHttpStatus(HttpStatus.NO_CONTENT);
				}
			}
			catch (Exception e) {
				LOGGER.error("Error occured while listing buckets of minio using mc: {}", e.getMessage());
				if (e.toString() != null && e.toString().contains("code=403")) {
					LOGGER.error("Access denied since no bucket available for user:{}", userId);
					getBucketResponse.setStatus(ConstantsUtility.SUCCESS);
					getBucketResponse.setHttpStatus(HttpStatus.NO_CONTENT);
					LOGGER.info("No bucket available");
				} else {
					getBucketResponse
							.setErrors(Arrays.asList(new ErrorDTO(null, "Error occured while listing buckets of minio. ")));
					getBucketResponse.setStatus(ConstantsUtility.FAILURE);
					getBucketResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		return getBucketResponse;
	}
	
	@Override
	public MinioGenericResponse getAllBuckets(String userId, boolean isAdmin) {
		MinioGenericResponse getBucketResponse = new MinioGenericResponse();
		try {
			String userSecretKey ="";
			if(isAdmin) {
				//Setting minio admin credentials
				userId = minioAdminAccessKey;
				userSecretKey = minioAdminSecretKey;
			}else {
				LOGGER.debug("Fetching secrets from vault for user:{}", userId);
				userSecretKey = vaultConfig.validateUserInVault(userId);
			}
			if (StringUtils.hasText(userSecretKey)) {
				LOGGER.debug("Fetch secret from vault successfull for user:{}", userId);
				MinioClient minioClient = MinioClient.builder().endpoint(minioBaseUri)
						.credentials(userId, userSecretKey).build();

				LOGGER.debug("Listing all buckets for user:{}", userId);
				List<Bucket> buckets = minioClient.listBuckets();

				LOGGER.info("Success from minio list bucket for user:{}", userId);
				getBucketResponse.setBuckets(buckets);
				getBucketResponse.setStatus(ConstantsUtility.SUCCESS);
				getBucketResponse.setHttpStatus(HttpStatus.OK);
			} else {
				LOGGER.info("User:{} not available in vault.", userId);
				getBucketResponse.setErrors(Arrays.asList(new ErrorDTO(null, "User:" + userId + " not available.")));
				getBucketResponse.setStatus(ConstantsUtility.SUCCESS);
				getBucketResponse.setHttpStatus(HttpStatus.NO_CONTENT);
			}
		}catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IOException e) {
			LOGGER.error("Error occured while listing buckets of minio: {}", e.getMessage());
			if (e.toString() != null && e.toString().contains("code=403")) {
				LOGGER.error("Access denied since no bucket available for user:{}", userId);
				getBucketResponse.setStatus(ConstantsUtility.SUCCESS);
				getBucketResponse.setHttpStatus(HttpStatus.NO_CONTENT);
				LOGGER.info("No bucket available");
			} else {
				getBucketResponse
						.setErrors(Arrays.asList(new ErrorDTO(null, "Error occured while listing buckets of minio. ")));
				getBucketResponse.setStatus(ConstantsUtility.FAILURE);
				getBucketResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return getBucketResponse;
	}

	@Override
	public MinioGenericResponse getObjectContents(String userId, String bucketName, String prefix) {
		MinioGenericResponse minioObjectContentResponse = new MinioGenericResponse();
		try {
			LOGGER.debug("Fetching secrets from vault for user:{}", userId);
			String userSecretKey =  vaultConfig.validateUserInVault(userId);
			if(StringUtils.hasText(userSecretKey)) {
				LOGGER.debug("Fetch secret from vault successfull for user:{}",userId);
				MinioClient minioClient = MinioClient.builder().endpoint(minioBaseUri).credentials(userId, userSecretKey)
						.build();

				LOGGER.debug("Fetching object contents from minio for user:{}", userId);
				InputStream stream = minioClient
						.getObject(GetObjectArgs.builder().bucket(bucketName).object(prefix).build());

				ObjectMetadataVO objectMetadataVO = new ObjectMetadataVO();
				ByteArrayResource resource = new ByteArrayResource(stream.readAllBytes());
				objectMetadataVO.setObjectContent(resource);
				stream.close();

				LOGGER.info("Success from minio get Object contents for user:{}", userId);
				minioObjectContentResponse.setObjectMetadata(objectMetadataVO);
				minioObjectContentResponse.setStatus(ConstantsUtility.SUCCESS);
				minioObjectContentResponse.setHttpStatus(HttpStatus.OK);
			}else {
				LOGGER.info("Fetch secret from vault failed for user:{}",userId);
				minioObjectContentResponse.setErrors(Arrays.asList(new ErrorDTO(null, "Fetch secret from vault failed for user:"+userId)));
				minioObjectContentResponse.setStatus(ConstantsUtility.FAILURE);
				minioObjectContentResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
			}
		} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
				| InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException
				| XmlParserException | IOException e) {
			LOGGER.error("DNA-MINIO-ERR-005::Error occured while fetching object content from minio: {}",
					e.getMessage());
			minioObjectContentResponse
					.setErrors(Arrays.asList(new ErrorDTO(null, "Error occured while fetching object content from minio. ")));
			minioObjectContentResponse.setStatus(ConstantsUtility.FAILURE);
			minioObjectContentResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return minioObjectContentResponse;
	}

	@Override
	public List<String> listObjectsInBucket(String userId, String bucketName) {
		List<String> objectNames = new ArrayList<>();
		try {
			LOGGER.debug("Fetching secrets from vault for user:{}", userId);
			String userSecretKey = vaultConfig.validateUserInVault(userId);
			if (StringUtils.hasText(userSecretKey)) {
				LOGGER.debug("Fetch secret from vault successful for user:{}", userId);
				MinioClient minioClient = MinioClient.builder().endpoint(minioBaseUri).credentials(userId, userSecretKey).build();

				// Lists objects information.
				LOGGER.debug("Listing Objects information from minio for user:{}", userId);
				Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).includeVersions(true).recursive(true).build());
				for (Result<Item> result : results) {
					Item item = result.get();
					LOGGER.debug("Got details for object:{}", item.objectName() != null ? item.objectName() : null);
					if (item.objectName() != null) {
						objectNames.add(item.objectName());
					}
				}
			} else {
				LOGGER.error("Fetch secret from vault failed for user:{}", userId);
			}
		} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException |
				 InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
				 XmlParserException | IOException e) {
			LOGGER.error("Error occurred while listing bucket's Recursive object from minio: {}", e.getMessage());
		}

		return objectNames;
	}

	@Override
	public MinioGenericResponse deleteBucketCascade(String userId, String bucketName) {
		MinioGenericResponse minioGenericResponse = new MinioGenericResponse();

		try {
			LOGGER.debug("Fetching secrets from vault for user:{}", userId);
			String userSecretKey = vaultConfig.validateUserInVault(userId);
			if (StringUtils.hasText(userSecretKey)) {
				LOGGER.debug("Fetch secret from vault successful for user:{}", userId);
				MinioClient minioClient = MinioClient.builder().endpoint(minioBaseUri).credentials(userId, userSecretKey).build();

				boolean isBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

				if (isBucketExists) {
					// To get list of Objects In Bucket.
					List<String> objectNames = listObjectsInBucket(userId, bucketName);
					List<DeleteObject> objects = new LinkedList<>();
					if (objectNames != null) {
						for (String objectName : objectNames) {
							objects.add(new DeleteObject(objectName));
						}
					}

					// To Remove objects from the bucket.
					Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());

					List<ErrorDTO> errors = new ArrayList<>();
					for (Result<DeleteError> result : results) {
						DeleteError deleteError = result.get();
						ErrorDTO error = new ErrorDTO(null, "Error in deleting object " + deleteError.objectName() + ": " + deleteError.message());
						errors.add(error);
						LOGGER.info("Error in deleting object {}: {}", deleteError.objectName() != null ? deleteError.objectName() : null, deleteError.message() != null ? deleteError.message() : null);
					}
					minioGenericResponse.setErrors(errors);

					if (ObjectUtils.isEmpty(minioGenericResponse.getErrors())) {
						LOGGER.info("Success from Remove objects from Minio bucket:{}", bucketName);
						minioGenericResponse.setStatus(ConstantsUtility.SUCCESS);
						minioGenericResponse.setHttpStatus(HttpStatus.OK);
					} else {
						LOGGER.error("Failure from Remove objects from Minio bucket:{}", bucketName);
						minioGenericResponse.setStatus(ConstantsUtility.FAILURE);
						minioGenericResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
						return minioGenericResponse;
					}

					// To remove bucket.
					LOGGER.info("Removing bucket:{}", bucketName);
					minioGenericResponse = removeBucket(userId, bucketName);
				} else {
					LOGGER.error("Bucket is not found" + bucketName);
					minioGenericResponse.setStatus(ConstantsUtility.FAILURE);
					minioGenericResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
					return minioGenericResponse;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error occurred while delete Bucket Cascade from minio: {}", e.getMessage());
			minioGenericResponse.setErrors(Arrays.asList(new ErrorDTO(null, e.getMessage())));
			minioGenericResponse.setStatus(ConstantsUtility.FAILURE);
			minioGenericResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return minioGenericResponse;
	}

	private BucketObjectVO getBucketObjectVOFromMinioObject(MinioObjectMetadata minioObject) {
		BucketObjectVO vo = new BucketObjectVO();
		vo.setIsLatest(false);
		vo.setOwner(null);
		vo.setStorageClass(null);
		vo.setUserMetadata(null);
		if(minioObject!=null && "SUCCESS".equalsIgnoreCase(minioObject.getStatus())) {
			vo.setEtag(minioObject.getEtag());
			vo.setIsDir("folder".equalsIgnoreCase(minioObject.getType()) ? true: false);
			String lastModifiedString = minioObject.getLastModified();
			vo.setLastModified("folder".equalsIgnoreCase(minioObject.getType()) ? null : lastModifiedString);
			vo.setObjectName(minioObject.getKey());
			vo.setSize("folder".equalsIgnoreCase(minioObject.getType()) ? null : minioObject.getSize());
			vo.setVersionId(minioPolicyVersion);
		}
		return vo;
	}
	
	@Override
	public MinioGenericResponse getBucketObjectsUsingMC(String userId, String bucketName, String prefix) {
		MinioGenericResponse minioObjectResponse = new MinioGenericResponse();
		List<BucketObjectVO> objects = new ArrayList<>();
		try {
			String userSecretKey ="";
			userId = minioAdminAccessKey;
			userSecretKey = minioAdminSecretKey;
			if (StringUtils.hasText(userSecretKey)) {
				LOGGER.info("Fetch secret from vault successfull for user:{}", userId);
				boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
				ObjectMapper mapper = new ObjectMapper();
				ProcessBuilder secondBuilder = new ProcessBuilder();
				String url = storageHttpMethod + storageConnectHost;
				String env = "storagebeminioclient";
				String flag = "--insecure";
				if(prefix==null || "null".equalsIgnoreCase(prefix) || "".equalsIgnoreCase(prefix)) {
					prefix="";
				}
				String secondCommand = "mc ls " + env + "/" + bucketName + "/" + prefix
						+ " --json " 
						+ flag;
	
				if (isWindows) {
					secondBuilder = new ProcessBuilder("cmd.exe", "/c", secondCommand);
				} else {
					secondBuilder = new ProcessBuilder(storageMCcommandKey, "-c", secondCommand);
				}
				Process p = secondBuilder.start();
				LOGGER.info("Started mc command to list bucket objects recursively");
				BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
				LOGGER.info("mc command to list objects was executed, started reading response");
				String line;
				String jsonprefix = "{\"data\":[";
				String suffix = "]}";
				String data = "";
				while (true) {
					line = r.readLine();
					if (line == null) {
						break;
					} else {
						data = data.concat(line).concat(",");
					}
				}
				LOGGER.info("finished reading response from mc list bucket objects");
				MinioObjectMetadataCollection listBucketObjectsCollectionDto = new MinioObjectMetadataCollection();
				if(data!=null && !"".equalsIgnoreCase(data)) { 
				data = jsonprefix.concat(data.substring(0, data.length() - 1)).concat(suffix);
				listBucketObjectsCollectionDto = mapper.readValue(data, MinioObjectMetadataCollection.class);
				LOGGER.info("Success from minio list bucket {} objects for user:{}", bucketName, userId);
				}else {
					LOGGER.info("Success from minio list bucket {} objects for user:{}. No data found, no objects present", bucketName, userId);
				}
				r.close();
				p.destroy();
				
				List<MinioObjectMetadata> minioObjects =  listBucketObjectsCollectionDto.getData();
				if(minioObjects!= null && !minioObjects.isEmpty()) {
					for(MinioObjectMetadata minioObject : minioObjects) {
						String key = prefix + minioObject.getKey();
						if("SUCCESS".equalsIgnoreCase(minioObject.getStatus())) {
							minioObject.setKey(key);
							BucketObjectVO bucketObj = this.getBucketObjectVOFromMinioObject(minioObject);
							objects.add(bucketObj);
						}
					}
				}
				minioObjectResponse.setHttpStatus(HttpStatus.OK);
				minioObjectResponse.setStatus("SUCCESS");
				minioObjectResponse.setObjects(objects);
				LOGGER.info("Success from minio list bucket {} objects setting to dto", bucketName);
			} else {
					LOGGER.info("User:{} not available in vault.", userId);
				}
			}
			catch (Exception e) {
				
				LOGGER.error("Error occured while listing bucket {} objects from minio using mc: {}",bucketName, e.getMessage());
				if (e.toString() != null && e.toString().contains("code=403")) {
					LOGGER.error("Access denied since no bucket available for user:{}", userId);
					minioObjectResponse.setStatus(ConstantsUtility.SUCCESS);
					minioObjectResponse.setHttpStatus(HttpStatus.NO_CONTENT);
					minioObjectResponse.setObjects(objects);
					LOGGER.info("No bucket available");
				} else {
					minioObjectResponse
							.setErrors(Arrays.asList(new ErrorDTO(null, "Error occured while listing bucket objects from minio. Please retry after sometime.")));
					minioObjectResponse.setStatus(ConstantsUtility.FAILURE);
					minioObjectResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
					minioObjectResponse.setObjects(objects);
				}
			}
		return minioObjectResponse;
	}
	
	@Override
	public MinioGenericResponse setBucketPublicDownloadUsingMc(String bucketName, boolean isEnablePublicAccess) {
		MinioGenericResponse minioObjectResponse = new MinioGenericResponse();
		try{
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			ProcessBuilder firstBuilder = new ProcessBuilder();
			String env = "storagebeminioclient";
			String flag = "--insecure";
			if(isEnablePublicAccess){
				String firstCommand = "mc anonymous set download " + env + "/" + bucketName +" "+ flag; 
				if (isWindows) {
					firstBuilder = new ProcessBuilder("cmd.exe", "/c", firstCommand);
				} else {
					firstBuilder = new ProcessBuilder(storageMCcommandKey, "-c", firstCommand);
				}
				Process p = firstBuilder.start();
				LOGGER.info("Started mc command to set public download access to contents of the bucket");
				BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
				LOGGER.info("mc command to set public download access to the contents of bucket was executed and output is {}",r.readLine());
				r.close();
				p.destroy();
			}else{
				String firstCommand = "mc anonymous set none " + env + "/" + bucketName + " " + flag; 
				if (isWindows) {
					firstBuilder = new ProcessBuilder("cmd.exe", "/c", firstCommand);
				} else {
					firstBuilder = new ProcessBuilder(storageMCcommandKey, "-c", firstCommand);
				}
				Process p = firstBuilder.start();
				LOGGER.info("Started mc command to set default access to contents of the bucket");
				BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
				LOGGER.info("mc command to set default access to the contents of bucket was executed and output is {}",r.readLine());
				r.close();
				p.destroy();
			}
			minioObjectResponse.setHttpStatus(HttpStatus.OK);
			minioObjectResponse.setStatus(ConstantsUtility.SUCCESS);

		} catch(Exception e) {
			LOGGER.error("Error in setting public download access to the bucket {} ",bucketName,e.getMessage());
			minioObjectResponse.setStatus(ConstantsUtility.FAILURE);
			minioObjectResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return minioObjectResponse;
	}
	
	@Override
	public MinioGenericResponse getBucketObjects(String userId, String bucketName, String prefix) {
		MinioGenericResponse minioObjectResponse = new MinioGenericResponse();

		try {
			LOGGER.debug("Fetching secrets from vault for user:{}", userId);
			String userSecretKey =  vaultConfig.validateUserInVault(userId);
			if(StringUtils.hasText(userSecretKey)) {
				LOGGER.debug("Fetch secret from vault successfull for user:{}",userId);
				MinioClient minioClient = MinioClient.builder().endpoint(minioBaseUri).credentials(userId, userSecretKey)
						.build();

				// Lists objects information.
				LOGGER.debug("Listing Objects information from minio for user:{}", userId);
				Iterable<Result<Item>> results = minioClient
						.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).build());

				List<BucketObjectVO> objects = new ArrayList<>();
				BucketObjectVO bucketObjectVO = null;
				for (Result<Item> result : results) {
					bucketObjectVO = new BucketObjectVO();
					Item item = result.get();
					LOGGER.debug("Got details for object:{}",item.objectName()!=null?item.objectName():null);
					bucketObjectVO.setEtag(item.etag());
					bucketObjectVO.setIsLatest(item.isLatest());
					bucketObjectVO.setObjectName(item.objectName());
					bucketObjectVO.setOwner(item.owner() != null ? item.owner().displayName() : null);
					bucketObjectVO.setStorageClass(item.storageClass());
					bucketObjectVO.setVersionId(item.versionId());

					if (item.isDir()) {
						bucketObjectVO.setIsDir(item.isDir());
					} else {
						bucketObjectVO.setLastModified(item.lastModified().toString());
						bucketObjectVO.setSize(item.size());
						bucketObjectVO.setIsDir(false);
					}

					objects.add(bucketObjectVO);

				}

				minioObjectResponse.setObjects(objects);
				minioObjectResponse.setStatus(ConstantsUtility.SUCCESS);
				minioObjectResponse.setHttpStatus(HttpStatus.OK);
			}else {
				LOGGER.info("Fetch secret from vault failed for user:{} , while getting bucket objects",userId);
				minioObjectResponse.setErrors(Arrays.asList(new ErrorDTO(null, "Fetch secret from vault failed for user:"+userId)));
				minioObjectResponse.setStatus(ConstantsUtility.FAILURE);
				minioObjectResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
			}
		} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
				| InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException
				| XmlParserException | IOException e) {
			LOGGER.error("Error occured while listing bucket's object from minio: {}",
					e.getMessage());
			minioObjectResponse.setErrors(Arrays.asList(new ErrorDTO(null, e.getMessage())));
			minioObjectResponse.setStatus(ConstantsUtility.FAILURE);
			minioObjectResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return minioObjectResponse;
	}

	@Override
	public MinioGenericResponse onboardUserMinio(String userId, List<String> policies) {
		MinioGenericResponse minioResponse = new MinioGenericResponse();
		String userSecretKey = "";
		try {
			MinioAdminClient minioAdminClient = minioConfig.getMinioAdminClient();
			LOGGER.debug("Validating user:{} in minio", userId);

			LOGGER.debug("List all minio users.");
			// To fetch user list from cache minioUsersCache
			Map<String, UserInfo> users = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
			if (users.containsKey(userId)) {
				LOGGER.info("User: {} already exists", userId);
				// Fetching user info
				UserInfo userInfo = users.get(userId);
				// Fetching user policies
				String existingPolicy = userInfo.policyName()!=null?userInfo.policyName():"";

				// Validating user in Vault
				LOGGER.debug("Validating user in Vault.");
				userSecretKey = vaultConfig.validateUserInVault(userId);
				if (!StringUtils.hasText(userSecretKey)) {
					LOGGER.warn("User:{} not available in vault.", userId);
				}
				// Adding new policies to existing one
				for (String policy : policies) {
					String policyResponse = this.attachPolicyToUser(userId, policy, false);
					LOGGER.info("mc attach policy response: "+ policyResponse);
					existingPolicy = StorageUtility.addPolicy(existingPolicy, policy);
				}

				// Setting new policy set to user
				// minioAdminClient.setPolicy(userId, false, existingPolicy);
				// LOGGER.info("Success from Minio set policy");

				// Update users list for minioUsersCache
				// updating policy
				UserInfo userInfoTemp = new UserInfo(userInfo.status(), userInfo.secretKey(), existingPolicy,
						userInfo.memberOf());
				users.put(userId, userInfoTemp);

			} else {
				// to build policies as comma separated
				String commaSeparatedPolicies = policies.stream().collect(Collectors.joining(","));

				userSecretKey = vaultConfig.validateUserInVault(userId);
				if (!StringUtils.hasText(userSecretKey)) {
					LOGGER.warn("User:{} not available in vault.", userId);
					LOGGER.debug("Creating SecretKey for user: {}", userId);
					userSecretKey = UUID.randomUUID().toString();
				}

				LOGGER.info("Onboarding user:{} to minio", userId);
				minioAdminClient.addUser(userId, Status.ENABLED, userSecretKey, commaSeparatedPolicies, null);

				// setting policy to user
				LOGGER.debug("Setting policy for user:{}", userId);
				//minioAdminClient.setPolicy(userId, false, commaSeparatedPolicies);
				String policyResponse = this.attachPolicyToUser(userId, commaSeparatedPolicies, false);
				LOGGER.info("mc attach policy response: "+ policyResponse);

				LOGGER.debug("Adding user: {} credentials to vault",userId);
				vaultConfig.addUserVault(userId, userSecretKey);

				LOGGER.info("User:{} Onboarded successfully.", userId);
				minioResponse.setStatus(ConstantsUtility.SUCCESS);

				// Update users list for minioUsersCache
				// updating policy
				UserInfo userInfoTemp = new UserInfo(Status.ENABLED, userSecretKey, commaSeparatedPolicies, null);
				users.put(userId, userInfoTemp);
			}
			// Building User Onboard response
			minioResponse.setStatus(ConstantsUtility.SUCCESS);
			minioResponse.setHttpStatus(HttpStatus.OK);
			UserVO userVO = new UserVO();
			userVO.setAccesskey(userId);
			userVO.setSecretKey(userSecretKey);
			minioResponse.setUser(userVO);

			// updating minioUsersCache
			LOGGER.debug("Removing all enteries from {}.", ConstantsUtility.MINIO_USERS_CACHE);
			cacheUtil.removeAll(ConstantsUtility.MINIO_USERS_CACHE);
			LOGGER.debug("Updating {}.", ConstantsUtility.MINIO_USERS_CACHE);
			cacheUtil.updateCache(ConstantsUtility.MINIO_USERS_CACHE, users);

		} catch (NoSuchAlgorithmException | InvalidKeyException | IOException | InvalidCipherTextException e) {
			LOGGER.error("Error occured while onboarding user to minio:{} ", e.getMessage());
			// Building response for user onboard failure
			minioResponse.setStatus(ConstantsUtility.FAILURE);
			minioResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			minioResponse
					.setErrors(Arrays.asList(new ErrorDTO(null, "Error occurred while onboarding user: " + userId)));

		}
		return minioResponse;
	}

	@Override
	public MinioGenericResponse userRefresh(String userId) {
		MinioGenericResponse minioResponse = new MinioGenericResponse();
		try {
			MinioAdminClient minioAdminClient = minioConfig.getMinioAdminClient();
			LOGGER.debug("Fetching user info from minio: {}", userId);
			Map<String, UserInfo> users = this.listUsers();
			// Creating new secret key for user
			String userSecretKey = UUID.randomUUID().toString();

			if (users.containsKey(userId)) {
				UserInfo userInfo = users.get(userId);

				LOGGER.debug("Setting new secret key for existing user:{} to minio.", userId);
				minioAdminClient.addUser(userId, Status.ENABLED, userSecretKey, userInfo.policyName(),
						userInfo.memberOf());

			} else {
				LOGGER.info("Adding new user:{} to minio.", userId);
				minioAdminClient.addUser(userId, Status.ENABLED, userSecretKey, null, null);
			}

			LOGGER.info("Adding user to vault:{}", userId);
			vaultConfig.addUserVault(userId, userSecretKey);

			UserVO userVO = new UserVO();
			userVO.setAccesskey(userId);
			userVO.setSecretKey(userSecretKey);
			minioResponse.setUser(userVO);
			minioResponse.setStatus(ConstantsUtility.SUCCESS);

		} catch (NoSuchAlgorithmException | InvalidKeyException | IOException | InvalidCipherTextException e) {
			e.printStackTrace();
			LOGGER.error("Error occured while refreshing user to minio:{} ", e.getMessage());
			minioResponse.setStatus(ConstantsUtility.FAILURE);
			minioResponse.setErrors(Arrays.asList(new ErrorDTO(null, "Error occured while refreshing user to minio: " + userId)));
		}

		return minioResponse;
	}
	
	@Override
	public PermissionVO getBucketPermission(String bucketName, String currentUser){
		PermissionVO permissionVO = null;
		LOGGER.debug("Getting minio users from ehcache: {}",ConstantsUtility.MINIO_USERS_CACHE);
		Map<String, UserInfo> usersInfo = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
		UserInfo userInfo = usersInfo.get(currentUser);
		if(Objects.nonNull(userInfo)) {
			//Setting permission for bucket
			permissionVO = new PermissionVO();
			if (userInfo.policyName().contains(bucketName + "_" + ConstantsUtility.READ)) {
				LOGGER.debug("User:{} has read access to bucket:{}",currentUser,bucketName);
				permissionVO.setRead(true);
				permissionVO.setWrite(false);
			}
			if(userInfo.policyName().contains(bucketName + "_" + ConstantsUtility.READWRITE)) {
				LOGGER.debug("User:{} has read/write access to bucket:{}",currentUser,bucketName);
				permissionVO.setRead(true);
				permissionVO.setWrite(true);
			}
		}
		return permissionVO;
	}
	
	@Override
	public List<UserVO> getBucketCollaborators(String bucketName, String currentUser) {
		List<UserVO> bucketCollaborators = null;
		// Getting minio users from ehcache minioUsersCache
		LOGGER.debug("Getting minio users from ehcache: {}",ConstantsUtility.MINIO_USERS_CACHE);
		Map<String, UserInfo> usersInfo = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
		if (!usersInfo.isEmpty()) {
			LOGGER.debug("Got user info from {}.",ConstantsUtility.MINIO_USERS_CACHE);
			bucketCollaborators = new ArrayList<>();
			// Iterating over usersInfo map to get collaborators
			for (var entry : usersInfo.entrySet()) {
				if (StringUtils.hasText(entry.getValue().policyName())) {
					UserVO userVO = new UserVO();
					PermissionVO permissionVO = new PermissionVO();
					if (StorageUtility.hasText(entry.getValue().policyName(), bucketName + "_" + ConstantsUtility.READ)) {
						LOGGER.debug("User:{} has read access to bucket:{}", entry.getKey(), bucketName);
						// Setting accesskey
						userVO.setAccesskey(entry.getKey());
						// Setting permission
						permissionVO.setRead(true);
						permissionVO.setWrite(false);
						userVO.setPermission(permissionVO);

					}
					if (StorageUtility.hasText(entry.getValue().policyName(), bucketName + "_" + ConstantsUtility.READWRITE)) {
						LOGGER.debug("User:{} has read/write access to bucket:{}", entry.getKey(), bucketName);
						// Setting accesskey
						userVO.setAccesskey(entry.getKey());
						// Setting permission
						permissionVO.setRead(true);
						permissionVO.setWrite(true);
						userVO.setPermission(permissionVO);
					}
					if (!ObjectUtils.isEmpty(userVO.getAccesskey())) {
						LOGGER.debug("Setting Collaborator as user:{} has access to bucket:{}", entry.getKey(),
								bucketName);
						userVO.setPermission(permissionVO);
						bucketCollaborators.add(userVO);
					}
				}
			}
		}
		return bucketCollaborators;
	}
	
	@Override
	public Map<String, UserInfo> listUsers() {
		Map<String, UserInfo> users = null;
		try {
			MinioAdminClient minioAdminClient = minioConfig.getMinioAdminClient();
			LOGGER.debug("Fetching users info from minio.");
			users = minioAdminClient.listUsers();
		} catch (Exception e) {
			LOGGER.debug("listing user from minio Admin client is filed with Exception :{} retrying with MC client", e.getMessage());
			try {
				boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
				ObjectMapper mapper = new ObjectMapper();
				ProcessBuilder firstBuilder = new ProcessBuilder();
				ProcessBuilder secondBuilder = new ProcessBuilder();

				String url = storageHttpMethod + storageConnectHost;
				String env = "storagebeminioclient ";
				String flag = "--insecure";
				String firstCommand = "mc alias set " + env + url + " " + minioAdminAccessKey + " " + minioAdminSecretKey + " " + flag;
				String secondCommand = "mc admin user list " + env + " --json " + flag;

				if (isWindows) {
					firstBuilder = new ProcessBuilder("cmd.exe", "/c", firstCommand);
					secondBuilder = new ProcessBuilder("cmd.exe", "/c", secondCommand);
				} else {
					firstBuilder = new ProcessBuilder(storageMCcommandKey, "-c", firstCommand);
					secondBuilder = new ProcessBuilder(storageMCcommandKey, "-c", secondCommand);
				}

				firstBuilder.redirectErrorStream(true);
				Process p2 = firstBuilder.start();
				BufferedReader r2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
				
				LOGGER.info("minio mc client alias command output is {}",r2.readLine());

				//if(!p2.waitFor(Integer.parseInt(storageMCAliasCmdTimeout), TimeUnit.SECONDS)) {
				//    p2.destroy();
				//}
				
				Process p = secondBuilder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

				//if(!p.waitFor(Integer.parseInt(storageMCListPoliciesCmdTimeout), TimeUnit.SECONDS)) {
				//    p.destroy();
				//}
				
				String line;
				String prefix = "{\"data\":[";
				String suffix = "]}";
				String data = "";
				while (true) {
					line = r.readLine();
					if (line == null) {
						break;
					} else {
						data = data.concat(line).concat(",");
					}
				}
				data = prefix.concat(data.substring(0, data.length() - 1)).concat(suffix);
				users = new HashMap<>();
				List<UserInfoDto> userInfoDto = new ArrayList<>();
				LOGGER.info("Policies data from minio to update cache is {} ", data);
				UserInfoWrapperDto userInfoWrapperDto = mapper.readValue(data, UserInfoWrapperDto.class);
				userInfoDto = userInfoWrapperDto.getData();
				if (userInfoDto != null && !userInfoDto.isEmpty()) {
					for (int i = 0; i < userInfoDto.size(); i++) {
						UserInfoDto sample = userInfoDto.get(i);
						UserInfo tempUser = new UserInfo(Status.ENABLED, null, sample.getPolicyName(), null);
						users.put(sample.getAccessKey(), tempUser);
						LOGGER.debug("User info details: Access key: {}, policy Name:{}", sample.getAccessKey(), sample.getPolicyName());
					}
				}
			} catch (Exception exception) {
				LOGGER.error("Error occurred while listing users from Minio:{}", exception.getMessage());
			}
		}
		return users;
	}

	@Override
	public Boolean validateUserInMinio(String userId) {
		Boolean isUserExist = false;
		// Getting minio users from ehcache minioUsersCache
		LOGGER.debug("Getting minio users from ehcache: {}",ConstantsUtility.MINIO_USERS_CACHE);
		Map<String, UserInfo> usersInfo = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
		if (usersInfo.containsKey(userId)) {
			isUserExist = true;
		}
		return isUserExist;
	}


	// To create bucket policy
	private void createBucketPolicy(String policyName, String version, String resource, String action, String effect,
			String sid) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
		MinioAdminClient minioAdminClient = minioConfig.getMinioAdminClient();
		// Creating policy in minio
		LOGGER.debug("Creating {} Bucket policy", policyName);
		String policy = PolicyUtility.policyGenerator(resource, version, action, sid, effect);
		minioAdminClient.addCannedPolicy(policyName, policy);
	}

	@Override
	public MinioGenericResponse removeObjects(String userId, String bucketName, String prefix) {
		MinioGenericResponse minioGenericResponse = new MinioGenericResponse();
		try {
			LOGGER.info("Fetching secrets from vault for user:{}", userId);
			String userSecretKey = vaultConfig.validateUserInVault(userId);
			if (StringUtils.hasText(userSecretKey)) {
				LOGGER.debug("Fetch secret from vault successful for user:{}", userId);
				MinioClient minioClient = MinioClient.builder().endpoint(minioBaseUri).credentials(userId, userSecretKey)
						.build();
				LOGGER.debug("Setting object list to be deleted");
				List<DeleteObject> objects = new LinkedList<>();

				// To get list of Objects In Bucket.
				List<String> objectNames = listObjectsInBucket(userId, bucketName);

				if (StringUtils.hasText(prefix)) {
					String decodePrefix = URLDecoder.decode(prefix, StandardCharsets.UTF_8.toString());
					String[] prefixValue = decodePrefix.split(",", 0);
					for (String path : prefixValue) {
						// Check if the path is a folder (ends with '/')
						if (path.endsWith("/")) {
							// Fetch and mark all objects with the prefix 'path' for deletion
							Iterable<Result<Item>> versions = minioClient.listObjects(
									ListObjectsArgs.builder().bucket(bucketName).prefix(path).recursive(true).includeVersions(true).build()
							);
							for (Result<Item> versionResult : versions) {
								try {
									Item versionItem = versionResult.get();
									objects.add(new DeleteObject(versionItem.objectName(), versionItem.versionId()));
									LOGGER.info("Marked for deletion: {}", versionItem.objectName());
								} catch (Exception e) {
									LOGGER.error("Error fetching version item: {}", e.getMessage());
								}
							}
						} else {
							// If it's not a folder, apply the logic for specific object deletion
							for (String objectName : objectNames) {
								if (path.equals(objectName) || objectName.startsWith(path + "/")) {
									Iterable<Result<Item>> versions = minioClient.listObjects(
											ListObjectsArgs.builder().bucket(bucketName).prefix(objectName).includeVersions(true).build()
									);
									for (Result<Item> versionResult : versions) {
										try {
											Item versionItem = versionResult.get();
											objects.add(new DeleteObject(objectName, versionItem.versionId()));
										} catch (Exception e) {
											LOGGER.error("Error fetching version item: {}", e.getMessage());
										}
									}
								}
							}
						}
					}
				}

				LOGGER.info("Removing objects from Minio bucket:{}", bucketName);
				Iterable<Result<DeleteError>> results = minioClient
						.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());

				List<ErrorDTO> errors = new ArrayList<>();
				for (Result<DeleteError> result : results) {
					DeleteError deleteError = result.get();
					ErrorDTO error = new ErrorDTO(null,
							"Error in deleting object " + deleteError.objectName() + ": " + deleteError.message());
					errors.add(error);
					LOGGER.info("Error in deleting object {}: {}",
							deleteError.objectName() != null ? deleteError.objectName() : null,
							deleteError.message() != null ? deleteError.message() : null);
				}
				minioGenericResponse.setErrors(errors);

				if (ObjectUtils.isEmpty(minioGenericResponse.getErrors())) {
					LOGGER.info("Success from Remove objects from Minio bucket:{}", bucketName);
					minioGenericResponse.setStatus(ConstantsUtility.SUCCESS);
					minioGenericResponse.setHttpStatus(HttpStatus.OK);
				} else {
					LOGGER.info("Failure from Remove objects from Minio bucket:{}", bucketName);
					minioGenericResponse.setStatus(ConstantsUtility.FAILURE);
					minioGenericResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
				}
			} else {
				LOGGER.debug("Fetch secret from vault failed for user:{}", userId);
				minioGenericResponse.setErrors(Arrays.asList(new ErrorDTO(null, "Fetch secret from vault failed for user:" + userId)));
				minioGenericResponse.setStatus(ConstantsUtility.FAILURE);
				minioGenericResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
			}
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				 | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				 | IllegalArgumentException | IOException e) {
			LOGGER.error("Error occurred while removing object from Minio:{} ", e.getMessage());
			minioGenericResponse.setStatus(ConstantsUtility.FAILURE);
			minioGenericResponse
					.setErrors(Arrays.asList(new ErrorDTO(null, "Error occurred while removing object from Minio: " + e.getMessage())));
		}

		return minioGenericResponse;
	}

	@Override
	public MinioGenericResponse removeBucket(String userId, String bucketName) {
		MinioGenericResponse minioGenericResponse = new MinioGenericResponse();
		try {
			LOGGER.info("Fetching secrets from vault for user:{}", userId);
			String userSecretKey = vaultConfig.validateUserInVault(userId);
			if(StringUtils.hasText(userSecretKey)) {
				LOGGER.debug("Fetch secret from vault successful for user:{}", userId);
				MinioClient minioClient = MinioClient.builder().endpoint(minioBaseUri).credentials(userId, userSecretKey)
						.build();

				// Delete all object versions
				Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).includeVersions(true).recursive(true).build());

				// Prepare DeleteObjects to delete
				List<DeleteObject> deleteObjects = new LinkedList<>();
				for (Result<Item> result : results) {
					Item item = result.get();
					deleteObjects.add(new DeleteObject(item.objectName(), item.versionId()));
				}

				// Delete the objects and check for errors
				Iterable<Result<DeleteError>> deleteResults = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(deleteObjects).build());
				for (Result<DeleteError> result : deleteResults) {
					DeleteError error = result.get();
					LOGGER.error("Error deleting object: " + error.objectName() + ", version: "  + ", message: " + error.message());
				}

				// Check if bucket is empty before deleting
				Iterable<Result<Item>> checkResults = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).includeVersions(true).recursive(true).build());
				if (!checkResults.iterator().hasNext()) {
					// No objects or versions remain, safe to delete bucket
					LOGGER.info("Removing bucket:{} from Minio", bucketName);
					minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
					LOGGER.info("Success from Minio remove Bucket:{}", bucketName);
				} else {
					LOGGER.error("Objects or versions still remain in bucket, cannot delete.");
					minioGenericResponse.setErrors(Arrays.asList(new ErrorDTO(null, "Objects or versions still remain in bucket, cannot delete for user" + userId)));
					minioGenericResponse.setStatus(ConstantsUtility.FAILURE);
					minioGenericResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
					return minioGenericResponse;
				}

				LOGGER.info("Removing policies for bucket:{}", bucketName);
				List<String> policies = Arrays.asList(bucketName + "_" + ConstantsUtility.READ, bucketName + "_" + ConstantsUtility.READWRITE);
				deletePolicy(policies);
				
				minioGenericResponse.setStatus(ConstantsUtility.SUCCESS);
				minioGenericResponse.setHttpStatus(HttpStatus.OK);
				
			} else {
				LOGGER.debug("Fetch secret from vault failed for user:{}", userId);
				minioGenericResponse.setErrors(Arrays.asList(new ErrorDTO(null, "Fetch secret from vault failed for user:" + userId)));
				minioGenericResponse.setStatus(ConstantsUtility.FAILURE);
				minioGenericResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
			}
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				 | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				 | IllegalArgumentException | IOException e) {
			LOGGER.error("Error occurred while removing bucket from Minio:{} ", e.getMessage());
			minioGenericResponse.setStatus(ConstantsUtility.FAILURE);
			minioGenericResponse.setErrors(Arrays
					.asList(new ErrorDTO(null, e.getMessage())));
			minioGenericResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
		}

		return minioGenericResponse;
	}
	
	/*
	 * To remove policies. 
	 */
	private void deletePolicy(List<String> policies) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		// Getting MinioAdminClient from config
		MinioAdminClient minioAdminClient = minioConfig.getMinioAdminClient();
		LOGGER.debug("Fetching users from cache.");
		Map<String, UserInfo> users = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
		// Iterating over usersInfo map
		Iterator usersIterator = users.entrySet().iterator();
		while (usersIterator.hasNext()) {
			Entry userEntry = (Entry) usersIterator.next();
			// To fetch user access key
			String userId = (String) userEntry.getKey();
			// To fetch user info
			UserInfo userInfo = (UserInfo) userEntry.getValue();
			// To fetch user policy
			String userPolicy = userInfo.policyName();
			// To check if user has any policy
			if (StringUtils.hasText(userPolicy)) {
				// Iterating over policies
				for (String policy : policies) {
					if (StorageUtility.hasText(userPolicy, policy)) {
						userPolicy = StorageUtility.removePolicy(userPolicy, policy);
						if (StringUtils.hasText(userPolicy)) {
							LOGGER.info("Unlinking policy from user:{}", userId);
							//minioAdminClient.setPolicy(userId, false, userPolicy);
							String policyResponse = this.detachPolicyFromUser(userId, policy, false);
							LOGGER.info("mc detach policy response: "+ policyResponse);
							// Update user's policy in minio cache
							UserInfo userInfoTemp = new UserInfo(Status.ENABLED, userInfo.secretKey(), userPolicy,
									userInfo.memberOf());
							users.put(userId, userInfoTemp);
						} else {
							LOGGER.info("Removing user:{} from minio", userId);
							minioAdminClient.deleteUser(userId);
							//Removing entry from minio cache
							usersIterator.remove();
						}
						
					}
				}
			}
		}
		// updating minioUsersCache
		LOGGER.debug("Removing all enteries from {}.", ConstantsUtility.MINIO_USERS_CACHE);
		cacheUtil.removeAll(ConstantsUtility.MINIO_USERS_CACHE);
		LOGGER.debug("Updating {}.", ConstantsUtility.MINIO_USERS_CACHE);
		cacheUtil.updateCache(ConstantsUtility.MINIO_USERS_CACHE, users);
	}

	@Override
	public void deleteUser(String userId) {
		MinioAdminClient minioAdminClient = minioConfig.getMinioAdminClient();
		LOGGER.debug("Fetching users from cache.");
		Map<String, UserInfo> users = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
		try {
			LOGGER.info("Deleting user record {} from minio", userId);
			minioAdminClient.deleteUser(userId);
			
			LOGGER.info("Deleting user minioPolicies {} from cache", userId);
			users.remove(userId);
			
			// updating minioUsersCache
			LOGGER.debug("Removing all enteries from {}.", ConstantsUtility.MINIO_USERS_CACHE);
			cacheUtil.removeAll(ConstantsUtility.MINIO_USERS_CACHE);
			LOGGER.debug("Updating {}.", ConstantsUtility.MINIO_USERS_CACHE);
			cacheUtil.updateCache(ConstantsUtility.MINIO_USERS_CACHE, users);

		} catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
			LOGGER.error("Error occured while deleting policy for user:{}", userId);
		}
	}
	
	@Override
	public void setPolicy(String userOrGroupName, boolean isGroup, String policyName) {
		// Getting MinioAdminClient from config
		MinioAdminClient minioAdminClient = minioConfig.getMinioAdminClient();
		LOGGER.debug("Fetching users from cache.");
		Map<String, UserInfo> users = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
		try {
			LOGGER.debug("Updating policy for user:{}", userOrGroupName);
			// minioAdminClient.setPolicy(userOrGroupName, isGroup, policyName);
			// LOGGER.info("Success from minio set policy");
			String policyResponse = this.attachPolicyToUser(userOrGroupName, policyName, false);
			LOGGER.info("mc attach policy response: "+ policyResponse);

			// updating cache
			UserInfo userInfo = users.get(userOrGroupName);
			UserInfo userInfoTemp = new UserInfo(userInfo.status(), userInfo.secretKey(), policyName,
					userInfo.memberOf());
			users.put(userOrGroupName, userInfoTemp);

			// updating minioUsersCache
			LOGGER.debug("Removing all enteries from {}.", ConstantsUtility.MINIO_USERS_CACHE);
			cacheUtil.removeAll(ConstantsUtility.MINIO_USERS_CACHE);
			LOGGER.debug("Updating {}.", ConstantsUtility.MINIO_USERS_CACHE);
			cacheUtil.updateCache(ConstantsUtility.MINIO_USERS_CACHE, users);

		} catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
			LOGGER.error("Error occured while updating policy for user:{}", userOrGroupName);
		}
	}

	@Override
	public Map<String,String> getUri(String userId, String bucketName, String object) {
		Map<String,String> bucketConnectionUri = new HashMap<>();
		//Setting URI
		bucketConnectionUri.put(ConstantsUtility.URI, minioBaseUri+"/"+"buckets/"+bucketName);
		//Setting host name followed by port
		String hostName = minioBaseUri.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");
		bucketConnectionUri.put(ConstantsUtility.HOSTNAME, hostName);
		return bucketConnectionUri;
	}

	@Override
	public String attachPolicyToUser(String userId, String policyName, boolean isAdmin) {
		try {
			String userSecretKey = "";

			// Use admin credentials if the user is an admin
			if (isAdmin) {
				userId = minioAdminAccessKey;
				userSecretKey = minioAdminSecretKey;
			} else {
				LOGGER.debug("Fetching secrets from vault for user: {}", userId);
				userSecretKey = vaultConfig.validateUserInVault(userId);
			}

			if (!StringUtils.hasText(userSecretKey)) {
				LOGGER.error("User: {} not available in vault or secret key is empty.", userId);
				return "User not available or secret key is empty.";
			}

			LOGGER.info("Fetched secret from vault successfully for user: {}", userId);

			// Construct the mc commands
			String env = "storagebeminioclient";
			String flag = "--insecure";
			String url = storageHttpMethod + storageConnectHost;

			// Set alias command
			String setAliasCommand = String.format("mc alias set %s %s %s %s %s",
					env, url, minioAdminAccessKey, minioAdminSecretKey, flag);

			// Attach policy command
			String attachPolicyCommand = String.format("mc admin policy attach %s %s --user=%s %s",
					env, policyName, userId,flag);

			// Execute the commands
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			ProcessBuilder aliasBuilder = new ProcessBuilder(isWindows ? "cmd.exe" : "sh", isWindows ? "/c" : "-c", setAliasCommand);
			ProcessBuilder policyBuilder = new ProcessBuilder(isWindows ? "cmd.exe" : "sh", isWindows ? "/c" : "-c", attachPolicyCommand);

			// Execute alias command
			Process aliasProcess = aliasBuilder.start();
			int aliasExitCode = aliasProcess.waitFor();
			if (aliasExitCode != 0) {
				LOGGER.error("Failed to set alias. Exit code: {}", aliasExitCode);
				return "Failed to set alias.";
			}
			LOGGER.debug("Alias set successfully for user: {}", userId);

			// Execute policy command
			policyBuilder.redirectErrorStream(true);
			Process policyProcess = policyBuilder.start();

			// Read the output of the policy command
			StringBuilder output = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(policyProcess.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
				}
			}

			int policyExitCode = policyProcess.waitFor();
			LOGGER.debug("Process exited with code: {}", policyExitCode);
			LOGGER.debug("Response from mc: {}", output.toString());

			if (policyExitCode != 0) {
				LOGGER.error("Failed to attach policy. Exit code: {}", policyExitCode);
				return "Failed to attach policy.";
			}

			return "Policy attached successfully to user: " + userId;
		} catch (Exception e) {
			LOGGER.error("Error occurred while attaching policy in MinIO using mc: {}", e.getMessage(), e);
			return "Error attaching policy: " + e.getMessage();
		}
	}

	@Override
	public String detachPolicyFromUser(String userId, String policyName, boolean isAdmin) {
		try {
			String userSecretKey = "";

			// Use admin credentials if the user is an admin
			if (isAdmin) {
				userId = minioAdminAccessKey;
				userSecretKey = minioAdminSecretKey;
			} else {
				LOGGER.debug("Fetching secrets from vault for user: {}", userId);
				userSecretKey = vaultConfig.validateUserInVault(userId);
			}

			if (!StringUtils.hasText(userSecretKey)) {
				LOGGER.error("User: {} not available in vault or secret key is empty.", userId);
				return "User not available or secret key is empty.";
			}

			LOGGER.info("Fetched secret from vault successfully for user: {}", userId);

			// Construct the mc commands
			String env = "storagebeminioclient";
			String flag = "--insecure";
			String url = storageHttpMethod + storageConnectHost;

			// Set alias command
			String setAliasCommand = String.format("mc alias set %s %s %s %s %s",
					env, url, minioAdminAccessKey, minioAdminSecretKey, flag);

			// Detach policy command
			String detachPolicyCommand = String.format("mc admin policy detach %s %s --user=%s %s",
					env, policyName, userId, flag);

			// Execute the commands
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			ProcessBuilder aliasBuilder = new ProcessBuilder(isWindows ? "cmd.exe" : "sh", isWindows ? "/c" : "-c", setAliasCommand);
			ProcessBuilder policyBuilder = new ProcessBuilder(isWindows ? "cmd.exe" : "sh", isWindows ? "/c" : "-c", detachPolicyCommand);

			// Execute alias command
			Process aliasProcess = aliasBuilder.start();
			int aliasExitCode = aliasProcess.waitFor();
			if (aliasExitCode != 0) {
				LOGGER.error("Failed to set alias. Exit code: {}", aliasExitCode);
				return "Failed to set alias.";
			}
			LOGGER.debug("Alias set successfully for user: {}", userId);

			// Execute policy command
			policyBuilder.redirectErrorStream(true);
			Process policyProcess = policyBuilder.start();

			// Read the output of the policy command
			StringBuilder output = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(policyProcess.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
				}
			}

			int policyExitCode = policyProcess.waitFor();
			LOGGER.debug("Process exited with code: {}", policyExitCode);
			LOGGER.debug("Response from mc: {}", output.toString());

			if (policyExitCode != 0) {
				LOGGER.error("Failed to detach policy. Exit code: {}", policyExitCode);
				return "Failed to detach policy.";
			}

			return "Policy detached successfully from user: " + userId;
		} catch (Exception e) {
			LOGGER.error("Error occurred while detaching policy in MinIO using mc: {}", e.getMessage(), e);
			return "Error detaching policy: " + e.getMessage();
		}
	}


	
}
