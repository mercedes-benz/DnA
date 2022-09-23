package com.daimler.data.application.client;

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

import com.daimler.data.dto.CollaboratorsDto;
import com.daimler.data.dto.CreateBucketRequestDto;
import com.daimler.data.dto.CreateBucketRequestWrapperDto;
import com.daimler.data.dto.CreateBucketResponseWrapperDto;
import com.daimler.data.dto.FileUploadResponseDto;
import com.daimler.data.dto.PermissionsDto;
import com.daimler.data.dto.forecast.CollaboratorVO;
import com.daimler.data.dto.forecast.CreatedByVO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StorageServicesClient {

	@Value("${storage.uri}")
	private String storageBaseUri;

	private static final String BUCKETS_PATH = "/api/buckets";
	private static final String UPLOADFILE_PATH = "/upload";
	private static final String INPUTS_PREFIX_PATH = "inputs/";
	private static final String BUCKET_CLASSIFICATION = "Internal";
	private static final Boolean PII_DATE_DEFAULT = false;
	private static final Boolean TERMS_OF_USE = true;
	
	@Autowired
	HttpServletRequest httpRequest;
	
	@Autowired
	private RestTemplate restClient;
	
	public CreateBucketResponseWrapperDto createBucket(String bucketName,CreatedByVO creator, List<CollaboratorVO> collaborators, Boolean piiData) {
		CreateBucketResponseWrapperDto createBucketResponse = null;
		try {
				String jwt = httpRequest.getHeader("Authorization");
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", "application/json");
				headers.set("Authorization", jwt);
				headers.setContentType(MediaType.APPLICATION_JSON);
				
				String uploadFileUrl = storageBaseUri + BUCKETS_PATH;
				CreateBucketRequestWrapperDto requestWrapper = new CreateBucketRequestWrapperDto();
				CreateBucketRequestDto data = new CreateBucketRequestDto();
				PermissionsDto permissions = new PermissionsDto();
				permissions.setRead(true);
				permissions.setWrite(false);
				data.setBucketName(bucketName);
				data.setClassificationType(BUCKET_CLASSIFICATION);
				if(piiData)
					data.setPiiData(piiData);
				else
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
				}
				data.setCreator(creator);
				requestWrapper.setData(data);
				HttpEntity<CreateBucketRequestWrapperDto> requestEntity = new HttpEntity<>(requestWrapper,headers);
				ResponseEntity<CreateBucketResponseWrapperDto> response = restClient.exchange(uploadFileUrl, HttpMethod.POST,
						requestEntity, CreateBucketResponseWrapperDto.class);
				if (response.hasBody()) {
					createBucketResponse = response.getBody();
				}
				}catch(Exception e) {
					e.printStackTrace();
				}
			return createBucketResponse;
	}
	
	public FileUploadResponseDto uploadFile(MultipartFile file,String bucketName) {
		FileUploadResponseDto uploadResponse = null;
		try {
			String jwt = httpRequest.getHeader("Authorization");
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", jwt);
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
			ResponseEntity<FileUploadResponseDto> response = restClient.exchange(uploadFileUrl, HttpMethod.POST,
					requestEntity, FileUploadResponseDto.class);
			if (response.hasBody()) {
				uploadResponse = response.getBody();
			}
			}catch(Exception e) {
				e.printStackTrace();
			}
		return uploadResponse;
	}
	
}
