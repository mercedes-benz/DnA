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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.application.config.MinioConfig;
import com.daimler.data.dto.Error;
import com.daimler.data.dto.MinioGenericResponse;
import com.daimler.data.dto.persistence.BucketObjectVO;
import com.daimler.data.dto.persistence.ObjectMetadataVO;
import com.daimler.data.dto.persistence.PermissionVO;
import com.daimler.data.dto.persistence.UserVO;
import com.daimler.data.util.CacheUtil;
import com.daimler.data.util.ConstantsUtility;
import com.daimler.data.util.PolicyUtility;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
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

	@Value("${spring.cloud.vault.token}")
	private String vaultToken;

	@Value("${spring.cloud.vault.scheme}")
	private String vaultScheme;

	@Value("${spring.cloud.vault.host}")
	private String vaultHost;

	@Value("${spring.cloud.vault.port}")
	private String vaultPort;

	@Value("${spring.cloud.vault.vaultpath}")
	private String vaultPath;

	@Value("${spring.cloud.vault.mountpath}")
	private String mountPath;

	@Autowired
	private MinioConfig minioConfig;
	
	@Autowired
	private CacheUtil cacheUtil;

	@Override
	public MinioGenericResponse createBucket(String bucketName) {
		MinioGenericResponse minioResponse = new MinioGenericResponse();
		List<String> policies;
		try {
			MinioClient minioClient = minioConfig.getMinioClient();
			// Make Bucket
			LOGGER.info("Making new bucket: {}", bucketName);
			minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

			LOGGER.debug("Creating policies for bucket:", bucketName);
			policies = new ArrayList<String>();
			
			//Setting resource for full bucket path
			String resource = "arn:aws:s3:::" + bucketName + "/*";
			
			//action to access bucket and corresponding files & directory
			String action = "";
			
			//Setting policy effect as allow
			String effect = "Allow";
			
			//Statement Id 
			String sid = "";

			LOGGER.debug("Creating READ policy");
			//Creating READ policy{eg: bucket1_READ} 
			String policyName = bucketName + "_" + ConstantsUtility.READ;
			//Setting action as view all bucket contents
			action = "s3:ListBucket,s3:GetObject,s3:GetBucketLocation";
			CreateBucketPolicy(policyName, minioPolicyVersion, resource, action, effect, sid);
			policies.add(policyName);

			LOGGER.debug("Creating READWRITE(RW) policy");
			// Create READWRITE(RW):{read+write+delete} policy eg:bucket1_RW
			policyName = bucketName + "_" + ConstantsUtility.READWRITE;
			//Setting action as view, edit & delete all bucket contents
			//action = "s3:ListBucket,s3:GetObject,s3:PutObject,s3:DeleteObject";
			action = "*";
			CreateBucketPolicy(policyName, minioPolicyVersion, resource, action, effect, sid);
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
			minioResponse.setError(buildError(null, "Error occurred while making new bucket: " + bucketName));
		}

		return minioResponse;
	}

	@Override
	public Boolean isBucketExists(String bucketName) {
		Boolean isBucketExists = null;
		try {
			MinioClient minioClient = minioConfig.getMinioClient();
			LOGGER.info("Connecting to minio client to get bucket status for:{}", bucketName);
			isBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			LOGGER.error("Error occurred while validating bucket:{}", e.getMessage());
		}
		return isBucketExists;
	}

	private Error buildError(String errorCode, String errorMsg) {
		return new Error(errorCode, errorMsg);
	}

	@Override
	public MinioGenericResponse objectUpload(String userId, MultipartFile uploadfile, String bucketName,
			String prefix) {

		MinioGenericResponse minioResponse = new MinioGenericResponse();
		try {
			LOGGER.info("Fetching secrets from vault for user:{}", userId);
			String userSecretKey = validateUserInVault(userId);
			MinioClient minioClient = MinioClient.builder().endpoint(minioBaseUri).credentials(userId, userSecretKey)
					.build();
			ByteArrayInputStream bais = new ByteArrayInputStream(uploadfile.getBytes());

			LOGGER.info("Uploading object to minio..{}", uploadfile.getOriginalFilename());
			ObjectWriteResponse objectWriteResponse = minioClient.putObject(PutObjectArgs.builder().bucket(bucketName)
					.object(prefix + "/" + uploadfile.getOriginalFilename()).stream(bais, bais.available(), -1)
					// .sse(ssec)
					.build());
			bais.close();
			LOGGER.info("Success from minio object upload..{}", uploadfile.getOriginalFilename());
			minioResponse.setStatus(ConstantsUtility.SUCCESS);

		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			LOGGER.error("DNA-MINIO-ERR-003::Error occured while uploading object to minio: {}", e.getMessage());
			minioResponse.setError(buildError(null, "Error occured while uploading object to minio "));
			minioResponse.setStatus(ConstantsUtility.FAILURE);
		}
		return minioResponse;

	}

	@Override
	public MinioGenericResponse getAllBuckets(String userId) {

		MinioGenericResponse getBucketResponse = new MinioGenericResponse();
		try {
			LOGGER.info("Fetching secrets from vault for user:{}", userId);
			String userSecretKey = validateUserInVault(userId);
			MinioClient minioClient = MinioClient.builder().endpoint(minioBaseUri).credentials(userId, userSecretKey)
					.build();

			LOGGER.info("Listing all buckets for user:{}", userId);
			List<Bucket> buckets = minioClient.listBuckets();

			LOGGER.info("Success from minio list bucket for user:{}", userId);
			getBucketResponse.setBuckets(buckets);
			getBucketResponse.setStatus(ConstantsUtility.SUCCESS);

		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IOException e) {
			LOGGER.error("DNA-MINIO-ERR-004::Error occured while listing buckets of minio: {}", e.getMessage());
			getBucketResponse.setError(buildError(null, "Error occured while listing buckets of minio. "));
			getBucketResponse.setStatus(ConstantsUtility.FAILURE);

		}
		return getBucketResponse;
	}

	@Override
	public MinioGenericResponse getObjectContents(String userId, String bucketName, String prefix) {
		MinioGenericResponse minioObjectContentResponse = new MinioGenericResponse();
		try {
			LOGGER.info("Fetching secrets from vault for user:{}", userId);
			String userSecretKey = validateUserInVault(userId);
			MinioClient minioClient = MinioClient.builder().endpoint(minioBaseUri).credentials(userId, userSecretKey)
					.build();

			LOGGER.info("Fetching object contents from minio for user:{}", userId);
			InputStream stream = minioClient
					.getObject(GetObjectArgs.builder().bucket(bucketName).object(prefix).build());

			ObjectMetadataVO objectMetadataVO = new ObjectMetadataVO();
			ByteArrayResource resource = new ByteArrayResource(stream.readAllBytes());
			objectMetadataVO.setObjectContent(resource);
			stream.close();

			LOGGER.info("Success from minio get Object contents for user:{}", userId);
			minioObjectContentResponse.setObjectMetadata(objectMetadataVO);
			minioObjectContentResponse.setStatus(ConstantsUtility.SUCCESS);

		} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
				| InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException
				| XmlParserException | IOException e) {
			LOGGER.error("DNA-MINIO-ERR-005::Error occured while fetching object content from minio: {}",
					e.getMessage());
			minioObjectContentResponse
					.setError(buildError(null, "Error occured while fetching object content from minio. "));
			minioObjectContentResponse.setStatus(ConstantsUtility.FAILURE);
		}

		return minioObjectContentResponse;
	}

	@Override
	public MinioGenericResponse getBucketObjects(String userId, String bucketName, String prefix) {
		MinioGenericResponse minioObjectResponse = new MinioGenericResponse();

		try {
			LOGGER.info("Fetching secrets from vault for user:{}", userId);
			String userSecretKey = validateUserInVault(userId);
			MinioClient minioClient = MinioClient.builder().endpoint(minioBaseUri).credentials(userId, userSecretKey)
					.build();

			// Lists objects information.
			LOGGER.info("Listing Objects information from minio for user:{}", userId);
			Iterable<Result<Item>> results = minioClient
					.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).build());

			List<BucketObjectVO> objects = new ArrayList<BucketObjectVO>();
			BucketObjectVO bucketObjectVO = null;
			for (Result<Item> result : results) {
				bucketObjectVO = new BucketObjectVO();
				Item item = result.get();
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

		} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
				| InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException
				| XmlParserException | IOException e) {
			LOGGER.error("DNA-MINIO-ERR-006::Error occured while listing bucket's object from minio: {}",
					e.getMessage());
			minioObjectResponse.setError(buildError(null, "Error occured while listing bucket's object from minio: "+e.getMessage()));
			minioObjectResponse.setStatus(ConstantsUtility.FAILURE);
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
			String commaSeparatedPolicies = "";
			if (!ObjectUtils.isEmpty(policies)) {
				commaSeparatedPolicies = policies.stream().collect(Collectors.joining(","));
			}

			LOGGER.debug("List all minio users.");
			Map<String, UserInfo> users = minioAdminClient.listUsers();
			if (users.containsKey(userId)) {
				LOGGER.info("User: {} already exists", userId);
				UserInfo userInfo = users.get(userId);
				if (StringUtils.hasText(userInfo.policyName())) {
					commaSeparatedPolicies = StringUtils.hasText(commaSeparatedPolicies)
							? commaSeparatedPolicies + "," + userInfo.policyName()
							: commaSeparatedPolicies + userInfo.policyName();
				}
				minioAdminClient.setPolicy(userId, false, commaSeparatedPolicies);
				minioResponse.setStatus(ConstantsUtility.SUCCESS);
				
				//Update users list for minioUsersCache
				String policyName=StringUtils.hasText(userInfo.policyName())?userInfo.policyName()+","+commaSeparatedPolicies:commaSeparatedPolicies;
				//updating policy
				UserInfo userInfoTemp = new UserInfo(userInfo.status(), userInfo.secretKey(), policyName, userInfo.memberOf());
				users.put(userId, userInfoTemp);
								
			} else {

				LOGGER.debug("Creating SecretKey for user: {}", userId);
				userSecretKey = UUID.randomUUID().toString();
				// List<String> memberOf = new ArrayList<String>();

				LOGGER.info("Onboarding user: to minio", userId);
				minioAdminClient.addUser(userId, Status.ENABLED, userSecretKey, commaSeparatedPolicies, null);

				// setting policy to user
				LOGGER.debug("Setting policy for user:{}", userId);
				minioAdminClient.setPolicy(userId, false, commaSeparatedPolicies);

				LOGGER.info("Adding user: {} credentials to vault");
				addUserVault(userId, userSecretKey);

				LOGGER.info("User:{} Onboarded successfully.", userId);
				minioResponse.setStatus(ConstantsUtility.SUCCESS);
				
				//Update users list for minioUsersCache
				//updating policy
				UserInfo userInfoTemp = new UserInfo(Status.ENABLED, userSecretKey, commaSeparatedPolicies, null);
				users.put(userId, userInfoTemp);
			}
			//updating minioUsersCache
			LOGGER.debug("Removing all enteries from {}.",ConstantsUtility.MINIO_USERS_CACHE);
			cacheUtil.removeAll(ConstantsUtility.MINIO_USERS_CACHE);
			LOGGER.debug("Updating {}.",ConstantsUtility.MINIO_USERS_CACHE);
			cacheUtil.updateCache(ConstantsUtility.MINIO_USERS_CACHE, users);

		} catch (NoSuchAlgorithmException | InvalidKeyException | IOException | InvalidCipherTextException e) {
			LOGGER.error("DNA-MINIO-ERR-007::Error occured while onboarding user to minio: ", e.getMessage());
			minioResponse.setStatus(ConstantsUtility.FAILURE);
			minioResponse.setError(buildError(null, "Error occurred while onboarding user: " + userId));
		}

		UserVO userVO = new UserVO();
		userVO.setAccesskey(userId);
		userVO.setSecretKey(userSecretKey);
		minioResponse.setUser(userVO);
		return minioResponse;
	}

	@Override
	public MinioGenericResponse userRefresh(String userId) {
		MinioGenericResponse minioResponse = new MinioGenericResponse();
		try {
			MinioAdminClient minioAdminClient = minioConfig.getMinioAdminClient();
			LOGGER.info("Fetching user info from minio: {}", userId);
			Map<String, UserInfo> users = minioAdminClient.listUsers();
			// Creating new secret key for user
			String userSecretKey = UUID.randomUUID().toString();

			if (users.containsKey(userId)) {
				UserInfo userInfo = users.get(userId);

				LOGGER.info("Setting new secret key for existing user:{} to minio.", userId);
				minioAdminClient.addUser(userId, Status.ENABLED, userSecretKey, userInfo.policyName(),
						userInfo.memberOf());

			} else {
				LOGGER.info("Adding new user:{} to minio.", userId);
				minioAdminClient.addUser(userId, Status.ENABLED, userSecretKey, null, null);
			}

			LOGGER.info("Adding user to vault:{}", userId);
			addUserVault(userId, userSecretKey);

			UserVO userVO = new UserVO();
			userVO.setAccesskey(userId);
			userVO.setSecretKey(userSecretKey);
			minioResponse.setUser(userVO);
			minioResponse.setStatus(ConstantsUtility.SUCCESS);

		} catch (NoSuchAlgorithmException | InvalidKeyException | IOException | InvalidCipherTextException e) {
			LOGGER.error("DNA-MINIO-ERR-008::Error occured while refreshing user to minio: ", e.getMessage());
			minioResponse.setStatus(ConstantsUtility.FAILURE);
			minioResponse.setError(buildError(null, "Error occured while refreshing user to minio: " + userId));
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
		// Getting minio users from ehcache {minioUsersCache}
		LOGGER.debug("Getting minio users from ehcache: {}",ConstantsUtility.MINIO_USERS_CACHE);
		Map<String, UserInfo> usersInfo = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
		if (!usersInfo.isEmpty()) {
			LOGGER.debug("Got user info from {}.",ConstantsUtility.MINIO_USERS_CACHE);
			bucketCollaborators = new ArrayList<UserVO>();
			// Iterating over usersInfo map to get collaborators
			for (var entry : usersInfo.entrySet()) {
				if (StringUtils.hasText(entry.getValue().policyName()) && !entry.getKey().equals(currentUser)) {
					UserVO userVO = new UserVO();
					PermissionVO permissionVO = new PermissionVO();
					if (entry.getValue().policyName().contains(bucketName + "_" + ConstantsUtility.READ)) {
						LOGGER.debug("User:{} has read access to bucket:{}", entry.getKey(), bucketName);
						// Setting accesskey
						userVO.setAccesskey(entry.getKey());
						// Setting permission
						permissionVO.setRead(true);
						permissionVO.setWrite(false);
						userVO.setPermission(permissionVO);

					}
					if (entry.getValue().policyName().contains(bucketName + "_" + ConstantsUtility.READWRITE)) {
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
			LOGGER.info("Fetching users info from minio.");
			users = minioAdminClient.listUsers();
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidCipherTextException | IOException e) {
			LOGGER.error("Error occured while listing users from Minio:{}", e.getMessage());
		}
		return users;
	}

	@Override
	public Boolean validateUserInMinio(String userId) {
		boolean isUserExist = false;
		// Getting minio users from ehcache {minioUsersCache}
		LOGGER.debug("Getting minio users from ehcache: {}",ConstantsUtility.MINIO_USERS_CACHE);
		Map<String, UserInfo> usersInfo = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
		if (usersInfo.containsKey(userId)) {
			isUserExist = true;
		}
		return isUserExist;
	}


	// To create bucket policy
	private void CreateBucketPolicy(String policyName, String version, String resource, String action, String effect,
			String sid) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
		MinioAdminClient minioAdminClient = minioConfig.getMinioAdminClient();
		// Creating policy in minio
		LOGGER.debug("Creating {} Bucket policy", policyName);
		String policy = PolicyUtility.policyGenerator(resource, version, action, sid, effect);
		minioAdminClient.addCannedPolicy(policyName, policy);

	}

	/*
	 * Return vault Path where value will be written.
	 * 
	 */
	private String vaultPathUtility(String userId) {
		LOGGER.debug("Processing vaultPathUtility");
		return vaultPath + "/" + userId;
	}

	/*
	 * push host,port,scheme in VaultEndpoint
	 * 
	 * @return VaultEndpoint
	 */
	private VaultEndpoint getVaultEndpoint() {
		LOGGER.debug("Processing getVaultEndpoint");
		VaultEndpoint vaultEndpoint = new VaultEndpoint();
		vaultEndpoint.setScheme(vaultScheme);
		vaultEndpoint.setHost(vaultHost);
		vaultEndpoint.setPort(Integer.parseInt(vaultPort));
		return vaultEndpoint;
	}

	/*
	 * To add user in vault
	 * 
	 */
	private String addUserVault(String userId, String userSecretKey) {
		// Adding user in vault
		VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(), new TokenAuthentication(vaultToken));
		Map<String, String> secMap = new HashMap<String, String>();
		secMap.put(userId, userSecretKey);
		vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2).put(vaultPathUtility(userId), secMap);
		return userSecretKey;
	}

	@Override
	public String validateUserInVault(String userId) {
		String userSecretKey = "";

		LOGGER.info("Validating user:{} in Vault.",userId);
		VaultTemplate vaultTemplate = new VaultTemplate(this.getVaultEndpoint(), new TokenAuthentication(vaultToken));
		userSecretKey = vaultTemplate.opsForKeyValue(mountPath, KeyValueBackend.KV_2).get(vaultPathUtility(userId))
				.getData().get(userId).toString();

		return userSecretKey;
	}

}
