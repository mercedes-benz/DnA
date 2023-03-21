package com.daimler.data.application.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.daimler.data.dto.storage.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.forecast.CollaboratorVO;
import com.daimler.data.dto.forecast.CreatedByVO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StorageServicesClient {

	@Value("${storage.uri}")
	private String storageBaseUri;
	
	@Value("${databricks.userid}")
	private String dataBricksUser;
	
	@Value("${databricks.userauth}")
	private String dataBricksAuth;
	
	/*@Value("${databricks.defaultConfigYml}")
	private String defaultConfigFolderPath;*/

	private static final String BUCKETS_PATH = "/api/buckets";
	private static final String V1_BUCKETS_PATH = "/api/v1/buckets";
	private static final String UPLOADFILE_PATH = "/upload";
	private static final String INPUTS_PREFIX_PATH = "/inputs/";
	private static final String BUCKET_CLASSIFICATION = "Internal";
	private static final Boolean PII_DATE_DEFAULT = true;
	private static final Boolean TERMS_OF_USE = true;
	
	@Autowired
	HttpServletRequest httpRequest;
	
	@Autowired
	private RestTemplate restTemplate;
	
	public CreateBucketResponseWrapperDto createBucket(String bucketName,CreatedByVO creator, List<CollaboratorVO> collaborators) {
		CreateBucketResponseWrapperDto createBucketResponse = new CreateBucketResponseWrapperDto();
		List<MessageDescription> errors = new ArrayList<>();
		try {
				HttpHeaders headers = new HttpHeaders();
				String jwt = httpRequest.getHeader("Authorization");
				headers.set("Accept", "application/json");
				headers.set("Authorization", jwt);
				headers.set("chronos-api-key",dataBricksAuth);
				headers.setContentType(MediaType.APPLICATION_JSON);
				
				String uploadFileUrl = storageBaseUri + BUCKETS_PATH;
				CreateBucketRequestWrapperDto requestWrapper = new CreateBucketRequestWrapperDto();
				CreateBucketRequestDto data = new CreateBucketRequestDto();
				PermissionsDto permissions = new PermissionsDto();
				permissions.setRead(true);
				permissions.setWrite(false);
				data.setBucketName(bucketName);
				data.setClassificationType(BUCKET_CLASSIFICATION);
				data.setPiiData(PII_DATE_DEFAULT);
				data.setTermsOfUse(TERMS_OF_USE);
				if(collaborators!=null && !collaborators.isEmpty()) {
						List<CollaboratorsDto> bucketCollaborators = collaborators.stream().map
										(n -> { CollaboratorsDto collaborator = new CollaboratorsDto();
										BeanUtils.copyProperties(n,collaborator);
										collaborator.setAccesskey(n.getId());
										collaborator.setPermission(permissions);
										return collaborator;
								}).collect(Collectors.toList());
					data.setCollaborators(bucketCollaborators);
						
				}else {
					List<CollaboratorsDto> bucketCollaborators = new ArrayList<>();
					data.setCollaborators(bucketCollaborators);
				}
				
				CollaboratorsDto creatorAsCollab = new CollaboratorsDto();
				BeanUtils.copyProperties(creator,creatorAsCollab);
				creatorAsCollab.setAccesskey(creator.getId());
				creatorAsCollab.setPermission(permissions);
				data.getCollaborators().add(creatorAsCollab);
				
				CreatedByVO creatorChronosSystemUser = new CreatedByVO();
				creatorChronosSystemUser.setId(dataBricksUser);
				data.setCreatedBy(creatorChronosSystemUser);
				
				requestWrapper.setData(data);
				HttpEntity<CreateBucketRequestWrapperDto> requestEntity = new HttpEntity<>(requestWrapper,headers);
				ResponseEntity<CreateBucketResponseWrapperDto> response = restTemplate.exchange(uploadFileUrl, HttpMethod.POST,
						requestEntity, CreateBucketResponseWrapperDto.class);
				if (response.hasBody()) {
					createBucketResponse = response.getBody();
				}
				}catch(Exception e) {
					log.error("Failed while creating bucket {} with exception {}",  bucketName,e.getMessage());
					MessageDescription errMsg = new MessageDescription("Failed while creating bucket with exception " + e.getMessage());
					errors.add(errMsg);
					createBucketResponse.setErrors(errors);
					createBucketResponse.setStatus("FAILED");
				}
			return createBucketResponse;
	}

	public GetBucketByNameResponseWrapperDto getBucketDetailsByName(String bucketName) {
		GetBucketByNameResponseWrapperDto getBucketByNameResonse = new GetBucketByNameResponseWrapperDto();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			String jwt = httpRequest.getHeader("Authorization");
			headers.set("Accept", "application/json");
			headers.set("Authorization", jwt);
			headers.set("chronos-api-key", dataBricksAuth);
			headers.setContentType(MediaType.APPLICATION_JSON);
			String getBucketByNameUrl = storageBaseUri + BUCKETS_PATH + "/" + bucketName;

			HttpEntity<GetBucketByNameRequestWrapperDto> requestEntity = new HttpEntity<>(headers);
			ResponseEntity<GetBucketByNameResponseWrapperDto> response = restTemplate.exchange(getBucketByNameUrl, HttpMethod.GET, requestEntity, GetBucketByNameResponseWrapperDto.class);
			if (response.hasBody()) {
				getBucketByNameResonse = response.getBody();
			}
		} catch (Exception e) {
			log.error("Failed while getting the bucket details of {} with exception {}", bucketName, e.getMessage());
			MessageDescription errMsg = new MessageDescription("Failed while getting the bucket with name details with exception " + e.getMessage());
			errors.add(errMsg);
			getBucketByNameResonse.setErrors(errors);
			getBucketByNameResonse.setStatus("FAILED");
		}
		return getBucketByNameResonse;
	}

	public UpdateBucketResponseWrapperDto updateBucket(String bucketName, String bucketId, CreatedByVO creator, List<CollaboratorVO> collaborators) {
		UpdateBucketResponseWrapperDto updateBucketResponse = new UpdateBucketResponseWrapperDto();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			String jwt = httpRequest.getHeader("Authorization");
			headers.set("Accept", "application/json");
			headers.set("Authorization", jwt);
			headers.set("chronos-api-key", dataBricksAuth);
			headers.setContentType(MediaType.APPLICATION_JSON);

			String uploadFileUrl = storageBaseUri + BUCKETS_PATH;
			UpdateBucketRequestWrapperDto requestWrapper = new UpdateBucketRequestWrapperDto();
			UpdateBucketRequestDto data = new UpdateBucketRequestDto();
			PermissionsDto permissions = new PermissionsDto();
			permissions.setRead(true);
			permissions.setWrite(false);
			data.setBucketName(bucketName);
			data.setId(bucketId);
			data.setClassificationType(BUCKET_CLASSIFICATION);
			data.setPiiData(PII_DATE_DEFAULT);
			data.setTermsOfUse(TERMS_OF_USE);
			if (collaborators != null && !collaborators.isEmpty()) {
				List<CollaboratorsDto> bucketCollaborators = collaborators.stream().map(n -> {
					CollaboratorsDto collaborator = new CollaboratorsDto();
					BeanUtils.copyProperties(n, collaborator);
					collaborator.setAccesskey(n.getId());
					collaborator.setPermission(permissions);
					return collaborator;
				}).collect(Collectors.toList());
				data.setCollaborators(bucketCollaborators);

			} else {
				List<CollaboratorsDto> bucketCollaborators = new ArrayList<>();
				data.setCollaborators(bucketCollaborators);
			}

			CollaboratorsDto creatorAsCollab = new CollaboratorsDto();
			BeanUtils.copyProperties(creator, creatorAsCollab);
			creatorAsCollab.setAccesskey(creator.getId());
			creatorAsCollab.setPermission(permissions);
			data.getCollaborators().add(creatorAsCollab);

			CreatedByVO creatorChronosSystemUser = new CreatedByVO();
			creatorChronosSystemUser.setId(dataBricksUser);
			data.setCreatedBy(creatorChronosSystemUser);

			requestWrapper.setData(data);
			HttpEntity<UpdateBucketRequestWrapperDto> requestEntity = new HttpEntity<>(requestWrapper, headers);
			ResponseEntity<UpdateBucketResponseWrapperDto> response = restTemplate.exchange(uploadFileUrl, HttpMethod.PUT, requestEntity, UpdateBucketResponseWrapperDto.class);
			if (response.hasBody()) {
				updateBucketResponse = response.getBody();
			}
		} catch (Exception e) {
			log.error("Failed while updating the bucket {} with exception {}", bucketName, e.getMessage());
			MessageDescription errMsg = new MessageDescription("Failed while updating the bucket with exception " + e.getMessage());
			errors.add(errMsg);
			updateBucketResponse.setErrors(errors);
			updateBucketResponse.setStatus("FAILED");
		}
		return updateBucketResponse;
	}
	
	public FileUploadResponseDto uploadFile(String prefix,MultipartFile file, String bucketName) {
		FileUploadResponseDto uploadResponse = new FileUploadResponseDto();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()){
			    @Override
			    public String getFilename(){
			        return file.getOriginalFilename();                                          
			    }
			};
			return this.uploadFile(prefix, fileAsResource, bucketName);
		}catch(Exception e) {
			log.error("Failed while uploading file {} to minio bucket {} with exception {}", file.getOriginalFilename(), bucketName,e.getMessage());
			MessageDescription errMsg = new MessageDescription("Failed while uploading file with exception " + e.getMessage());
			errors.add(errMsg);
			uploadResponse.setErrors(errors);
			uploadResponse.setStatus("FAILED");
		}
		return uploadResponse;
	}
	
	
	public FileUploadResponseDto uploadFile(String prefix,ByteArrayResource file, String bucketName) {
		FileUploadResponseDto uploadResponse = new FileUploadResponseDto();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			String jwt = httpRequest.getHeader("Authorization");
			headers.set("Accept", "application/json");
			headers.set("Authorization", jwt);
			headers.set("chronos-api-key",dataBricksAuth);
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			LinkedMultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();
			ByteArrayResource fileAsResource = file;
			String uploadFileUrl = storageBaseUri + BUCKETS_PATH + "/" + bucketName + UPLOADFILE_PATH;
			HttpEntity<ByteArrayResource> attachmentPart = new HttpEntity<>(fileAsResource);
			multipartRequest.set("file",attachmentPart);
			multipartRequest.set("prefix",prefix);
			HttpEntity<LinkedMultiValueMap<String,Object>> requestEntity = new HttpEntity<>(multipartRequest,headers);
			ResponseEntity<FileUploadResponseDto> response = restTemplate.exchange(uploadFileUrl, HttpMethod.POST,
					requestEntity, FileUploadResponseDto.class);
			if (response.hasBody()) {
				uploadResponse = response.getBody();
			}
			}catch(Exception e) {
				log.error("Failed while uploading file {} to minio bucket {} with exception {}", file.getFilename(), bucketName,e.getMessage());
				MessageDescription errMsg = new MessageDescription("Failed while uploading file with exception " + e.getMessage());
				errors.add(errMsg);
				uploadResponse.setErrors(errors);
				uploadResponse.setStatus("FAILED");
			}
		return uploadResponse;
	}
	
	public FileDownloadResponseDto getFileContents(String bucketName, String path) {
		FileDownloadResponseDto downloadResponse = new FileDownloadResponseDto();
		ByteArrayResource data = null;
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			String jwt = httpRequest.getHeader("Authorization");
			headers.set("Accept", "application/json");
			headers.set("Authorization", jwt);
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("chronos-api-key",dataBricksAuth);
			HttpEntity requestEntity = new HttpEntity<>(headers);

			String getFileUrl = storageBaseUri + BUCKETS_PATH + "/" + bucketName + "/objects/metadata?prefix=" + path;

			ResponseEntity<ByteArrayResource> response = restTemplate.exchange(getFileUrl, HttpMethod.GET,requestEntity, ByteArrayResource.class);

			if (response.hasBody()) {
				data = response.getBody();
				downloadResponse.setData(data);
			}
			}catch(Exception e) {
				log.error("Failed while downloading result files {} from minio bucket {} with exception {}", path, bucketName,e.getMessage());
				MessageDescription errMsg = new MessageDescription("Failed while downloading file with exception " + e.getMessage());
				errors.add(errMsg);
				downloadResponse.setErrors(errors);
				downloadResponse.setData(data);

			}
		return downloadResponse;
	}
	
	
	public BucketObjectsCollectionWrapperDto getBucketObjects(String path) {
		BucketObjectsCollectionWrapperDto filesList = new BucketObjectsCollectionWrapperDto();
		ByteArrayResource data = null;
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			String jwt = httpRequest.getHeader("Authorization");
			headers.set("Accept", "application/json");
			headers.set("Authorization", jwt);
			headers.set("chronos-api-key",dataBricksAuth);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String getFilesListUrl = storageBaseUri + BUCKETS_PATH + "/" +path;
			ResponseEntity<BucketObjectsCollectionWrapperDto> response = restTemplate.exchange(getFilesListUrl, HttpMethod.GET,requestEntity, BucketObjectsCollectionWrapperDto.class);
			if (response.hasBody() && response.getBody()!=null) {
				if(response.getBody()!=null && response.getBody().getData()!=null && response.getBody().getData().getBucketObjects()!=null
						&& !response.getBody().getData().getBucketObjects().isEmpty()) {
					filesList = response.getBody();
					List<BucketObjectDetailsDto> filteredBucketObjects = filesList.getData().getBucketObjects().stream().
						filter(str -> str.getObjectName().endsWith(".yml") || str.getObjectName().endsWith(".yaml")).collect(Collectors.toList());
					filesList.getData().setBucketObjects(filteredBucketObjects);
				}
			}
			}catch(Exception e) {
				log.error("Failed while getting default config yamls and ymls from chronos-core/config path with exception {}", e.getMessage());
			}
		return filesList;
	}
	
	
	public Boolean isFilePresent(String fileNamePrefix,List<BucketObjectDetailsDto> bucketObjectDetails) {
		Boolean flag = false;
		if(bucketObjectDetails!=null && !bucketObjectDetails.isEmpty() && bucketObjectDetails.size()>0) {
			List<BucketObjectDetailsDto> filteredList=bucketObjectDetails.stream().filter(str -> (fileNamePrefix).equalsIgnoreCase(str.getObjectName())).collect(Collectors.toList());
			if(filteredList!=null && !filteredList.isEmpty() && filteredList.size()>0)
				flag = true;
		}
		return flag;
	}

	public List<BucketObjectDetailsDto> getFilesPresent(String bucketName,String prefix) {
		Boolean flag = false;
		BucketObjectsCollectionWrapperDto filesList = new BucketObjectsCollectionWrapperDto();
		ByteArrayResource data = null;
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			String jwt = httpRequest.getHeader("Authorization");
			headers.set("Accept", "application/json");
			headers.set("Authorization", jwt);
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("chronos-api-key",dataBricksAuth);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String getFilesListUrl = storageBaseUri + BUCKETS_PATH + "/" +bucketName+"/objects?prefix=" + prefix;
			ResponseEntity<BucketObjectsCollectionWrapperDto> response = restTemplate.exchange(getFilesListUrl, HttpMethod.GET,requestEntity, BucketObjectsCollectionWrapperDto.class);
			if (response.hasBody() && response.getBody()!=null) {
				if(response.getBody()!=null && response.getBody().getData()!=null && response.getBody().getData().getBucketObjects()!=null
						&& !response.getBody().getData().getBucketObjects().isEmpty()) {
					filesList = response.getBody();
					List<BucketObjectDetailsDto> filteredBucketObjects = filesList.getData().getBucketObjects().stream().
							collect(Collectors.toList());
					return filteredBucketObjects;

				}

			}
		}catch(Exception e) {
			log.error("Failed while getting  files from results path {}  with exception {}", bucketName+"/"+prefix, e.getMessage());
		}
		return null;

	}


	public DeleteBucketResponseWrapperDto deleteBucket(String bucketName) {
		DeleteBucketResponseWrapperDto deleteBucketResponse = new DeleteBucketResponseWrapperDto();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			DeleteBucketResponseDataDto data = new DeleteBucketResponseDataDto();
			HttpHeaders headers = new HttpHeaders();
			String jwt = httpRequest.getHeader("Authorization");
			headers.set("Accept", "application/json");
			headers.set("Authorization", jwt);
			headers.set("chronos-api-key",dataBricksAuth);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String apiUrl = storageBaseUri + BUCKETS_PATH + "/" + bucketName;
			ResponseEntity<DeleteBucketResponseWrapperDto> response = restTemplate.exchange(apiUrl, HttpMethod.DELETE, requestEntity, DeleteBucketResponseWrapperDto.class);
			if (response.hasBody()) {
				deleteBucketResponse = response.getBody();
			}
		} catch (Exception e) {
			MessageDescription errMsg = new MessageDescription("Failed to Delete bucket in minio storage " + e.getMessage());
			log.error(errMsg.getMessage());
			errors.add(errMsg);
			deleteBucketResponse.setErrors(errors);
			deleteBucketResponse.setStatus("FAILED");
		}

		return deleteBucketResponse;
	}
	
	public DeleteBucketResponseWrapperDto deleteBucketCascade(String bucketName) {
		DeleteBucketResponseWrapperDto deleteBucketResponse = new DeleteBucketResponseWrapperDto();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			DeleteBucketResponseDataDto data = new DeleteBucketResponseDataDto();
			HttpHeaders headers = new HttpHeaders();
			String jwt = httpRequest.getHeader("Authorization");
			headers.set("Accept", "application/json");
			headers.set("Authorization", jwt);
			headers.set("chronos-api-key",dataBricksAuth);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String apiUrl = storageBaseUri + V1_BUCKETS_PATH + "/" + bucketName;
			ResponseEntity<DeleteBucketResponseWrapperDto> response = restTemplate.exchange(apiUrl, HttpMethod.DELETE, requestEntity, DeleteBucketResponseWrapperDto.class);
			if (response.hasBody()) {
				deleteBucketResponse = response.getBody();
			}
		} catch (Exception e) {
			MessageDescription errMsg = new MessageDescription("Failed to delete Bucket Cascade in minio storage " + e.getMessage());
			log.error(errMsg.getMessage());
			errors.add(errMsg);
			deleteBucketResponse.setErrors(errors);
			deleteBucketResponse.setStatus("FAILED");
		}

		return deleteBucketResponse;
	}

	public Boolean isBucketExists(String bucketName) {
		Boolean isBucketPresent = false;
		try {
			HttpHeaders headers = new HttpHeaders();
			String jwt = httpRequest.getHeader("Authorization");
			headers.set("Accept", "application/json");
			headers.set("Authorization", jwt);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String getFilesListUrl = storageBaseUri + BUCKETS_PATH + "/" +bucketName + "/" + "present" ;
			ResponseEntity<BucketPresentResponseWrapperDto> response = restTemplate.exchange(getFilesListUrl, HttpMethod.GET,requestEntity, BucketPresentResponseWrapperDto.class);
			if (response.hasBody() && response.getBody()!=null) {
				if (response.hasBody()) {
					isBucketPresent = Boolean.valueOf(response.getBody().getIsBucketPresent());
				}
			}
		} catch(Exception e) {
			log.error("Failed to check isBucketExists {}  with exception {}", bucketName + e.getMessage());
		}
		return isBucketPresent;
	}
}
