package com.mb.dna.data.dataiku.service;

import java.math.BigInteger;
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
import com.mb.dna.data.application.adapter.dataiku.DataikuUserDto;
import com.mb.dna.data.assembler.DataikuAssembler;
import com.mb.dna.data.dataiku.api.dto.CollaboratorDetailsDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectResponseDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectUpdateDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectUpdateRequestDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectsCollectionDto;
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
	public DataikuProjectDto getByProjectName(String projectName) {
		DataikuProjectDto dto = new DataikuProjectDto();
		DataikuSql result = dataikuRepo.findByProjectName(projectName);
		if(result!=null && projectName.equalsIgnoreCase(result.getProjectName())) {
			dto = assembler.toVo(result);
		}
		return dto;
	}
	
	@Override
	@Transactional
	public DataikuProjectResponseDto updateProject(String id,DataikuProjectUpdateRequestDto updateRequest) {
		DataikuProjectUpdateDto updateData = updateRequest.getData();
		DataikuProjectResponseDto responseWrapperDto = new DataikuProjectResponseDto();
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		DataikuProjectDto existingRecord = this.getById(id);
		existingRecord.setCollaborators(updateData.getCollaborators());
		if(updateData.getDescription()!=null)
			existingRecord.setDescription(updateData.getDescription());
		try {
			DataikuSql entity = assembler.toEntity(existingRecord);
			dataikuRepo.update(entity);
			responseMessage.setSuccess("SUCCESS");
		}catch(Exception e) {
			log.error("Failed to update dataiku project {} with exception {}", existingRecord.getProjectName(), e.getMessage());
			MessageDescription errMsg = new MessageDescription("Failed to save new dataiku project " + existingRecord.getProjectName() 
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
	@Transactional
	public DataikuProjectResponseDto createProject(String userId, DataikuProjectDto requestDto,UserPrivilegeResponseDto ownerDetails, List<UserPrivilegeResponseDto> collabPrivilegeDetails) {
		DataikuProjectResponseDto responseWrapperDto = new DataikuProjectResponseDto();
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		DataikuProjectDto dto = requestDto;
		try {
			String projectName = requestDto.getProjectName();
			MessageDescription updateErrMsg = dataikuClient.updateScenario(projectName);
			if(updateErrMsg!=null) {
				errors.add(updateErrMsg);
				responseMessage.setErrors(errors);
				responseMessage.setWarnings(warnings);
				responseWrapperDto.setResponse(responseMessage);
				responseWrapperDto.setData(dto);
				return responseWrapperDto;
			}
			MessageDescription runErrMsg = dataikuClient.runScenario(projectName);
			if(runErrMsg!=null) {
				errors.add(runErrMsg);
				responseMessage.setErrors(errors);
				responseMessage.setWarnings(warnings);
				responseWrapperDto.setResponse(responseMessage);
				responseWrapperDto.setData(dto);
				return responseWrapperDto;
			}
			String envPrefix = dataikuClientConfig.getEnvironmentProfile();
			String groupPrefix = "";
			if("onPremise".equalsIgnoreCase(dto.getCloudProfile())) {
				groupPrefix = dataikuClientConfig.getOnPremiseGroupNamePrefix();
			}else {
				groupPrefix = dataikuClientConfig.getExtolloGroupNamePrefix();
			}
			DataikuUserDto ownerUserDetails = dataikuClient.getDataikuUser(userId.toUpperCase());
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
				MessageDescription onboardOwnerErrMsg = dataikuClient.addUser(ownerUserDetails);
				if(onboardOwnerErrMsg!=null) {
					errors.add(onboardOwnerErrMsg);
					responseMessage.setErrors(errors);
					responseMessage.setWarnings(warnings);
					responseWrapperDto.setResponse(responseMessage);
					responseWrapperDto.setData(dto);
					return responseWrapperDto;
				}
			}else {
				ownerUserDetails.getGroups().add(projectSpecificAdminAccessGroup);
				MessageDescription UpdateOwnerErrMsg = dataikuClient.updateUser(ownerUserDetails);
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
					DataikuUserDto tempCollabUserDetails = dataikuClient.getDataikuUser(tempCollab.getUserId().toUpperCase());
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
					if(tempCollabUserDetails == null || tempCollabUserDetails.getLogin()==null) {
						tempCollabUserDetails = new DataikuUserDto();
						tempCollabUserDetails.setLogin(userId.toUpperCase());
						tempCollabUserDetails.setSourceType("LOCAL_NO_AUTH");
						tempCollabUserDetails.setDisplayName(tempCollab.getGivenName() + " " + tempCollab.getSurName());
						List<String> groups =new ArrayList<>();
						groups.add(groupName);
						tempCollabUserDetails.setGroups(groups);
						tempCollabUserDetails.setEmail(userId.toUpperCase());
						tempCollabUserDetails.setUserProfile(ownerDetails.getData().getProfile());
						tempCollabUserDetails.setEnabled(true);
						MessageDescription onboardTempCollabErrMsg = dataikuClient.addUser(tempCollabUserDetails);
						if(onboardTempCollabErrMsg!=null) {
							errors.add(onboardTempCollabErrMsg);
							responseMessage.setErrors(errors);
							responseMessage.setWarnings(warnings);
							responseWrapperDto.setResponse(responseMessage);
							responseWrapperDto.setData(dto);
							return responseWrapperDto;
						}
					}else {
						tempCollabUserDetails.getGroups().add(groupName);
						MessageDescription UpdateTempCollabErrMsg = dataikuClient.updateUser(tempCollabUserDetails);
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
			DataikuSql entity = new DataikuSql();
			requestDto.setCreatedBy(userId);
			requestDto.setCreatedOn(new Date());
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
			users.add(existingDto.getCreatedBy());
			List<CollaboratorDetailsDto> collaborators = existingDto.getCollaborators();
			if(collaborators!=null && !collaborators.isEmpty())
				collaborators.forEach(n->users.add(n.getUserId()));
			for(String record: users) {
				DataikuUserDto tempUserDetails = dataikuClient.getDataikuUser(record.toUpperCase());
				List<String> currentGroups = tempUserDetails.getGroups();
				String envPrefix = dataikuClientConfig.getEnvironmentProfile();
				String groupPrefix = "";
				if("onPremise".equalsIgnoreCase(existingDto.getCloudProfile())) {
					groupPrefix = dataikuClientConfig.getOnPremiseGroupNamePrefix();
				}else {
					groupPrefix = dataikuClientConfig.getExtolloGroupNamePrefix();
				}
				String projectName = existingDto.getProjectName();
				String consolidatedPrefix = groupPrefix + envPrefix + projectName;
				currentGroups.removeIf(n->n.contains(consolidatedPrefix));
				tempUserDetails.setGroups(currentGroups);
				MessageDescription UpdateTempCollabErrMsg = dataikuClient.updateUser(tempUserDetails);
				if(UpdateTempCollabErrMsg!=null) {
					warnings.add(new MessageDescription("Failed to remove project group for user " + record + ". Please update manually."));
				}
			}
			dataikuRepo.deleteById(id);
			responseMessage.setSuccess("SUCCESS");
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
