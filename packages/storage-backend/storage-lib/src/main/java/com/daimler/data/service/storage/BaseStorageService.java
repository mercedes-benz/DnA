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

package com.daimler.data.service.storage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.config.MalwareScannerClient;
import com.daimler.data.application.config.VaultConfig;
import com.daimler.data.assembler.StorageAssembler;
import com.daimler.data.auth.client.DnaAuthClient;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dataiku.client.DataikuClient;
import com.daimler.data.db.entities.StorageNsql;
import com.daimler.data.db.jsonb.Storage;
import com.daimler.data.db.repo.storage.IStorageRepository;
import com.daimler.data.db.repo.storage.StorageRepository;
import com.daimler.data.dto.DataikuConnectionRequestDTO;
import com.daimler.data.dto.DataikuGenericResponseDTO;
import com.daimler.data.dto.DataikuIndexDTO;
import com.daimler.data.dto.DataikuParameterDTO;
import com.daimler.data.dto.DataikuPermission;
import com.daimler.data.dto.DataikuReadabilityDTO;
import com.daimler.data.dto.ErrorDTO;
import com.daimler.data.dto.FileScanDetailsVO;
import com.daimler.data.dto.McListBucketCollectionDto;
import com.daimler.data.dto.McListBucketDto;
import com.daimler.data.dto.MinioGenericResponse;
import com.daimler.data.dto.Permission;
import com.daimler.data.dto.UserInfoVO;
import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.data.dto.storage.BucketCollectionVO;
import com.daimler.data.dto.storage.BucketObjectResponseVO;
import com.daimler.data.dto.storage.BucketObjectResponseWrapperVO;
import com.daimler.data.dto.storage.BucketResponseWrapperVO;
import com.daimler.data.dto.storage.BucketVo;
import com.daimler.data.dto.storage.ConnectionResponseVO;
import com.daimler.data.dto.storage.ConnectionResponseWrapperVO;
import com.daimler.data.dto.storage.ConnectionVO;
import com.daimler.data.dto.storage.CreatedByVO;
import com.daimler.data.dto.storage.PermissionVO;
import com.daimler.data.dto.storage.UserRefreshWrapperVO;
import com.daimler.data.dto.storage.UserVO;
import com.daimler.data.minio.client.DnaMinioClient;
import com.daimler.data.util.ConstantsUtility;
import com.daimler.data.util.RedisCacheUtil;
import com.daimler.data.util.StorageUtility;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;

import io.minio.admin.UserInfo;
import io.minio.messages.Bucket;

@Service
public class BaseStorageService implements StorageService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseStorageService.class);

	@Value("${minio.endpoint}")
	private String minioBaseUri;

	@Value("${storage.termsOfUse.uri}")
	private String storageTermsOfUseUri;

	@Value("${storage.connect.host}")
	private String storageConnectHost;

    @Autowired
	HttpServletRequest httpRequest;

	@Value("${databricks.userid}")
	private String dataBricksUser;

	@Value("${databricks.userauth}")
	private String dataBricksAuth;


	@Value("${minio.clientApi}")
	private String minioClientApi;

	@Autowired
	private RedisCacheUtil cacheUtil;

	@Autowired
	private UserStore userStore;

	@Autowired
	private DnaMinioClient dnaMinioClient;

	@Autowired
	private VaultConfig vaultConfig;

	@Value("${dna.feature.attachmentMalwareScan}")
	private Boolean attachmentMalwareScan;

	@Autowired
	private MalwareScannerClient malwareScannerClient;

	@Autowired
	private KafkaProducerService kafkaProducer;

	private static String bucketCreationEvent = "Storage - Bucket Creation";

	@Autowired
	private IStorageRepository jpaRepo;

	@Autowired
	private StorageRepository customRepo;

	@Autowired
	private StorageAssembler storageAssembler;

	@Autowired
	private DnaAuthClient dnaAuthClient;

	@Autowired
	private DataikuClient dataikuClient;

	@Value("${storage.technical.id}")
	private String technicalId;

	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.sss'Z'");

	public BaseStorageService() {
		super();
	}

	@Override
	@Transactional
	public ResponseEntity<BucketResponseWrapperVO> createBucket(BucketVo bucketVo) {
		BucketResponseWrapperVO responseVO = new BucketResponseWrapperVO();
		HttpStatus httpStatus;

		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		String ownerEmail = userStore.getUserInfo().getEmail();
		List<MessageDescription> warnings = new ArrayList<>();


			String chronosUserToken = httpRequest.getHeader("chronos-api-key");
			boolean authFlag = chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken);
			if(chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken)) {
				currentUser = dataBricksUser;
				CreatedByVO pidAsCreator = new CreatedByVO();
				pidAsCreator.setId(currentUser);
				pidAsCreator.setFirstName("Pool-ID");
				pidAsCreator.setLastName("CHRONOS_POOLUSER");
				bucketVo.setCreatedBy(pidAsCreator);
			}
			LOGGER.debug("authflag {} currentUser {}",authFlag,currentUser);



		PermissionVO permissionVO = null;

		LOGGER.debug("Validate Bucket before create.");
		List<MessageDescription> validateMsg = validateCreateBucket(bucketVo);
		if (!ObjectUtils.isEmpty(validateMsg)) {
			responseVO.setStatus(ConstantsUtility.FAILURE);
			responseVO.setErrors(validateMsg);
			httpStatus = HttpStatus.BAD_REQUEST;
		} else {
			LOGGER.debug("Make bucket:{} request for user:{}", bucketVo.getBucketName(), currentUser);
			MinioGenericResponse createBucketResponse = dnaMinioClient.createBucket(bucketVo.getBucketName());
			if (createBucketResponse != null && createBucketResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
				LOGGER.info("Success from make minio bucket");
				responseVO.setStatus(createBucketResponse.getStatus());
				LOGGER.debug("Onboarding current user:{}", currentUser);
				MinioGenericResponse onboardOwnerResponse = dnaMinioClient.onboardUserMinio(currentUser,
						createBucketResponse.getPolicies());
				if (onboardOwnerResponse != null && onboardOwnerResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
					LOGGER.info("Onboard bucket owner:{} successfull.", currentUser);
					// Setting R/W access for owner
					permissionVO = new PermissionVO();
					permissionVO.setRead(true);
					permissionVO.setWrite(true);

					UserVO ownerUserVO = onboardOwnerResponse.getUser();
					// Setting permission
					ownerUserVO.setPermission(permissionVO);
					// Setting uri
					Map<String, String> bucketConnectionUri = dnaMinioClient.getUri(currentUser,
							bucketVo.getBucketName(), null);
					ownerUserVO.setUri(bucketConnectionUri.get(ConstantsUtility.URI));
					String bucketUri = bucketVo.getBucketName();
					ownerUserVO.setHostName(bucketConnectionUri.get(ConstantsUtility.HOSTNAME));

					// Setting bucket access info for owner
					responseVO.setBucketAccessinfo(ownerUserVO);
					List<String> subscribedUsers = new ArrayList<>();
					subscribedUsers.add(currentUser);
					List<String> subscribedUsersEmails = new ArrayList<>();
					subscribedUsersEmails.add(ownerEmail);
                    if (currentUser.equalsIgnoreCase(dataBricksUser)) {
                        ownerUserVO.setFirstName(bucketVo.getCreatedBy().getFirstName());
                        ownerUserVO.setLastName(bucketVo.getCreatedBy().getLastName());
                    } else {
                        ownerUserVO.setFirstName(userStore.getUserInfo().getFirstName());
                        ownerUserVO.setLastName(userStore.getUserInfo().getLastName());
                        ownerUserVO.setEmail(userStore.getUserInfo().getEmail());
                        ownerUserVO.setDepartment(userStore.getUserInfo().getDepartment());
                        ownerUserVO.setMobileNumber(userStore.getUserInfo().getMobileNumber());
                    }

                    // Get the existing collaborators
                    List<UserVO> existingCollaborators = bucketVo.getCollaborators();

                    // If the existing collaborators list is null, initialize it.
                    if (existingCollaborators == null) {
                        existingCollaborators = new ArrayList<>();
                    }

                    // Add the owner collaborator to the list.
                    existingCollaborators.add(ownerUserVO);

                    // Set the updated list of collaborators on the bucketVo
                    bucketVo.setCollaborators(existingCollaborators);
					LOGGER.debug("Onboarding collaborators");
					if (!ObjectUtils.isEmpty(bucketVo.getCollaborators())) {
						for (UserVO userVO : bucketVo.getCollaborators()) {
							if (Objects.nonNull(userVO.getPermission())) {
								List<String> policies = new ArrayList<>();
								if (Boolean.TRUE.equals(userVO.getPermission().isRead())) {
									LOGGER.debug("Setting READ access.");
									policies.add(bucketVo.getBucketName() + "_" + ConstantsUtility.READ);
								}
								if (Boolean.TRUE.equals(userVO.getPermission().isWrite())) {
									LOGGER.debug("Setting READ/WRITE access.");
									policies.add(bucketVo.getBucketName() + "_" + ConstantsUtility.READWRITE);
								}

								LOGGER.debug("Onboarding collaborator:{}", userVO.getAccesskey());
								MinioGenericResponse onboardUserResponse = dnaMinioClient
										.onboardUserMinio(userVO.getAccesskey(), policies);
								if (onboardUserResponse != null
										&& onboardUserResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
									subscribedUsers.add(userVO.getAccesskey());
									subscribedUsersEmails.add(userVO.getEmail());
									LOGGER.info("Collaborator:{} onboarding successfull", userVO.getAccesskey());
								} else {
									LOGGER.info("Collaborator:{} onboarding failed", userVO.getAccesskey());
								}
							} else {
								LOGGER.info("Collaborator:{} onboarding not possible since permission is not given.",
										userVO.getAccesskey());
							}
						}
					}

					String eventType = bucketCreationEvent;
					this.publishEventMessages(eventType, bucketUri, null, bucketVo.getBucketName(), subscribedUsers,subscribedUsersEmails);

				} else {
					LOGGER.info("Failure from onboard bucket owner.");
				}
				if (bucketVo.isEnablePublicAccess() != null) {
					if(bucketVo.isEnablePublicAccess()){
						MinioGenericResponse minioObjectResponse = dnaMinioClient.setBucketPublicDownloadUsingMc(bucketVo.getBucketName(), bucketVo.isEnablePublicAccess());
						if(minioObjectResponse != null && minioObjectResponse.getStatus().equals(ConstantsUtility.SUCCESS)){
							LOGGER.info("Successfully set public download accsess to the storage bucket");
						}else{
							bucketVo.setEnablePublicAccess(false);
							LOGGER.info("Failed to set public download accsess to the storage bucket");
							MessageDescription msg = new MessageDescription("Failed to update the public access status of storage buckets");
							warnings.add(msg);
							responseVO.setWarnings(warnings);
						}
					}
				}
				//To save bucket info in db
				BucketVo savedBucketVo = saveBucket(bucketVo);
				bucketVo.setId(savedBucketVo.getId());

				responseVO.setStatus(createBucketResponse.getStatus());
				httpStatus = HttpStatus.OK;
			} else {
				LOGGER.info("Failure from make bucket minio client");
				responseVO.setStatus(ConstantsUtility.FAILURE);
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				responseVO.setErrors(getMessages(createBucketResponse.getErrors()));
			}
		}
		responseVO.setData(bucketVo);
		return new ResponseEntity<>(responseVO, httpStatus);
	}

	/*
	 * To save bucket info to database
	 *
	 */
	private BucketVo saveBucket(BucketVo requestbucketVo) {
		if (Objects.isNull(requestbucketVo.getCreatedBy())
				|| !StringUtils.hasText(requestbucketVo.getCreatedBy().getId())) {
			requestbucketVo.setCreatedBy(userStore.getVO());
		}
			requestbucketVo.setCreatedDate(new Date());
			requestbucketVo.setLastModifiedDate(new Date());
		LOGGER.debug("Converting to entity.");
		StorageNsql storageNsql = storageAssembler.toEntity(requestbucketVo);
		LOGGER.debug("Saving entity.");
		StorageNsql savedEntity = jpaRepo.save(storageNsql);
		return storageAssembler.toBucketVo(savedEntity);
	}


	private void publishEventMessages(String eventType, String bucketUri, List<ChangeLogVO> changeLogs, String bucketName,
			List<String> subscribedUsers, List<String> subscribedUsersEmail) {
		try {
			String message = "";
			String messageDetails = "";
			Boolean mailRequired = true;
			com.daimler.data.application.auth.UserStore.UserInfo currentUser = userStore.getUserInfo();
			String userId = currentUser.getId() != null ? currentUser.getId() : "dna_system";
			String userName = userId;
			if(currentUser!=null && currentUser.getFirstName()!= null) {
				userName = currentUser.getFirstName();
				if(currentUser.getLastName()!= null)
					userName = userName + " " + currentUser.getLastName();
			}

			/*
			 * if(subscribedUsers!=null && !subscribedUsers.isEmpty() &&
			 * subscribedUsers.contains(userId)) {
			 * LOGGER.info("Removed current userid from subscribedUsers");
			 * subscribedUsers.remove(userId); }
			 */

			if (bucketCreationEvent.equalsIgnoreCase(eventType)) {
				message = "Storage bucket:  " + bucketName + ", is created by user " + userName;
				messageDetails = "Please refer the link for [Terms of Use](" + storageTermsOfUseUri + ")";
				LOGGER.info("Publishing message on bucket creation for bucketname {} by userId {}", bucketName, userId);
			}
			if (eventType != null && eventType != "") {
					kafkaProducer.send(eventType, bucketUri, messageDetails, userId, message, mailRequired, subscribedUsers,subscribedUsersEmail,changeLogs);
					LOGGER.info("Published event bucket-creation for bucketname {} by userId {}, for all collaborators {}", bucketName, userId,Arrays.toString(subscribedUsers.toArray()));
			}
		} catch (Exception e) {
			LOGGER.trace("Failed while publishing storage event msg {} ", e.getMessage());
		}
	}

	/*
	 * To validate create bucket.
	 *
	 */
	private List<MessageDescription> validateCreateBucket(BucketVo bucketVo) {
		List<MessageDescription> messages = new ArrayList<>();
		MessageDescription message = null;
		LOGGER.debug("Check if bucket already exists.");
		Boolean isBucketExists = dnaMinioClient.isBucketExists(bucketVo.getBucketName());
		if (isBucketExists == null) {
			message = new MessageDescription();
			message.setMessage("Error occurred while validating bucket: " + bucketVo.getBucketName());
			messages.add(message);
		} else if (isBucketExists) {
			LOGGER.info("Bucket already exists: {}", bucketVo.getBucketName());
			message = new MessageDescription();
			message.setMessage("Bucket already exists: " + bucketVo.getBucketName());
			messages.add(message);
		} else {
			LOGGER.info("Bucket not exists proceed to make new bucket.");
		}

		return messages;
	}

	/*
	 * To validate update bucket.
	 *
	 */
	private List<MessageDescription> validateUpdateBucket(BucketVo bucketVo) {
		List<MessageDescription> messages = new ArrayList<>();
		MessageDescription message = null;
		LOGGER.debug("Check if bucket already exists.");
		Boolean isBucketExists = dnaMinioClient.isBucketExists(bucketVo.getBucketName());
		if (isBucketExists == null) {
			message = new MessageDescription();
			message.setMessage("Error occurred while validating bucket: " + bucketVo.getBucketName());
			messages.add(message);
		} else if (!isBucketExists) {
			LOGGER.info("Bucket:{} not found.", bucketVo.getBucketName());
			message = new MessageDescription();
			message.setMessage("Bucket:{} "+bucketVo.getBucketName()+ "not found");
			messages.add(message);
		} else {
			LOGGER.info("Bucket:{} exists.",bucketVo.getBucketName());
		}

		return messages;
	}

	@Override
	public ResponseEntity<BucketCollectionVO> getAllBuckets(int limit, String sortBy, String sortOrder, int offset) {
		LOGGER.info("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		HttpStatus httpStatus;
		BucketCollectionVO bucketCollectionVO = new BucketCollectionVO();
		LOGGER.info("list buckets for user:{}", currentUser);
		MinioGenericResponse minioResponse = dnaMinioClient.getAllBuckets(currentUser, false);
		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			LOGGER.info("Success from list buckets minio client");
			httpStatus = minioResponse.getHttpStatus();
			if (!ObjectUtils.isEmpty(minioResponse.getBuckets())) {
				// Fetching data from database for specified users
				LOGGER.info("Fetching records from database.");
				List<StorageNsql> storageEntities = customRepo.getAllWithFilters(currentUser,  limit, sortBy, sortOrder, offset);
				List<BucketVo> bucketsVO = new ArrayList<>();
				// converting the storageEntities to bucketVO objects and adding it to bucketsVO
				bucketsVO = storageEntities.stream().map(n-> storageAssembler.toBucketVo(n)).collect(Collectors.toList());
				for (Bucket bucket : minioResponse.getBuckets()) {
					BucketVo bucketVo = storageAssembler.toBucketVo(storageEntities, bucket.name());
					for (BucketVo s: bucketsVO) {
						if (s.getBucketName().equals(bucket.name())) {
							if (Objects.isNull(bucketVo.getPermission())) {
								// Setting current user details
								s.setPermission(dnaMinioClient.getBucketPermission(bucket.name(), currentUser));
							}
							s = new BucketVo();
							s.setBucketName(bucket.name());
							s.setCreatedDate(Date.from(bucket.creationDate().toInstant()));
							s.setCollaborators(dnaMinioClient.getBucketCollaborators(bucket.name(), currentUser));
							LOGGER.info("Setting collaborators for bucket:{}", bucket.name());
						}
					}
				}
				bucketCollectionVO.setData(bucketsVO);
			}
		} else {
			LOGGER.info("Failure from list buckets minio client");
			httpStatus = minioResponse != null ? minioResponse.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
			bucketCollectionVO.setErrors(getMessages(minioResponse != null ? minioResponse.getErrors() : null));
		}
		return new ResponseEntity<>(bucketCollectionVO, httpStatus);
	}
	
	@Override
	public ResponseEntity<BucketCollectionVO> getAllBucketsUsingMC(int limit, String sortBy, String sortOrder, int offset) {
		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		HttpStatus httpStatus;
		BucketCollectionVO bucketCollectionVO = new BucketCollectionVO();
		LOGGER.debug("list buckets for user:{}", currentUser);
		McListBucketCollectionDto minioResponse = dnaMinioClient.getAllBucketsUsingMC(currentUser, true);
		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			LOGGER.info("Success from list buckets minio client");
			httpStatus = minioResponse.getHttpStatus();
			if (!ObjectUtils.isEmpty(minioResponse.getData())) {
				// Fetching data from database for specified users
				LOGGER.info("Fetching records from database.");
				List<StorageNsql> storageEntities = customRepo.getAllWithFilters(currentUser,  limit, sortBy, sortOrder, offset);
				List<BucketVo> bucketsVO = new ArrayList<>();
				// converting the storageEntities to bucketVO objects and adding it to bucketsVO
				bucketsVO = storageEntities.stream().map(n-> storageAssembler.toBucketVo(n)).collect(Collectors.toList());
				List<McListBucketDto> minioBuckets = minioResponse.getData();
				for(BucketVo tempBucket : bucketsVO) {
					
						if(tempBucket.getCollaborators() != null && !tempBucket.getCollaborators().isEmpty()) {
							Optional<UserVO> currentUserRecord = tempBucket.getCollaborators().stream().filter(n->currentUser.equalsIgnoreCase(n.getAccesskey())).findFirst();
							if(currentUserRecord.isPresent() && !currentUserRecord.isEmpty() && currentUserRecord.get()!=null && currentUserRecord.get().getPermission()!=null) {
								tempBucket.setPermission(currentUserRecord.get().getPermission());
							}
						}
						if(tempBucket.getLastModifiedDate()==null) {
							if(minioBuckets!=null && !minioBuckets.isEmpty()) {
								Optional<McListBucketDto> matchingBucket = minioBuckets.stream().filter(n-> tempBucket.getBucketName().equalsIgnoreCase(n.getKey().substring(0, n.getKey().length() - 1))).findFirst();
								if(matchingBucket.isPresent() && !matchingBucket.isEmpty()) {
									String lastModifiedString = matchingBucket.get().getLastModified();
									if(lastModifiedString!=null) {
										try {
											ZonedDateTime dateTime = ZonedDateTime.parse(lastModifiedString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
											Instant instant = dateTime.toInstant();
											Date utilDate = Date.from(instant);
											tempBucket.setLastModifiedDate(utilDate);
										}catch (Exception e){
											LOGGER.error("Failed to set lastmodified date to bucket {}", tempBucket.getBucketName());
										}
									}
									
								}
							}
						}
				}
				bucketCollectionVO.setData(bucketsVO);
				int totalBuckets = 0;
				try
				{
					totalBuckets = customRepo.totalNumberOfBuckets(currentUser);
					bucketCollectionVO.setTotalCount(totalBuckets);
				}
				catch(Exception e)
				{
					LOGGER.info("Failed to get total bucket count"+e.getMessage());
				}
				bucketCollectionVO.setTotalCount(totalBuckets);
			}
		} else {
			LOGGER.info("Failure from list buckets minio client");
			httpStatus = minioResponse != null ? minioResponse.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
			bucketCollectionVO.setErrors(getMessages(minioResponse != null ? minioResponse.getErrors() : null));
		}
		return new ResponseEntity<>(bucketCollectionVO, httpStatus);
	}


	@Override
	public ResponseEntity<BucketObjectResponseWrapperVO> getBucketObjects(String bucketName, String prefix) {
		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		HttpStatus httpStatus;

			String chronosUserToken = httpRequest.getHeader("chronos-api-key");
			boolean authFlag = chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken);
			if(chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken)) {
				currentUser = dataBricksUser;
			}
			LOGGER.debug("authflag {} currentUser {}",authFlag,currentUser);

		BucketObjectResponseWrapperVO objectResponseWrapperVO = new BucketObjectResponseWrapperVO();

		LOGGER.debug("list bucket objects through minio client");
		MinioGenericResponse minioObjectResponse = dnaMinioClient.getBucketObjectsUsingMC(currentUser, bucketName, prefix);
		LOGGER.debug("minioObjectResponse :"+ minioObjectResponse);
		if (minioObjectResponse != null && minioObjectResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			LOGGER.debug("Success from list objects minio client");
			BucketObjectResponseVO bucketObjectResponseVO = new BucketObjectResponseVO();
			//setting Bucket's object response from minio
			bucketObjectResponseVO.setBucketObjects(minioObjectResponse.getObjects());

			LOGGER.debug("Fetching bucket:{} permission for user:{}",bucketName,currentUser);
			bucketObjectResponseVO.setBucketPermission(dnaMinioClient.getBucketPermission(bucketName, currentUser));
			LOGGER.debug("bucketObjectResponseVO :"+ bucketObjectResponseVO);
			objectResponseWrapperVO.setData(bucketObjectResponseVO);
			httpStatus = minioObjectResponse.getHttpStatus();

		} else {
			LOGGER.info("Failure from list objects minio client for bucket {} ", bucketName);
			objectResponseWrapperVO
					.setErrors(getMessages(minioObjectResponse!=null?minioObjectResponse.getErrors():null));
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(objectResponseWrapperVO, httpStatus);
	}

	@Override
	public ResponseEntity<ByteArrayResource> getObjectContent(String bucketName, String prefix) {
		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		HttpStatus httpStatus;
		String chronosUserToken = httpRequest.getHeader("chronos-api-key");
		boolean authFlag = chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken);
		if(chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken)) {
			currentUser = dataBricksUser;
		}
		LOGGER.debug("authflag {} currentUser {}",authFlag,currentUser);
		LOGGER.debug("fetch object/file content through minio client");
		MinioGenericResponse minioResponse = dnaMinioClient.getObjectContents(currentUser, bucketName, prefix);

		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			if (Objects.nonNull(minioResponse.getObjectMetadata().getObjectContent())) {
				LOGGER.info("Success from get object minio client");
				ByteArrayResource resource = minioResponse.getObjectMetadata().getObjectContent();
				return ResponseEntity.ok().contentLength(resource.contentLength()).contentType(contentType(prefix))
						.header("Content-disposition", "attachment; filename=\"" + fileName(prefix) + "\"")
						.body(resource);
			} else {
				LOGGER.info("No content available.");
				httpStatus = HttpStatus.NO_CONTENT;
			}

		} else {
			LOGGER.info("Failure from get object minio client");
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(null, httpStatus);
	}

	/*
	 * fetching filename from given prefix
	 *
	 */
	private String fileName(String prefix) {
		String[] arr = prefix.split("/");
		return arr[arr.length - 1];
	}

	/*
	 * Setting media type with file extension
	 *
	 */
	private MediaType contentType(String fileName) {
		String[] arr = fileName.split("\\.");
		String type = arr[arr.length - 1];
		switch (type) {
		case "txt":
			return MediaType.TEXT_PLAIN;
		case "png":
			return MediaType.IMAGE_PNG;
		case "jpg":
			return MediaType.IMAGE_JPEG;
		default:
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	@Override
	public ResponseEntity<BucketResponseWrapperVO> objectUpload(MultipartFile uploadfile, String bucketName,
			String prefix) {
		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		String chronosUserToken = httpRequest.getHeader("chronos-api-key");
		if(chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken)) {
			currentUser = dataBricksUser;
		}
		LOGGER.debug("currentUser {}",currentUser);
		HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
		BucketResponseWrapperVO bucketResponseWrapperVO = new BucketResponseWrapperVO();
		List<MessageDescription> errors = validateForUpload(uploadfile);
		if (ObjectUtils.isEmpty(errors)) {
			LOGGER.debug("upload object/file through minio client.");
			MinioGenericResponse minioResponse = dnaMinioClient.objectUpload(currentUser, uploadfile, bucketName,
					prefix);
			if (minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
				LOGGER.info("Success from put object minio client.");
				httpStatus = HttpStatus.OK;
			} else {
				LOGGER.info("Failure from put object minio client.");
				bucketResponseWrapperVO.setErrors(getMessages(minioResponse.getErrors()));
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			}
			bucketResponseWrapperVO.setStatus(minioResponse.getStatus());
		} else {
			bucketResponseWrapperVO.setErrors(errors);
		}
		return new ResponseEntity<>(bucketResponseWrapperVO, httpStatus);
	}

	/*
	 * To validate file before upload
	 */
	private List<MessageDescription> validateForUpload(MultipartFile uploadfile) {
		List<MessageDescription> errors = new ArrayList<>();
		if (uploadfile.isEmpty()) {
			LOGGER.info("Uploaded file is empty.");
			errors.add(new MessageDescription("Please select file to upload."));
		} else if (Boolean.TRUE.equals(attachmentMalwareScan)) {
			LOGGER.debug("Scanning for malware for file {}", uploadfile.getOriginalFilename());
			FileScanDetailsVO fileScanDetailsVO = this.scan(uploadfile);
			if (Objects.nonNull(fileScanDetailsVO) && Boolean.TRUE.equals(fileScanDetailsVO.getDetected())) {
				LOGGER.info("Malware detected in the uploaded file {}", uploadfile.getOriginalFilename());
				// setting upload as false
				errors.add(new MessageDescription(
						"Malware detected in the uploaded file " + uploadfile.getOriginalFilename()));
			} else if (Objects.isNull(fileScanDetailsVO) || StringUtils.hasText(fileScanDetailsVO.getErrorMessage())) {
				LOGGER.info("Failed to scan file:{}", uploadfile.getName());
				// setting upload as false
				errors.add(new MessageDescription("Failed to scan file:" + uploadfile.getName()));
			}
		}
		return errors;
	}

	@Override
	public ResponseEntity<UserRefreshWrapperVO> userRefresh(String userId) {
		HttpStatus httpStatus;
		UserRefreshWrapperVO userRefreshWrapperVO = new UserRefreshWrapperVO();

		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();

		// Setting current user as user Id if userId is null
		userId = StringUtils.hasText(userId) ? userId : currentUser;

		if (!userId.equals(currentUser) && !userStore.getUserInfo().hasAdminAccess()) {
			LOGGER.info("No permission to refresh user:{}, only owner or admin can refresh", userId);
			userRefreshWrapperVO.setErrors(Arrays.asList(new MessageDescription(
					"No permission to refresh user:" + userId + ", only owner or admin can refresh")));
			httpStatus = HttpStatus.FORBIDDEN;
			userRefreshWrapperVO.setStatus(ConstantsUtility.FAILURE);
		} else {

			LOGGER.debug("Refresh user through minio client.");
			MinioGenericResponse minioResponse = dnaMinioClient.userRefresh(userId);
			if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
				LOGGER.info("Success from refresh minio client.");
				httpStatus = HttpStatus.OK;
				userRefreshWrapperVO.setData(minioResponse.getUser());
			} else {
				LOGGER.info("Failure from refresh minio client.");
				userRefreshWrapperVO
						.setErrors(getMessages(minioResponse!=null?minioResponse.getErrors():null));
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			}
			userRefreshWrapperVO.setStatus(minioResponse!=null?minioResponse.getStatus():null);
		}

		return new ResponseEntity<>(userRefreshWrapperVO, httpStatus);
	}

	@Override
	public ResponseEntity<ConnectionResponseWrapperVO> getConnection(String bucketName, String userId, String prefix) {
		HttpStatus httpStatus;
		ConnectionResponseWrapperVO responseWrapperVO = new ConnectionResponseWrapperVO();
		responseWrapperVO.setStatus(ConstantsUtility.FAILURE);
		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		// Setting current user as user Id if userId is null
		userId = StringUtils.hasText(userId) ? userId : currentUser;
		if (!userId.equals(currentUser) && !userStore.getUserInfo().hasAdminAccess()) {
			LOGGER.info(
					"No permission to get Connection details for user:{}, only owner or admin can get connection details.",
					userId);
			responseWrapperVO
					.setErrors(Arrays.asList(new MessageDescription("No permission to get Connection details for user:"
							+ userId + ", only owner or admin can get connection details.")));
			httpStatus = HttpStatus.FORBIDDEN;
		} else if (Boolean.FALSE.equals(dnaMinioClient.validateUserInMinio(userId))) {
			LOGGER.info("User:{} not present in Minio.", userId);
			responseWrapperVO
					.setErrors(Arrays.asList(new MessageDescription("User:" + userId + " not present in Minio.")));
			httpStatus = HttpStatus.NO_CONTENT;
		} else {
			String secretKey = vaultConfig.validateUserInVault(userId);
			if (StringUtils.hasText(secretKey)) {
				ConnectionResponseVO responseVO = new ConnectionResponseVO();

				UserVO userVO = new UserVO();
				// setting credentials
				userVO.setAccesskey(userId);
				userVO.setSecretKey(secretKey);
				// Setting permission
				userVO.setPermission(dnaMinioClient.getBucketPermission(bucketName, userId));
				//Map<String, String> bucketConnectionUri = dnaMinioClient.getUri(currentUser,bucketName, null);
				userVO.setUri(storageConnectHost+"/buckets/"+bucketName);
				userVO.setHostName(storageConnectHost);
				responseVO.setUserVO(userVO);

				// To get connected dataiku projects from database
				StorageNsql storageEntity = customRepo.findbyUniqueLiteral(ConstantsUtility.BUCKET_NAME, bucketName);
				if (Objects.nonNull(storageEntity) && Objects.nonNull(storageEntity.getData())) {
					// setting dataiku projects
					responseVO.setDataikuProjects(storageEntity.getData().getDataikuProjects());
				}

				responseWrapperVO.setData(responseVO);
				responseWrapperVO.setStatus(ConstantsUtility.SUCCESS);
				httpStatus = HttpStatus.OK;
			} else {
				LOGGER.info("User:{} not present in Vault.", userId);
				responseWrapperVO
						.setErrors(Arrays.asList(new MessageDescription("User:" + userId + " not present in Vault.")));
				httpStatus = HttpStatus.NO_CONTENT;
			}
		}

		return new ResponseEntity<>(responseWrapperVO, httpStatus);
	}

	@Override
	public ResponseEntity<GenericMessage> cacheRefresh() {
		GenericMessage genericMessage = new GenericMessage();
		HttpStatus httpStatus;
		LOGGER.debug("Fetching users from Minio.");
		Map<String, UserInfo> users = dnaMinioClient.listUsers();
		if (users.isEmpty()) {
			genericMessage.setSuccess(ConstantsUtility.FAILURE);
			genericMessage.setErrors(
					Arrays.asList(new MessageDescription("Cache refresh failed as no data got from Minio.")));
			httpStatus = HttpStatus.NOT_FOUND;
		} else {
			// updating minioUsersCache
			LOGGER.debug("Removing all enteries from minioUsersCache.");
			cacheUtil.removeAll(ConstantsUtility.MINIO_USERS_CACHE);
			LOGGER.debug("Updating minioUsersCache.");
			cacheUtil.updateCache(ConstantsUtility.MINIO_USERS_CACHE, users);

			genericMessage.setSuccess(ConstantsUtility.SUCCESS);
			httpStatus = HttpStatus.OK;
		}

		return new ResponseEntity<>(genericMessage, httpStatus);
	}

	@Override
	public ResponseEntity<GenericMessage> deleteBucketObjects(String bucketName, String prefix) {
		GenericMessage genericMessage = new GenericMessage();
		HttpStatus httpStatus;

		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		String chronosUserToken = httpRequest.getHeader("chronos-api-key");
		boolean authFlag = chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken);
		if (chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken)) {
			currentUser = dataBricksUser;
		}
		LOGGER.info("authflag {} currentUser {}",authFlag,currentUser);

		MinioGenericResponse minioResponse = dnaMinioClient.removeObjects(currentUser, bucketName, prefix);
		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			LOGGER.info("Success from minio remove objects.");
			genericMessage.setSuccess(ConstantsUtility.SUCCESS);
			httpStatus = HttpStatus.OK;
		} else {
			LOGGER.info("Failure from minio remove objects.");
			genericMessage.setSuccess(ConstantsUtility.FAILURE);
			genericMessage
					.setErrors(getMessages(minioResponse!=null?minioResponse.getErrors():null));
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(genericMessage, httpStatus);
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteBucket(String bucketName, Boolean live) {
		GenericMessage genericMessage = new GenericMessage();
		HttpStatus httpStatus;

		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
			StorageNsql entity = customRepo.findbyUniqueLiteral(ConstantsUtility.BUCKET_NAME, bucketName); 
		if(technicalId.equalsIgnoreCase(currentUser) || userStore.getUserInfo().hasAdminAccess()){
			currentUser=entity.getData().getCreatedBy().getId();
			LOGGER.info("The current user while calling api from technicaluser or admin" + currentUser);
		}
		else if(!currentUser.equalsIgnoreCase(entity.getData().getCreatedBy().getId())){
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new GenericMessage("User not authorized to delete this bucket."));
		}
		LOGGER.info("the current user is" + currentUser);
		String chronosUserToken = httpRequest.getHeader("chronos-api-key");
		boolean authFlag = chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken);
		if (chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken)) {
			currentUser = dataBricksUser;
		}
		LOGGER.debug("authflag {} currentUser {}",authFlag,currentUser);

		LOGGER.debug("Removing bucket:{}", bucketName);
		MinioGenericResponse minioResponse = dnaMinioClient.removeBucket(currentUser, bucketName);
		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			LOGGER.info("Success from minio remove bucket.");
			// Fetching bucket info from database
			entity = customRepo.findbyUniqueLiteral(ConstantsUtility.BUCKET_NAME, bucketName);
			if (Objects.nonNull(entity) && StringUtils.hasText(entity.getId())) {
				// To delete dataiku connection if exists
				Optional.ofNullable(entity.getData().getDataikuProjects()).ifPresent(l -> l.forEach(projectAndCloudProfile -> {
					String project = projectAndCloudProfile;
					String cloudProfile = null;
					Pattern pattern = Pattern.compile("([^\\s]+) \\((\\w+)\\)");
					Matcher matcher = pattern.matcher(project);
					if (matcher.find()) {
						project = matcher.group(1); // Extracts the project name
						cloudProfile = matcher.group(2); // Extracts the cloud profile
					}
					LOGGER.info("Removing connection for project:{}", project);
					dataikuClient.deleteDataikuConnection(StorageUtility.getDataikuConnectionName(project, bucketName),
							live, cloudProfile);
				}));
				LOGGER.info("Deleting bucket:{} info from database", bucketName);
				jpaRepo.deleteById(entity.getId());
			}
			genericMessage.setSuccess(ConstantsUtility.SUCCESS);
			httpStatus = HttpStatus.OK;
		} else {
			LOGGER.info("Failure from minio remove bucket.");
			genericMessage.setSuccess(ConstantsUtility.FAILURE);
			genericMessage.setErrors(getMessages(minioResponse != null ? minioResponse.getErrors() : null));
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(genericMessage, httpStatus);
	}

	 @Override
	 @Transactional
	 public ResponseEntity<GenericMessage> deleteBucketCascade(String bucketName, Boolean live) {
	 	GenericMessage genericMessage = new GenericMessage();
	 	HttpStatus httpStatus;

	 	LOGGER.debug("Fetching Current user.");
	 	String currentUser = userStore.getUserInfo().getId(); 
	 	String chronosUserToken = httpRequest.getHeader("chronos-api-key");
	 	boolean authFlag = chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken);
	 	if (chronosUserToken!=null && dataBricksAuth.equals(chronosUserToken)) {
	 		currentUser = dataBricksUser;
	 	}
	 	LOGGER.debug("authflag {} currentUser {}",authFlag,currentUser);

		 // To delete bucket cascade.
		 MinioGenericResponse minioObjectResponse = dnaMinioClient.removeBucket(currentUser, bucketName);

	 	 if (minioObjectResponse != null && minioObjectResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
	 	 	LOGGER.info("Success from minio remove bucket.");
	 	 	// Fetching bucket info from database
	 	 	StorageNsql entity = customRepo.findbyUniqueLiteral(ConstantsUtility.BUCKET_NAME, bucketName);
	 	 	if (Objects.nonNull(entity) && StringUtils.hasText(entity.getId())) {
	 	 		// To delete dataiku connection if exists
	 	 		Optional.ofNullable(entity.getData().getDataikuProjects()).ifPresent(l -> l.forEach(projectAndCloudProfile -> {
					String project = projectAndCloudProfile;
					String cloudProfile = null;
					Pattern pattern = Pattern.compile("([^\\s]+) \\((\\w+)\\)");
					Matcher matcher = pattern.matcher(project);
					if (matcher.find()) {
						project = matcher.group(1); // Extracts the project name
						cloudProfile = matcher.group(2); // Extracts the cloud profile
					}
	 	 			LOGGER.info("Removing connection for project:{}", project);
	 	 			dataikuClient.deleteDataikuConnection(StorageUtility.getDataikuConnectionName(project, bucketName),
	 	 					live, cloudProfile);
	 	 		}));
	 	 		LOGGER.info("Deleting bucket:{} info from database", bucketName);
	 	 		jpaRepo.deleteById(entity.getId());
	 	 	}
	 	 	genericMessage.setSuccess(ConstantsUtility.SUCCESS);
	 	 	httpStatus = HttpStatus.OK;
	 	 } else {
	 	 	LOGGER.info("Failure from minio remove bucket.");
	 	 	genericMessage.setSuccess(ConstantsUtility.FAILURE);
	 	 	genericMessage.setErrors(getMessages(minioObjectResponse != null ? minioObjectResponse.getErrors() : null));
	 	 	httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
	 	 }
	 	return new ResponseEntity<>(genericMessage, httpStatus);
	 }

	@Override
	public Boolean isBucketPresent(String bucketName) {
		return dnaMinioClient.isBucketExists(bucketName);
	}

	/*
	 * To convert List<Error> errors to List<MessageDescription>
	 *
	 */
	private List<MessageDescription> getMessages(List<ErrorDTO> errors){
		List<MessageDescription> messages = null;
		if(!ObjectUtils.isEmpty(errors)) {
			messages = new ArrayList<>();
			for(ErrorDTO error:errors) {
				messages.add(new MessageDescription(error.getErrorMsg()));
			}
		}
		return messages;
	}

	@Override
	@Transactional
	public ResponseEntity<BucketResponseWrapperVO> updateBucket(BucketVo bucketVo) {
		BucketResponseWrapperVO responseVO = new BucketResponseWrapperVO();
		HttpStatus httpStatus;
		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		List<MessageDescription> warnings = new ArrayList<>();
		
		LOGGER.debug("Validating Bucket before update.");

		List<MessageDescription> errors = validateUpdateBucket(bucketVo);
		if (!ObjectUtils.isEmpty(errors)) {
			responseVO.setStatus(ConstantsUtility.FAILURE);
			responseVO.setErrors(errors);
			httpStatus = HttpStatus.BAD_REQUEST;
		} else {
			StorageNsql entity = customRepo.findbyUniqueLiteral(ConstantsUtility.BUCKET_NAME, bucketVo.getBucketName());
			if (entity != null && entity.getData() != null ) {
				if( entity.getData().getDataikuProjects() != null) {
					bucketVo.setDataikuProjects(entity.getData().getDataikuProjects());
				}
				BucketVo existingBucketVo = storageAssembler.toBucketVo(entity);
				bucketVo.setCreatedBy(existingBucketVo.getCreatedBy());
				bucketVo.setCreatedDate(existingBucketVo.getCreatedDate());
				if( bucketVo.isEnablePublicAccess() != null ){
					if (existingBucketVo.isEnablePublicAccess() == null || !(bucketVo.isEnablePublicAccess().equals(existingBucketVo.isEnablePublicAccess()))) {
						MinioGenericResponse minioObjectResponse = dnaMinioClient
								.setBucketPublicDownloadUsingMc(bucketVo.getBucketName(), bucketVo.isEnablePublicAccess());
						if (minioObjectResponse != null
								&& minioObjectResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
							LOGGER.info("Successfully updated the public access status of storage bucket");
						} else {
							bucketVo.setEnablePublicAccess(false);
							LOGGER.info("Failed to update the public access status of storage bucket");
							MessageDescription msg = new MessageDescription(
									"Failed to update the public access status of storage bucket");
							warnings.add(msg);
							responseVO.setWarnings(warnings);
						}
					}
				}
			}
			LOGGER.debug("Fetching existing collaborators for bucket:{}", bucketVo.getBucketName());
			List<UserVO> existingCollaborators = dnaMinioClient.getBucketCollaborators(bucketVo.getBucketName(),
					currentUser);
			LOGGER.debug("Fetching new collaborators for bucket:{}", bucketVo.getBucketName());
			List<UserVO> newCollaborators = getNewCollaborators(bucketVo);
			if(!(ObjectUtils.isEmpty(existingCollaborators) && ObjectUtils.isEmpty(newCollaborators))) {
				// To update collaborators list
				errors = updateBucketCollaborator(bucketVo.getBucketName(), existingCollaborators,
						bucketVo.getCollaborators());
			}
			if (ObjectUtils.isEmpty(errors)) {
				//To update Bucket record in database
				bucketVo.setLastModifiedDate(new Date());
				bucketVo.setUpdatedBy(userStore.getVO());
				StorageNsql updatedData = storageAssembler.toEntity(bucketVo);
				jpaRepo.save(updatedData);
				responseVO.setData(bucketVo);

				responseVO.setStatus(ConstantsUtility.SUCCESS);
				httpStatus = HttpStatus.OK;
			} else {
				responseVO.setStatus(ConstantsUtility.FAILURE);
				responseVO.setErrors(errors);
				httpStatus = HttpStatus.BAD_REQUEST;
			}
		}

		responseVO.setData(bucketVo);
		return new ResponseEntity<>(responseVO, httpStatus);
	}

	/*
	 * To get list of new collaborators
	 * Add creator as collaborator if not present in collaborators list
	 */
	private List<UserVO> getNewCollaborators(BucketVo bucketVo) {
		List<UserVO> newCollaborators = bucketVo.getCollaborators();
		UserVO creator = storageAssembler.toUserVO(bucketVo.getCreatedBy());
		if (Objects.nonNull(creator) && (ObjectUtils.isEmpty(newCollaborators) || newCollaborators.stream()
				.noneMatch(c -> creator.getAccesskey().equalsIgnoreCase(c.getAccesskey())))) {
			newCollaborators.add(creator);
		}
		return newCollaborators;
	}

	/*
	 * To get Union of list return list of user by making unison of 2 userVO list
	 */
	private List<String> getUsersUnion(List<UserVO> list1, List<UserVO> list2) {
		Set<UserVO> set = new TreeSet<>(new Comparator<UserVO>() {
			@Override
			public int compare(UserVO u1, UserVO u2) {
				return u1.getAccesskey().compareTo(u2.getAccesskey());
			}
		});
		// Adding list one to set
		set.addAll(list1);
		// Adding list two to set
		set.addAll(list2);
		// fetching users
		return set.stream().map(t -> t.getAccesskey()).toList();
	}

	/*
	 * update collaborator for bucket by comparing existing and new collaborator
	 * list
	 */
	private List<MessageDescription> updateBucketCollaborator(String bucketName, List<UserVO> existingCollaborators,
			List<UserVO> newCollaborators) {
		List<MessageDescription> errors = new ArrayList<>();
		LOGGER.debug("Fetching users from Minio user cache.");
		Map<String, UserInfo> usersInfo = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
		String readPolicy = bucketName + "_" + ConstantsUtility.READ;
		String readWritePolicy = bucketName + "_" + ConstantsUtility.READWRITE;
		// To get all users list
		List<String> usersId = getUsersUnion(existingCollaborators, newCollaborators);
		for (String userId : usersId) {
			// To get User details from newCollaborators
			Optional<UserVO> newCollaborator = newCollaborators.stream()
					.filter(userVO -> userVO.getAccesskey().equals(userId)).findAny();
			// To get User details from existingCollaborators
			Optional<UserVO> existingCollaborator = existingCollaborators.stream()
					.filter(userVO -> userVO.getAccesskey().equals(userId)).findAny();
			// To get user info from Minio
			UserInfo userInfo = usersInfo.get(userId);
			String policy = "";
			// if user presents in new and existing
			if (newCollaborator.isPresent() && existingCollaborator.isPresent()) {
				LOGGER.debug("Checking permission update for existing collaborators");
				PermissionVO permissionVO = newCollaborator.get().getPermission();
				// Getting policy from user
				policy = userInfo.policyName();
				// Checking for read permission
				// if read permission available adding it
				// if read permission not available removing it
				if (Boolean.TRUE.equals(permissionVO.isRead())) {
					policy = StorageUtility.addPolicy(policy, readPolicy);
				} else {
					policy = StorageUtility.removePolicy(policy, readPolicy);
				}
				// Checking for read/write permission
				// if read/write permission available adding it
				// if read/write permission not available removing it
				if (Boolean.TRUE.equals(permissionVO.isWrite())) {
					policy = StorageUtility.addPolicy(policy, readWritePolicy);
				} else {
					policy = StorageUtility.removePolicy(policy, readWritePolicy);
				}
				// Setting permission in Minio
				if("".equalsIgnoreCase(policy) || policy == null || !StringUtils.hasText(policy)) {
					dnaMinioClient.deleteUser(userId);
				}else {
					dnaMinioClient.setPolicy(userId, false, policy);
				}

			}
			// If user presents only in new
			else if (newCollaborator.isPresent() && !existingCollaborator.isPresent()) {
				LOGGER.debug("Setting permission for new collaborators");
				PermissionVO permissionVO = newCollaborator.get().getPermission();
				List<String> policies = new ArrayList<>();
				// for read permission
				if (Boolean.TRUE.equals(permissionVO.isRead())) {
					LOGGER.debug("Setting READ access.");
					policies.add(readPolicy);
				}
				// for write permission
				if (Boolean.TRUE.equals(permissionVO.isWrite())) {
					LOGGER.debug("Setting READ/WRITE access.");
					policies.add(readWritePolicy);
				}
				LOGGER.debug("Onboarding collaborator:{}", userId);
				MinioGenericResponse onboardUserResponse = dnaMinioClient.onboardUserMinio(userId,
						policies);
				if (onboardUserResponse != null && onboardUserResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
					LOGGER.info("Collaborator:{} onboarding successfull", userId);

				} else {
					LOGGER.info("Collaborator:{} onboarding failed", userId);
					errors = getMessages(onboardUserResponse!=null?onboardUserResponse.getErrors():null);
					break;
				}
			}
			// If user presents only in existing
			else if (!newCollaborator.isPresent() && existingCollaborator.isPresent()) {
				// Getting policy from user
				policy = userInfo.policyName();
				// Removing read permission
				policy = StorageUtility.removePolicy(policy, readPolicy);
				// Removing read/write permission
				policy = StorageUtility.removePolicy(policy, readWritePolicy);
				// Setting permission in Minio
				if("".equalsIgnoreCase(policy) || policy == null || !StringUtils.hasText(policy)) {
					dnaMinioClient.deleteUser(userId);
				}else {
					dnaMinioClient.setPolicy(userId, false, policy);
				}

			}
		}
		return errors;
	}


	@Override
	public ResponseEntity<BucketVo> getByBucketName(String bucketName) {
		HttpStatus httpStatus;
		BucketVo bucketVo = new BucketVo();

		LOGGER.debug("Check if bucket exists.");
		boolean isBucketExists = dnaMinioClient.isBucketExists(bucketName);
		if (!isBucketExists) {
			httpStatus = HttpStatus.NOT_FOUND;
			LOGGER.info("Bucket not found.");
		} else {
			//Fetching bucket details from database
			StorageNsql entity = customRepo.findbyUniqueLiteral(ConstantsUtility.BUCKET_NAME, bucketName);
			bucketVo = storageAssembler.toBucketVo(entity);
			LOGGER.debug("Fetching Current user.");
			String currentUser = userStore.getUserInfo().getId();
			// Setting bucket details
			bucketVo.setPermission(dnaMinioClient.getBucketPermission(bucketName, currentUser));
			httpStatus = HttpStatus.OK;
		}
		return new ResponseEntity<>(bucketVo, httpStatus);
	}

	/**
	 * To scan file by calling AVscan service
	 *
	 * @param multiPartFile
	 * @return FileScanDetailsVO
	 */
	private FileScanDetailsVO scan(MultipartFile multiPartFile) {
		LOGGER.debug("Calling avscan client to scan file:{}",multiPartFile.getOriginalFilename());
		Optional<FileScanDetailsVO> aVScannerRes = malwareScannerClient.scan(multiPartFile);
		return aVScannerRes.isPresent()?aVScannerRes.get():null;
	}


	@Override
	public ResponseEntity<GenericMessage> bucketMigrate() {
		MinioGenericResponse minioResponse = dnaMinioClient.getAllBuckets(null, true);
		HttpStatus httpStatus;
		GenericMessage genericMessage = new GenericMessage();
		if (minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)
				&& !ObjectUtils.isEmpty(minioResponse.getBuckets())) {
			LOGGER.info("Success from list buckets minio client");
			httpStatus = minioResponse.getHttpStatus();
			genericMessage.setSuccess(ConstantsUtility.SUCCESS);
			// getting users info from minio user cache
			Map<String, UserInfo> usersInfo = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
			// Iterating over bucket list
			for (Bucket bucket : minioResponse.getBuckets()) {
				String bucketName = bucket.name();
				// To check if record already exist for bucket
				StorageNsql storage = customRepo.findbyUniqueLiteral(ConstantsUtility.BUCKET_NAME, bucketName);
				if (Objects.isNull(storage)) {
					BucketVo bucketVo = new BucketVo();
					bucketVo.setBucketName(bucketName);
					bucketVo.setCreatedDate(Date.from(bucket.creationDate().toInstant()));
					bucketVo.setLastModifiedDate(Date.from(bucket.creationDate().toInstant()));
					// Setting default values
					bucketVo.setPiiData(false);
					bucketVo.setClassificationType("Internal");
					bucketVo.setTermsOfUse(false);
					List<UserVO> collaborators = new ArrayList<>();
					for (var entry : usersInfo.entrySet()) {
						if (StringUtils.hasText(entry.getValue().policyName())) {
							UserVO userVO = null;
							PermissionVO permissionVO = new PermissionVO();
							if (StorageUtility.hasText(entry.getValue().policyName(), bucketName + "_" + ConstantsUtility.READ)) {
								LOGGER.debug("User:{} has read access to bucket:{}", entry.getKey(), bucketName);
								// Setting accesskey
								userVO = new UserVO();
								userVO.setAccesskey(entry.getKey());
								// Setting permission
								permissionVO.setRead(true);
								permissionVO.setWrite(false);
								userVO.setPermission(permissionVO);
							}
							if (StorageUtility.hasText(entry.getValue().policyName(), bucketName + "_" + ConstantsUtility.READWRITE)) {
								LOGGER.debug("User:{} has read/write access to bucket:{}", entry.getKey(), bucketName);
								userVO = new UserVO();
								// Setting accesskey
								userVO.setAccesskey(entry.getKey());
								// Setting permission
								permissionVO.setRead(true);
								permissionVO.setWrite(true);
								userVO.setPermission(permissionVO);
								if (Objects.isNull(bucketVo.getCreatedBy())) {
									CreatedByVO createdByVO = new CreatedByVO();
									createdByVO.setId(entry.getKey());
									UserInfoVO userInfoVO = dnaAuthClient.userInfoById(userVO.getAccesskey());
									if (Objects.nonNull(userInfoVO)) {
										BeanUtils.copyProperties(userInfoVO, createdByVO);
									}
									bucketVo.setCreatedBy(createdByVO);
								}
							}
							if (Objects.nonNull(userVO)) {
								UserInfoVO userInfoVO = dnaAuthClient.userInfoById(userVO.getAccesskey());
								if (Objects.nonNull(userInfoVO)) {
									BeanUtils.copyProperties(userInfoVO, userVO);
								}
								userVO.setPermission(permissionVO);
								collaborators.add(userVO);
							}
						}
					}
					// setting collaborators
					bucketVo.setCollaborators(collaborators);
					LOGGER.info("Saving bucket:{} to database.", bucketVo.getBucketName());
					this.saveBucket(bucketVo);

				} else {
					LOGGER.info("Bucket:{} already exists in database", bucketName);
				}
			}
		} else {
			LOGGER.info("Failure from list buckets minio client");
			httpStatus = minioResponse.getHttpStatus();
			genericMessage.setErrors(getMessages(minioResponse.getErrors()));
		}
		return new ResponseEntity<>(genericMessage, httpStatus);
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> createDataikuConnection(ConnectionVO connectionVO, Boolean live) {
		LOGGER.debug("Fetching Current user.");
		GenericMessage genericMessage = new GenericMessage();
		HttpStatus httpStatus;
		// To fetch bucket details
		StorageNsql storageEntity = customRepo.findbyUniqueLiteral(ConstantsUtility.BUCKET_NAME,
				connectionVO.getBucketName());
		// To validate before connection creation
		List<MessageDescription> validationErrors = this.validateCreateConnection(storageEntity, connectionVO);
		if (!ObjectUtils.isEmpty(validationErrors)) {
			httpStatus = HttpStatus.BAD_REQUEST;
			genericMessage.setErrors(validationErrors);
		} else {
			Storage storage = storageEntity.getData();
			// Union of existing and new projects
			List<String> projectsUnion = StorageUtility.getUnion(storage.getDataikuProjects(),
					connectionVO.getDataikuProjects());

			Optional.ofNullable(projectsUnion).ifPresent(l -> l.forEach(projectAndCloudProfile -> {
				String project = projectAndCloudProfile;
				String cloudProfile = null;
				Pattern pattern = Pattern.compile("([^\\s]+) \\((\\w+)\\)");
				Matcher matcher = pattern.matcher(project);
				if (matcher.find()) {
					 project = matcher.group(1); // Extracts the project name
					 cloudProfile = matcher.group(2); // Extracts the cloud profile
				}
				// To check if project available in new list
				boolean isNewProject = !ObjectUtils.isEmpty(connectionVO.getDataikuProjects())
						&& connectionVO.getDataikuProjects().contains(projectAndCloudProfile);
				boolean isExistingProject = !ObjectUtils.isEmpty(storage.getDataikuProjects())
						&& storage.getDataikuProjects().contains(projectAndCloudProfile);
				if (isNewProject && !isExistingProject) {
					LOGGER.info("Creating new connection for project:{}", project);
					this.createDataikuConnection(connectionVO.getBucketName(), project, live, cloudProfile);
				} else if (!isNewProject && isExistingProject) {
					LOGGER.info("Removing connection for project:{}", project);
					dataikuClient.deleteDataikuConnection(StorageUtility.getDataikuConnectionName(project, connectionVO.getBucketName()),
							live, cloudProfile);
				}
			}));

			// Saving connection details in db
			storage.setDataikuProjects(connectionVO.getDataikuProjects());
			storage.setLastModifiedDate(new Date());
			storage.setUpdatedBy(storageAssembler.setUpdatedBy(userStore));
			storageEntity.setData(storage);
			jpaRepo.save(storageEntity);

			httpStatus = HttpStatus.OK;
			genericMessage.setSuccess(ConstantsUtility.SUCCESS);
		}
		return new ResponseEntity<>(genericMessage, httpStatus);
	}

	/*
	 * To validate create dataiku connection
	 */
	private List<MessageDescription> validateCreateConnection(StorageNsql storageEntity, ConnectionVO connectionVO) {
		List<MessageDescription> errors = new ArrayList<>();
		if (Objects.isNull(storageEntity) || Objects.isNull(storageEntity.getData())) {
			LOGGER.info("Bucket:{} info not available in database, migrate data first.", connectionVO.getBucketName());
			errors.add(new MessageDescription("Bucket information not available in database."));
		}
		return errors;
	}

	/*
	 * To setup data and create dataiku connection
	 */
	private DataikuGenericResponseDTO createDataikuConnection(String bucketName, String projectKey, Boolean live, String cloudProfile) {
		DataikuConnectionRequestDTO requestDTO = new DataikuConnectionRequestDTO();
		String currentUser = userStore.getUserInfo().getId();
		String secretKey = vaultConfig.validateUserInVault(currentUser);
		LOGGER.debug("Fetching permission..");
		Optional<DataikuPermission> projectPermission = dataikuClient
				.getDataikuProjectPermission(projectKey, live, cloudProfile);

		List<String> projectGroups = new ArrayList<>();
		List<String> allowedProjectGroups = new ArrayList<>();
		if (projectPermission.isPresent() && !ObjectUtils.isEmpty(projectPermission.get().getPermissions())) {
			projectGroups = projectPermission.get().getPermissions().stream().map(Permission::getGroup).toList();
			if (!ObjectUtils.isEmpty(projectGroups)) {
				//Removing READ-ONLY group from allowed groups
				allowedProjectGroups = projectGroups.stream()
						.filter(group -> !group.contains(ConstantsUtility.DATAIKU_READ_ONLY)).toList();
			}
		}
		//Setting RequestDTO
		requestDTO.setName(StorageUtility.getDataikuConnectionName(projectKey, bucketName));
		requestDTO.setType("EC2");

		//Setting params
		DataikuParameterDTO params = new DataikuParameterDTO();
		params.setCredentialsMode("KEYPAIR");
		params.setAccessKey(currentUser);
		params.setSecretKey(secretKey);
		params.setDefaultManagedBucket("/"+bucketName);
		params.setDefaultManagedPath("/");
		params.setRegionOrEndpoint(minioClientApi);
		params.setHdfsInterface("S3A");
		params.setEncryptionMode("NONE");
		params.setChbucket(bucketName);
		params.setChroot("/");
		params.setSwitchToRegionFromBucket(false);
		params.setUsePathMode(true);
		params.setMetastoreSynchronizationMode("NO_SYNC");

		requestDTO.setParams(params);
		requestDTO.setAllowWrite(true);
		requestDTO.setAllowManagedDatasets(true);
		requestDTO.setAllowManagedFolders(true);
		requestDTO.setUseGlobalProxy(false);
		requestDTO.setMaxActivities(0);
		requestDTO.setCredentialsMode("GLOBAL");
		requestDTO.setUsableBy("ALLOWED");
		//Setting allowed groups by removing READ-ONLY group
		requestDTO.setAllowedGroups(allowedProjectGroups);

		DataikuReadabilityDTO detailsReadability = new DataikuReadabilityDTO();
		detailsReadability.setReadableBy("ALLOWED");
		detailsReadability.setAllowedGroups(projectGroups);
		requestDTO.setDetailsReadability(detailsReadability);

		DataikuIndexDTO indexingSettings = new DataikuIndexDTO();
		indexingSettings.setIndexForeignKeys(false);
		indexingSettings.setIndexIndices(false);
		indexingSettings.setIndexSystemTables(false);
		requestDTO.setIndexingSettings(indexingSettings);

		return dataikuClient.createDataikuConnection(requestDTO, live, cloudProfile);
	}

	@Override
	public GenericMessage reassignOwner(CreatedByVO currentUser, BucketVo bucketVo, UserVO newOwnerVo) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		List<com.daimler.data.db.jsonb.UserInfo> existingCollaborators = new ArrayList<>();
		List<com.daimler.data.db.jsonb.UserInfo> updatedCollaborators = new ArrayList<>();
		boolean isBucketOwner = false;
		String bucketName = bucketVo.getBucketName();
		String newOwnerpolicy = "";
		String readWritePolicy = bucketName + "_" + ConstantsUtility.READWRITE;
		List<String> collabIds = new ArrayList<>();
		//Fetching bucket details from database
		StorageNsql entity = customRepo.findbyUniqueLiteral(ConstantsUtility.BUCKET_NAME, bucketVo.getBucketName());
		if(Objects.nonNull(entity)) {
			existingCollaborators = entity.getData().getCollaborators();
		}
		updatedCollaborators.addAll(existingCollaborators);
		String bucketOwnerId = entity.getData().getCreatedBy().getId();
		if (bucketOwnerId.equalsIgnoreCase(currentUser.getId())) {
			isBucketOwner = true;
		}
		if(isBucketOwner) {
			com.daimler.data.db.jsonb.UserInfo currentOwnerAsCollab = entity.getData().getCreatedBy();
			com.daimler.data.db.jsonb.UserInfo newOwner = new com.daimler.data.db.jsonb.UserInfo();
			BeanUtils.copyProperties(newOwnerVo, newOwner);
			newOwner.setId(newOwnerVo.getAccesskey());
			com.daimler.data.db.jsonb.Permission newOwnerPermission = new com.daimler.data.db.jsonb.Permission();
			newOwnerPermission.setRead(Boolean.TRUE);
			newOwnerPermission.setWrite(Boolean.TRUE);
			newOwner.setPermission(newOwnerPermission);
			// To update project owner.
			entity.getData().setCreatedBy(newOwner);
			try {
				Map<String, UserInfo> usersInfo = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
				// To get user info from Minio
				UserInfo newOwnerUserInfo = usersInfo.get(newOwner.getId());
				//Getting policy from user
				newOwnerpolicy = newOwnerUserInfo.policyName();
				//Add read-write policy to the new owner
				newOwnerpolicy = StorageUtility.addPolicy(newOwnerpolicy, readWritePolicy);
				dnaMinioClient.setPolicy(newOwner.getId(), false, newOwnerpolicy);
			}catch(Exception e) {
				LOGGER.error("Failed while calling DnaMinioClient with Exception: {} ", e.getMessage());
				MessageDescription msg = new MessageDescription("Failed while calling DnaMinioClient.");
				errors.add(msg);
				responseMessage.setSuccess("FAILED");
				responseMessage.setErrors(errors);
				return responseMessage;
			}
			//permission is storing as null by default, so setting read/write permissions to the old owner
			com.daimler.data.db.jsonb.Permission currentOwnerPermission = new com.daimler.data.db.jsonb.Permission();
			currentOwnerPermission.setRead(Boolean.TRUE);
			currentOwnerPermission.setWrite(Boolean.TRUE);
			currentOwnerAsCollab.setPermission(currentOwnerPermission);
			// To update permission of the new owner from collaborator list and to add current owner as collaborator.
			if (Objects.nonNull(existingCollaborators)) {
				for (com.daimler.data.db.jsonb.UserInfo collab : existingCollaborators) {
					collabIds.add(collab.getId());
					if (collab.getId().equalsIgnoreCase(newOwner.getId())) {
						collab.getPermission().setWrite(true);
						collab.getPermission().setRead(true);
					}
				}
				if (!(collabIds.contains(currentOwnerAsCollab.getId()))) {
					updatedCollaborators.add(currentOwnerAsCollab);
				}
				else {
					for (com.daimler.data.db.jsonb.UserInfo collab : existingCollaborators) {
						if(collab.getId().equalsIgnoreCase(currentOwnerAsCollab.getId())) {
							if(Objects.isNull(collab.getPermission())) {
								updatedCollaborators.remove(collab);
								com.daimler.data.db.jsonb.Permission collabPermission = new com.daimler.data.db.jsonb.Permission();
								collabPermission.setRead(Boolean.TRUE);
								collabPermission.setWrite(Boolean.TRUE);
								collab.setPermission(collabPermission);
								updatedCollaborators.add(collab);
							}
						}
					}
				}
			}
			entity.getData().setCollaborators(updatedCollaborators);
			try {
				jpaRepo.save(entity);
				LOGGER.info("Project owner and collaborator details updated successfully");
				responseMessage.setSuccess("SUCCESS");
				return responseMessage;
			} catch (Exception e) {
				LOGGER.error("Failed to add collaborator details as requested with Exception: {} ", e.getMessage());
				MessageDescription msg = new MessageDescription("Failed to update project owner and collaborator details");
				errors.add(msg);
				responseMessage.setSuccess("FAILED");
				responseMessage.setErrors(errors);
				return responseMessage;
			}
		}
		return null;
	}

}
