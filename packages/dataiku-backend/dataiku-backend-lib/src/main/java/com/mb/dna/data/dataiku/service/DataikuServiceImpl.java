package com.mb.dna.data.dataiku.service;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.mb.dna.data.api.controller.exceptions.GenericMessage;
import com.mb.dna.data.api.controller.exceptions.MessageDescription;
import com.mb.dna.data.application.adapter.dataiku.DataikuClient;
import com.mb.dna.data.application.adapter.dataiku.DataikuClientConfig;
import com.mb.dna.data.application.adapter.dataiku.DataikuProjectDetailsDto;
import com.mb.dna.data.application.adapter.dataiku.DataikuUserDto;
import com.mb.dna.data.assembler.DataikuAssembler;
import com.mb.dna.data.dataiku.api.dto.CollaboratorDetailsDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectResponseDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectUpdateDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectUpdateRequestDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectsCollectionDto;
import com.mb.dna.data.dataiku.db.entities.CollaboratorSql;
import com.mb.dna.data.dataiku.db.entities.DataikuSql;
import com.mb.dna.data.dataiku.db.repo.DataikuRepository;
import com.mb.dna.data.userprivilege.api.dto.UserPrivilegeResponseDto;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class DataikuServiceImpl implements DataikuService	{

	@Inject
	private DataikuRepository dataikuRepo;
	
	@Inject
	private DataikuAssembler assembler;
	
	@Inject
	DataikuClient dataikuClient;
	
	@Inject
	DataikuClientConfig dataikuClientConfig;
	
	public DataikuServiceImpl() {
		super();
	}
	
	public DataikuServiceImpl(DataikuRepository dataikuRepo, DataikuAssembler assembler, DataikuClient dataikuClient) {
		super();
		this.dataikuRepo = dataikuRepo;
		this.assembler = assembler;
		this.dataikuClient = dataikuClient;
	}
	
	@Override
	@Transactional
	public DataikuProjectsCollectionDto getAllDataikuProjects(String userId, int offset, int limit, String sortBy, String sortOrder, String projectName) {
		DataikuProjectsCollectionDto response = new DataikuProjectsCollectionDto();
		response.setData(new ArrayList<>());
		response.setTotalCount(new BigInteger("0"));
		List<DataikuSql> projects = dataikuRepo.getAll(userId, offset, limit, sortBy, sortOrder, projectName);
		List<DataikuProjectDto> data = projects.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
		response.setData(data);
		BigInteger count = dataikuRepo.getTotalCount(userId, projectName);
		response.setTotalCount(count);
		return response;
	}
	
	@Override
	@Transactional
	public DataikuProjectDto getById(String id) {
		DataikuProjectDto dto = new DataikuProjectDto();
		Optional<DataikuSql> result = dataikuRepo.findById(id);
		if(result!=null && result.get()!=null) {
			dto = assembler.toVo(result.get());
		}
		return dto;
	}
	
	
	@Override
	@Transactional
	public DataikuProjectDto getByProjectName(String projectName, String cloudProfile) {
		DataikuProjectDto dto = new DataikuProjectDto();
		DataikuSql result = dataikuRepo.findByProjectName(projectName,cloudProfile);
		if(result!=null && projectName.equalsIgnoreCase(result.getProjectName())) {
			dto = assembler.toVo(result);
		}
		return dto;
	}
	
	@Override
	@Transactional
	public DataikuProjectResponseDto provisionSolutionToDataikuProject(String projectName, String cloudProfile,
			String solutionId) {
		DataikuProjectResponseDto responseWrapperDto = new DataikuProjectResponseDto();
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			dataikuRepo.updateSolutionForDataiku(projectName, cloudProfile, solutionId);
			responseMessage.setSuccess("SUCCESS");
		}catch(Exception e) {
			log.error("Failed to update dataiku project {} at profile {} with solutionId {} with exception {}", projectName , cloudProfile , solutionId, e.getMessage());
			MessageDescription errMsg = new MessageDescription("Failed to update dataiku project " + projectName  
			+  " at " + cloudProfile  +" with exception " + e.getMessage());
			errors.add(errMsg);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseWrapperDto.setResponse(responseMessage);
		responseWrapperDto.setData(null);
		return responseWrapperDto;
	}
	
	
	@Override
	@Transactional
	public DataikuProjectResponseDto updateProject(String id,DataikuProjectUpdateRequestDto updateRequest, List<UserPrivilegeResponseDto> collabPrivilegeDetails) {
		DataikuProjectUpdateDto updateData = updateRequest.getData();
		DataikuProjectResponseDto responseWrapperDto = new DataikuProjectResponseDto();
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		DataikuProjectDto existingRecord = this.getById(id);
		
		if(updateData.getDescription()!=null)
			existingRecord.setDescription(updateData.getDescription());
		try {
			List<CollaboratorDetailsDto>  currentCollabs = updateData.getCollaborators();
			List<CollaboratorDetailsDto>  existingCollabs = existingRecord.getCollaborators();
			String envPrefix = dataikuClientConfig.getEnvironmentProfile();
			String cloudProfile = existingRecord.getCloudProfile();
			String groupPrefix = "";
			
			if("onPremise".equalsIgnoreCase(existingRecord.getCloudProfile())) {
				groupPrefix = dataikuClientConfig.getOnPremiseGroupNamePrefix();
			}else {
				groupPrefix = dataikuClientConfig.getExtolloGroupNamePrefix();
			}
			String projectName = existingRecord.getProjectName();
			String consolidatedPrefix = groupPrefix + envPrefix + projectName;
			String projectSpecificAdminAccessGroup = groupPrefix + envPrefix + projectName + "--ADMINISTRATOR";
			String projectSpecificContributorAccessGroup = groupPrefix + envPrefix + projectName + "--CONTRIBUTOR";
			String projectSpecificReadAccessGroup = groupPrefix + envPrefix + projectName + "--READ-ONLY";
			for(CollaboratorDetailsDto record : existingCollabs) {
				DataikuUserDto tempUserDetails = dataikuClient.getDataikuUser(record.getUserId().toUpperCase(),cloudProfile);
				if(tempUserDetails!=null) {
					List<String> currentGroups = tempUserDetails.getGroups();
					if(currentGroups!=null && !currentGroups.isEmpty()) 
						currentGroups.removeIf(n->n.contains(consolidatedPrefix+"--"));
					tempUserDetails.setGroups(currentGroups);
					log.info("Removing existing group with prefix {} for user {} ", consolidatedPrefix+"--",record.getUserId().toUpperCase() );
					MessageDescription UpdateTempCollabErrMsg = dataikuClient.updateUser(tempUserDetails,cloudProfile);
					if(UpdateTempCollabErrMsg!=null) {
						warnings.add(new MessageDescription("Failed to remove project group with prefix " + consolidatedPrefix + " for user " + record.getUserId() + ". Please update manually."));
					}
				}
			}
			if(currentCollabs!=null && !currentCollabs.isEmpty()) {
				currentCollabs.forEach(x -> {
					DataikuUserDto tempCollabUserDetails = dataikuClient.getDataikuUser(x.getUserId().toUpperCase(),cloudProfile);
					String groupName = "";
					String permission = x.getPermission();
					if("Administrator".equalsIgnoreCase(permission)) {
						groupName = projectSpecificAdminAccessGroup;
					}
					if("Contributor".equalsIgnoreCase(permission)) {
						groupName = projectSpecificContributorAccessGroup;
					}
					if("Reader".equalsIgnoreCase(permission)) {
						groupName = projectSpecificReadAccessGroup;
					}
					Optional<UserPrivilegeResponseDto> tempCollabUserPrivilegeResponseDtoOptional = collabPrivilegeDetails.stream().filter(n-> x.getUserId().equalsIgnoreCase(n.getData().getUserId())).findFirst();
					if(tempCollabUserDetails == null || tempCollabUserDetails.getLogin()==null) {
						tempCollabUserDetails = new DataikuUserDto();
						tempCollabUserDetails.setLogin(x.getUserId().toUpperCase());
						tempCollabUserDetails.setSourceType("LOCAL_NO_AUTH");
						tempCollabUserDetails.setDisplayName(x.getGivenName() + " " + x.getSurName());
						List<String> groups =new ArrayList<>();
						groups.add(groupName);
						tempCollabUserDetails.setGroups(groups);
						tempCollabUserDetails.setEmail(x.getUserId().toUpperCase());
						tempCollabUserDetails.setEnabled(true);
						if(tempCollabUserPrivilegeResponseDtoOptional.isPresent()) {
							tempCollabUserDetails.setUserProfile(tempCollabUserPrivilegeResponseDtoOptional.get().getData().getProfile());
						}
						MessageDescription onboardTempCollabErrMsg = dataikuClient.addUser(tempCollabUserDetails,cloudProfile);
						if(onboardTempCollabErrMsg!=null) {
							warnings.add(onboardTempCollabErrMsg);
						}
					}else {
						List<String> currentGroups = tempCollabUserDetails.getGroups();
						if(currentGroups==null || currentGroups.isEmpty()) 
							currentGroups = new ArrayList<>();
						currentGroups.add(groupName);
						tempCollabUserDetails.setGroups(currentGroups);
						if(tempCollabUserPrivilegeResponseDtoOptional.isPresent()) {
							tempCollabUserDetails.setUserProfile(tempCollabUserPrivilegeResponseDtoOptional.get().getData().getProfile());
						}
						log.info("Adding group {} for user {} ", groupName,x.getUserId().toUpperCase());
						MessageDescription UpdateTempCollabErrMsg = dataikuClient.updateUser(tempCollabUserDetails,cloudProfile);
						if(UpdateTempCollabErrMsg!=null) {
							warnings.add(UpdateTempCollabErrMsg);
						}
					}
				});
			}
			List<CollaboratorSql> updatedCollabs =  currentCollabs.stream().map(n -> assembler.toCollaboratorsData(n,id)).collect(Collectors.toList());
			existingRecord.setStatus(updateData.getStatus());
			existingRecord.setClassificationType(updateData.getClassificationType());
			existingRecord.setHasPii(updateData.getHasPii());
			existingRecord.setDivisionId(updateData.getDivisionId());
			existingRecord.setDivisionName(updateData.getDivisionName());
			existingRecord.setSubdivisionId(updateData.getSubdivisionId());
			existingRecord.setSubdivisionName(updateData.getSubdivisionName());
			existingRecord.setDepartment(updateData.getDepartment());
			DataikuSql entity = assembler.toEntity(existingRecord);
			entity.setCollaborators(updatedCollabs);
			dataikuRepo.update(entity);
			responseMessage.setSuccess("SUCCESS");
			
		}catch(Exception e) {
			log.error("Failed to update dataiku project {} with exception {}", existingRecord.getProjectName(), e.getMessage());
			MessageDescription errMsg = new MessageDescription("Failed to update dataiku project " + existingRecord.getProjectName() 
			+  " with exception " + e.getMessage());
			errors.add(errMsg);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseWrapperDto.setResponse(responseMessage);
		responseWrapperDto.setData(existingRecord);
		return responseWrapperDto;
			
	}
	
	
	@Override
	public boolean checkExistingProject(String projectName, String cloudProfile) {
		boolean isExisting = false;
		List<DataikuProjectDetailsDto> existingProjects = dataikuClient.getDataikuProjects(cloudProfile);
		if(existingProjects!=null && !existingProjects.isEmpty()) {
			
			Optional<DataikuProjectDetailsDto> projectFound = existingProjects.stream().filter(x -> projectName.equalsIgnoreCase(x.getProjectKey())).findAny();
	        if (projectFound.isPresent()) {
	        	isExisting = true;
	        }
		}
		return isExisting;
	}
	
	@Override
	@Transactional
	public DataikuProjectResponseDto createProject(String userId, DataikuProjectDto requestDto,UserPrivilegeResponseDto ownerDetails, List<UserPrivilegeResponseDto> collabPrivilegeDetails) {
		DataikuProjectResponseDto responseWrapperDto = new DataikuProjectResponseDto();
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		DataikuProjectDto dto = requestDto;
		try {
			String envPrefix = dataikuClientConfig.getEnvironmentProfile();
			String cloudProfile = dto.getCloudProfile();
			String groupPrefix = "";
			if("onPremise".equalsIgnoreCase(cloudProfile)) {
				groupPrefix = dataikuClientConfig.getOnPremiseGroupNamePrefix();
			}else {
				groupPrefix = dataikuClientConfig.getExtolloGroupNamePrefix();
			}
			String projectName = requestDto.getProjectName();
			MessageDescription updateErrMsg = dataikuClient.updateScenario(projectName,cloudProfile);
			if(updateErrMsg!=null) {
				errors.add(updateErrMsg);
				responseMessage.setErrors(errors);
				responseMessage.setWarnings(warnings);
				responseWrapperDto.setResponse(responseMessage);
				responseWrapperDto.setData(dto);
				log.info("Failed at updating scenario, returning from Service");
				return responseWrapperDto;
			}
			MessageDescription runErrMsg = dataikuClient.runScenario(projectName,cloudProfile);
			if(runErrMsg!=null) {
				errors.add(runErrMsg);
				responseMessage.setErrors(errors);
				responseMessage.setWarnings(warnings);
				responseWrapperDto.setResponse(responseMessage);
				responseWrapperDto.setData(dto);
				log.info("Failed at updating scenario, returning from Service");
				return responseWrapperDto;
			}
			DataikuUserDto ownerUserDetails = dataikuClient.getDataikuUser(userId.toUpperCase(),cloudProfile);
			String projectSpecificAdminAccessGroup = groupPrefix + envPrefix + projectName + "--ADMINISTRATOR";
			String projectSpecificContributorAccessGroup = groupPrefix + envPrefix + projectName + "--CONTRIBUTOR";
			String projectSpecificReadAccessGroup = groupPrefix + envPrefix + projectName + "--READ-ONLY";
			if(ownerUserDetails == null || ownerUserDetails.getLogin()==null) {
				ownerUserDetails = new DataikuUserDto();
				ownerUserDetails.setLogin(userId.toUpperCase());
				ownerUserDetails.setSourceType("LOCAL_NO_AUTH");
				ownerUserDetails.setDisplayName(ownerDetails.getData().getGivenName() + " " + ownerDetails.getData().getSurName());
				List<String> groups =new ArrayList<>();
				groups.add(projectSpecificAdminAccessGroup);
				ownerUserDetails.setGroups(groups);
				ownerUserDetails.setEmail(userId.toUpperCase());
				ownerUserDetails.setUserProfile(ownerDetails.getData().getProfile());
				ownerUserDetails.setEnabled(true);
				MessageDescription onboardOwnerErrMsg = dataikuClient.addUser(ownerUserDetails,cloudProfile);
				if(onboardOwnerErrMsg!=null) {
					errors.add(onboardOwnerErrMsg);
					responseMessage.setErrors(errors);
					responseMessage.setWarnings(warnings);
					responseWrapperDto.setResponse(responseMessage);
					responseWrapperDto.setData(dto);
					return responseWrapperDto;
				}
			}else {
				ownerUserDetails.setUserProfile(ownerDetails.getData().getProfile());
				ownerUserDetails.getGroups().add(projectSpecificAdminAccessGroup);
				MessageDescription UpdateOwnerErrMsg = dataikuClient.updateUser(ownerUserDetails,cloudProfile);
				if(UpdateOwnerErrMsg!=null) {
					errors.add(UpdateOwnerErrMsg);
					responseMessage.setErrors(errors);
					responseMessage.setWarnings(warnings);
					responseWrapperDto.setResponse(responseMessage);
					responseWrapperDto.setData(dto);
					return responseWrapperDto;
				}
			}
			List<CollaboratorDetailsDto> projectCollaborators = dto.getCollaborators();
			if(projectCollaborators!=null && !projectCollaborators.isEmpty()) {
				for(CollaboratorDetailsDto tempCollab: projectCollaborators) {
					DataikuUserDto tempCollabUserDetails = dataikuClient.getDataikuUser(tempCollab.getUserId().toUpperCase(),cloudProfile);
					String groupName = "";
					String permission = tempCollab.getPermission();
					if("Administrator".equalsIgnoreCase(permission)) {
						groupName = projectSpecificAdminAccessGroup;
					}
					if("Contributor".equalsIgnoreCase(permission)) {
						groupName = projectSpecificContributorAccessGroup;
					}
					if("Reader".equalsIgnoreCase(permission)) {
						groupName = projectSpecificReadAccessGroup;
					}
					Optional<UserPrivilegeResponseDto> tempCollabUserPrivilegeResponseDtoOptional = collabPrivilegeDetails.stream().filter(x-> tempCollab.getUserId().equalsIgnoreCase(x.getData().getUserId())).findFirst();
					if(tempCollabUserDetails == null || tempCollabUserDetails.getLogin()==null) {
						tempCollabUserDetails = new DataikuUserDto();
						tempCollabUserDetails.setLogin(tempCollab.getUserId().toUpperCase());
						tempCollabUserDetails.setSourceType("LOCAL_NO_AUTH");
						tempCollabUserDetails.setDisplayName(tempCollab.getGivenName() + " " + tempCollab.getSurName());
						List<String> groups =new ArrayList<>();
						groups.add(groupName);
						tempCollabUserDetails.setGroups(groups);
						tempCollabUserDetails.setEmail(tempCollab.getUserId().toUpperCase());
						if(tempCollabUserPrivilegeResponseDtoOptional.isPresent()) {
							tempCollabUserDetails.setUserProfile(tempCollabUserPrivilegeResponseDtoOptional.get().getData().getProfile());
						}
						tempCollabUserDetails.setEnabled(true);
						MessageDescription onboardTempCollabErrMsg = dataikuClient.addUser(tempCollabUserDetails,cloudProfile);
						if(onboardTempCollabErrMsg!=null) {
							errors.add(onboardTempCollabErrMsg);
							responseMessage.setErrors(errors);
							responseMessage.setWarnings(warnings);
							responseWrapperDto.setResponse(responseMessage);
							responseWrapperDto.setData(dto);
							return responseWrapperDto;
						}
					}else {
						if(tempCollabUserPrivilegeResponseDtoOptional.isPresent()) {
							tempCollabUserDetails.setUserProfile(tempCollabUserPrivilegeResponseDtoOptional.get().getData().getProfile());
						}
						tempCollabUserDetails.getGroups().add(groupName);
						MessageDescription UpdateTempCollabErrMsg = dataikuClient.updateUser(tempCollabUserDetails,cloudProfile);
						if(UpdateTempCollabErrMsg!=null) {
							errors.add(UpdateTempCollabErrMsg);
							responseMessage.setErrors(errors);
							responseMessage.setWarnings(warnings);
							responseWrapperDto.setResponse(responseMessage);
							responseWrapperDto.setData(dto);
							return responseWrapperDto;
						}
					}
				}
			}
			CollaboratorDetailsDto ownerAsAdminCollab = new CollaboratorDetailsDto();
			ownerAsAdminCollab.setGivenName(ownerDetails.getData().getGivenName());
			ownerAsAdminCollab.setPermission("administrator");
			ownerAsAdminCollab.setSurName(ownerDetails.getData().getSurName());
			ownerAsAdminCollab.setUserId(ownerDetails.getData().getUserId());
			projectCollaborators.add(ownerAsAdminCollab);
			requestDto.setCollaborators(projectCollaborators);
			
			DataikuSql entity = new DataikuSql();
			requestDto.setCreatedBy(userId);
			try {
				SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
				Date createdOn = isoFormat.parse(isoFormat.format(new Date()));
				requestDto.setCreatedOn(createdOn);
			}catch(Exception e) {
				
			}
			String id = UUID.randomUUID().toString();
			requestDto.setId(id);
			entity = assembler.toEntity(requestDto);
			dataikuRepo.save(entity);
			dto = requestDto;
			responseMessage.setSuccess("SUCCESS");
		}catch(Exception e) {
			log.error("Failed to save new dataiku project {} with exception {}", requestDto.getProjectName(), e.getMessage());
			MessageDescription errMsg = new MessageDescription("Failed to save new dataiku project " + requestDto.getProjectName() 
			+  " with exception " + e.getMessage());
			errors.add(errMsg);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseWrapperDto.setResponse(responseMessage);
		responseWrapperDto.setData(dto);
		return responseWrapperDto;
	}
	
	@Override
	@Transactional
	public GenericMessage deleteById(String id,DataikuProjectDto existingDto) {
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			List<String> users = new ArrayList<>();
			//createdby is also listed in collaborators. this is not required. users.add(existingDto.getCreatedBy());
			List<CollaboratorDetailsDto> collaborators = existingDto.getCollaborators();
			String envPrefix = dataikuClientConfig.getEnvironmentProfile();
			String cloudProfile = existingDto.getCloudProfile();
			String groupPrefix = "";
			if("onPremise".equalsIgnoreCase(existingDto.getCloudProfile())) {
				groupPrefix = dataikuClientConfig.getOnPremiseGroupNamePrefix();
			}else {
				groupPrefix = dataikuClientConfig.getExtolloGroupNamePrefix();
			}
			if(collaborators!=null && !collaborators.isEmpty()) {
				collaborators.forEach(n->users.add(n.getUserId()));
			}
			String projectName = existingDto.getProjectName();
			String consolidatedPrefix = groupPrefix + envPrefix + projectName;
			for(String record: users) {
				DataikuUserDto tempUserDetails = dataikuClient.getDataikuUser(record.toUpperCase(),cloudProfile);
				if(tempUserDetails!=null) {
					List<String> currentGroups = tempUserDetails.getGroups();
					if(currentGroups!=null && !currentGroups.isEmpty()) 
						currentGroups.removeIf(n->n.contains(consolidatedPrefix));
					tempUserDetails.setGroups(currentGroups);
					MessageDescription UpdateTempCollabErrMsg = dataikuClient.updateUser(tempUserDetails,cloudProfile);
					if(UpdateTempCollabErrMsg!=null) {
						warnings.add(new MessageDescription("Failed to remove project group with prefix " + consolidatedPrefix + " for user " + record + ". Please update manually."));
					}
				}
			}
			MessageDescription dataikuProjectDeleteResponse = dataikuClient.deleteProject(projectName, cloudProfile);
			if(dataikuProjectDeleteResponse!=null) {
				log.error("Failed to delete dataiku project with id {}",id);
				errors.add(dataikuProjectDeleteResponse);
			}else {
				dataikuRepo.deleteById(id);
				responseMessage.setSuccess("SUCCESS");
			}
		}catch(Exception e) {
			log.error("Failed to delete dataiku project with id {} and exception {}",id,e.getMessage());
			MessageDescription noRecordsMsg = new MessageDescription("Failed to delete dataiku project with exception " + e.getMessage());
			errors.add(noRecordsMsg);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		return responseMessage;
	}

	
	
}
