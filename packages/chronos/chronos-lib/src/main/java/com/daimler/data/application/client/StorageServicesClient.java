package com.daimler.data.application.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

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
import com.daimler.data.dto.storage.BucketObjectDetailsDto;
import com.daimler.data.dto.storage.BucketObjectsCollectionWrapperDto;
import com.daimler.data.dto.storage.CollaboratorsDto;
import com.daimler.data.dto.storage.CreateBucketRequestDto;
import com.daimler.data.dto.storage.CreateBucketRequestWrapperDto;
import com.daimler.data.dto.storage.CreateBucketResponseWrapperDto;
import com.daimler.data.dto.storage.FileDownloadResponseDto;
import com.daimler.data.dto.storage.FileUploadResponseDto;
import com.daimler.data.dto.storage.PermissionsDto;

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
	
	@Value("${databricks.defaultConfigYml}")
	private String defaultConfigFolderPath;

	private static final String BUCKETS_PATH = "/api/buckets";
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
				data.setCreator(creatorChronosSystemUser);
				
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
	
	public FileUploadResponseDto uploadFile(MultipartFile file,String bucketName) {
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
	
			ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()){
			    @Override
			    public String getFilename(){
			        return file.getOriginalFilename();                                          
			    }
			};
			String uploadFileUrl = storageBaseUri + BUCKETS_PATH + "/" + bucketName + UPLOADFILE_PATH;
			HttpEntity<ByteArrayResource> attachmentPart = new HttpEntity<>(fileAsResource);
			multipartRequest.set("file",attachmentPart);
			multipartRequest.set("prefix",INPUTS_PREFIX_PATH);
			HttpEntity<LinkedMultiValueMap<String,Object>> requestEntity = new HttpEntity<>(multipartRequest,headers);
			ResponseEntity<FileUploadResponseDto> response = restTemplate.exchange(uploadFileUrl, HttpMethod.POST,
					requestEntity, FileUploadResponseDto.class);
			if (response.hasBody()) {
				uploadResponse = response.getBody();
			}
			}catch(Exception e) {
				log.error("Failed while uploading file {} to minio bucket {} with exception {}", file.getOriginalFilename(), bucketName,e.getMessage());
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
	
	
	public BucketObjectsCollectionWrapperDto getBucketObjects() {
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
			String getFilesListUrl = storageBaseUri + BUCKETS_PATH + "/" +defaultConfigFolderPath;
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
	
	
	public Boolean getBucketObjects(String bucketName,String prefix) {
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
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String getFilesListUrl = storageBaseUri + BUCKETS_PATH + "/" +bucketName+"/objects?prefix=" + prefix;
			ResponseEntity<BucketObjectsCollectionWrapperDto> response = restTemplate.exchange(getFilesListUrl, HttpMethod.GET,requestEntity, BucketObjectsCollectionWrapperDto.class);
			if (response.hasBody() && response.getBody()!=null) {
				if(response.getBody()!=null && response.getBody().getData()!=null && response.getBody().getData().getBucketObjects()!=null
						&& !response.getBody().getData().getBucketObjects().isEmpty()) {
					filesList = response.getBody();
					List<BucketObjectDetailsDto> filteredBucketObjects = filesList.getData().getBucketObjects().stream().
						filter(str -> "SUCCESS".equalsIgnoreCase(str.getObjectName())).collect(Collectors.toList());
					if(filteredBucketObjects!=null && !filteredBucketObjects.isEmpty() && filteredBucketObjects.size()>0)
						flag = true;
				}
			}
			}catch(Exception e) {
				log.error("Failed while getting SUCCESS file from results path {}  with exception {}", bucketName+"/"+prefix, e.getMessage());
			}
		return flag;
	}
	
	
}
