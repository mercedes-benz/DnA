package com.daimler.data.service.forecast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.application.client.FabricWorkspaceClient;
import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.repo.forecast.FabricWorkspaceCustomRepository;
import com.daimler.data.db.repo.forecast.FabricWorkspaceRepository;
import com.daimler.data.dto.fabric.CreateWorkspaceDto;
import com.daimler.data.dto.fabric.ErrorResponseDto;
import com.daimler.data.dto.fabric.WorkspaceDetailDto;
import com.daimler.data.dto.fabric.WorkspaceUpdateDto;
import com.daimler.data.dto.fabricWorkspace.CapacityVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceResponseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.tag.TagVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.tag.TagService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseFabricWorkspaceService extends BaseCommonService<FabricWorkspaceVO, FabricWorkspaceNsql, String> implements FabricWorkspaceService{

	@Autowired
	private FabricWorkspaceClient fabricWorkspaceClient;
	
	@Autowired
	private FabricWorkspaceCustomRepository customRepo;
	
	@Autowired
	private FabricWorkspaceRepository jpaRepo;
	
	@Autowired
	private FabricWorkspaceAssembler assembler;

	@Autowired
	private TagService tagService;
	
	@Value("${fabricWorkspaces.capacityId}")
	private String capacityId;
	
	@Value("${fabricWorkspaces.capacityName}")
	private String capacityName;
	
	@Value("${fabricWorkspaces.capacitySku}")
	private String capacitySku;
	
	@Value("${fabricWorkspaces.capacityRegion}")
	private String capacityRegion;
	
	@Value("${fabricWorkspaces.capacityState}")
	private String capacityState;
	
	public BaseFabricWorkspaceService() {
		super();
	}

	@Override
	@Transactional
	public List<FabricWorkspaceVO> getAll( int limit,  int offset, String user) {
		List<FabricWorkspaceVO> vos = new ArrayList<>();
		List<FabricWorkspaceNsql> entities = customRepo.getAll(user, offset, limit);
		if (entities != null && !entities.isEmpty()) {
			for(FabricWorkspaceNsql entity : entities) {
				try {
					String id = entity.getId();
					log.info("Fetched fabric project record from db successfully for id {} ", id);
					WorkspaceDetailDto dtoFromFabric = fabricWorkspaceClient.getWorkspaceDetails(id);
					if(dtoFromFabric!=null) {
						if(dtoFromFabric.getErrorCode()!=null && ("WorkspaceNotFound".equalsIgnoreCase(dtoFromFabric.getErrorCode()) || "InsufficientPrivileges".equalsIgnoreCase(dtoFromFabric.getErrorCode()))) {
								log.info("No fabric project with id {} found at Microsoft Fabric, WorkspaceNotFound error.", id);
								jpaRepo.deleteById(id);
								log.info("Project id {} not found in Microsoft Fabric, hence successfully removed from database.", id);
						}else {
							entity.getData().setName(dtoFromFabric.getDisplayName());
							entity.getData().setDescription(dtoFromFabric.getDescription());
							jpaRepo.save(entity);
							FabricWorkspaceVO updatedVO = assembler.toVo(entity);
							vos.add(updatedVO);
						}
					}
				}catch(Exception e) {
					log.error("Failed to update Fabric workspace record of id {} during get all records",entity.getId());
					FabricWorkspaceVO updatedVO = assembler.toVo(entity);
					vos.add(updatedVO);
				}
			}
		}
		return vos;
	}

	@Override
	public Long getCount(String user) {
		return customRepo.getTotalCount(user);
	}
	
	@Override
	@Transactional
	public FabricWorkspaceVO getById(String id) {
		FabricWorkspaceVO voFromDb =  super.getById(id);
		log.info("Fetched fabric project record from db successfully for id {} ", id);
		WorkspaceDetailDto dtoFromFabric = fabricWorkspaceClient.getWorkspaceDetails(id);
		if(dtoFromFabric!=null) {
			if(dtoFromFabric.getErrorCode()!=null && ("WorkspaceNotFound".equalsIgnoreCase(dtoFromFabric.getErrorCode()) || "InsufficientPrivileges".equalsIgnoreCase(dtoFromFabric.getErrorCode()))) {
				log.info("No fabric project with id {} found at Microsoft Fabric, WorkspaceNotFound error.", id);
				try{
					jpaRepo.deleteById(id);
					log.info("Project id {} not found in Microsoft Fabric, hence successfully removed from database.", id);
				}catch(Exception e) {
					log.error("Project id {} not found in Microsoft Fabric. Failed to remove from database, will remove in next fetch", id);
				}
				return null;
			}
			voFromDb.setName(dtoFromFabric.getDisplayName());
			voFromDb.setDescription(dtoFromFabric.getDescription());
			try {
				FabricWorkspaceNsql updatedEntity = assembler.toEntity(voFromDb);
				log.info("Successfully updated latest displayName and description from Fabric to Database for project id {}", id);
				jpaRepo.save(updatedEntity);
			}catch(Exception e) {
				log.error("Failed to update latest displayName and description from Fabric to Database for project id {} . Will be updated in next fetch", id);
			}
		}
		return voFromDb;
	}

	@Override
	@Transactional
	public ResponseEntity<FabricWorkspaceResponseVO> createWorkspace(FabricWorkspaceVO vo) {
		FabricWorkspaceResponseVO responseData = new FabricWorkspaceResponseVO();
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		CreateWorkspaceDto createRequest = new CreateWorkspaceDto();
		createRequest.setDescription(vo.getDescription());
		createRequest.setDisplayName(vo.getName());
		try {
			WorkspaceDetailDto createResponse = fabricWorkspaceClient.createWorkspace(createRequest);
			if(createResponse!=null ) {
				if(createResponse.getErrorCode() != null ) {
					MessageDescription message = new MessageDescription();
					message.setMessage("Failed to create workspace. Error is : " + createResponse.getMessage());
					errors.add(message);
					responseMessage.setErrors(errors);
					responseMessage.setSuccess("FAILED");
					responseData.setData(vo);
					responseData.setResponses(responseMessage);
					log.error("Error occurred:{} while creating fabric workspace project {} ", createResponse.getErrorCode(), vo.getName());
					if("409".equalsIgnoreCase(createResponse.getErrorCode())) {
						return new ResponseEntity<>(responseData, HttpStatus.CONFLICT);
					}else {
						return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
				if(createResponse.getId()!=null) {
					log.info("created fabric workspace project {} successfully with id {}", vo.getName(), createResponse.getId());
					GenericMessage addUserResponse = fabricWorkspaceClient.addUser(createResponse.getId(), vo.getCreatedBy().getEmail());
					if(addUserResponse == null || !"SUCCESS".equalsIgnoreCase(addUserResponse.getSuccess())) {
						log.error("Failed to add user {} to workspace {}", vo.getCreatedBy().getEmail(), createResponse.getId());
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to add user to created workspace " + vo.getName() + " with id" + createResponse.getId() + ". Please contact Admin.");
						errors.add(message);
						responseMessage.setErrors(errors);
						responseMessage.setSuccess("FAILED");
						responseData.setData(vo);
						responseData.setResponses(responseMessage);
						return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
					}else {
						log.info("Successfully added  user {} to workspace {} ", vo.getCreatedBy().getEmail(), createResponse.getId());
					}
					
					
					GenericMessage addGroupResponse = fabricWorkspaceClient.addGroup(createResponse.getId());
					if(addGroupResponse == null || !"SUCCESS".equalsIgnoreCase(addGroupResponse.getSuccess())) {
						log.error("Failed to add default group to workspace {}", createResponse.getId());
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to add default group to created workspace " + vo.getName() + ". Please add Default Group to your workspace manually or contact Admin.");
						warnings.add(message);
					}else {
						log.info("Successfully added  default Group to workspace {} ", createResponse.getId());
					}
					
					
					FabricWorkspaceVO data = new FabricWorkspaceVO();
					BeanUtils.copyProperties(vo, data);
					data.setId(createResponse.getId());
					data.setHasPii(vo.isHasPii());
					ErrorResponseDto assignCapacityResponse = fabricWorkspaceClient.assignCapacity(createResponse.getId());
					CapacityVO capacityVO = new CapacityVO();
					if(assignCapacityResponse!=null && assignCapacityResponse.getErrorCode()!=null && "500".equalsIgnoreCase(assignCapacityResponse.getErrorCode())) {
						capacityVO = null;
						warnings.add(new MessageDescription("Failed to assign capacity, please reassign or update workspace to assign capacity automatically."));
					}else {
						capacityVO.setId(capacityId);
						capacityVO.setName(capacityName);
						capacityVO.setRegion(capacityRegion);
						capacityVO.setSku(capacitySku);
						capacityVO.setState(capacityState);
					}
					updateTags(data);
					data.setCapacity(capacityVO);
					FabricWorkspaceVO savedRecord = super.create(data);
					log.info("created workspace project {} with id {} saved to database successfully", vo.getName(), createResponse.getId());
					fabricWorkspaceClient.provisionWorkspace(createResponse.getId());
					responseData.setData(savedRecord);
					responseMessage.setSuccess("SUCCESS");
					responseMessage.setErrors(errors);
					responseMessage.setWarnings(warnings);
					responseData.setResponses(responseMessage);
					return new ResponseEntity<>(responseData, HttpStatus.CREATED);
				}
				
			}
		}catch(Exception e) {
			GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to create workspace due to internal error");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
			responseData.setData(vo);
			responseData.setResponses(failedResponse);
			log.error("Exception occurred:{} while creating fabric workspace project {} ", e.getMessage(), vo.getName());
			return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		GenericMessage failedResponse = new GenericMessage();
		List<MessageDescription> messages = new ArrayList<>();
		MessageDescription message = new MessageDescription();
		message.setMessage("Failed to create workspace due to internal error. Empty response from Fabric create workspace");
		messages.add(message);
		failedResponse.addErrors(message);
		failedResponse.setSuccess("FAILED");
		responseData.setData(vo);
		responseData.setResponses(failedResponse);
		log.error("Empty response for create workspace from fabric. Failed while creating fabric workspace project {} ", vo.getName());
		return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
		
	}

	@Override
	@Transactional
	public GenericMessage delete(String id) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			ErrorResponseDto deleteResponse = fabricWorkspaceClient.deleteWorkspace(id);
			if(deleteResponse!=null && deleteResponse.getMessage() != null) {
					MessageDescription message = new MessageDescription();
					message.setMessage(deleteResponse.getMessage());
					errors.add(message);
					responseMessage.setErrors(errors);
					responseMessage.setSuccess("FAILED");
					log.error("Error occurred:{} while deleting fabric workspace project {} ", id);
					return responseMessage;
			}
			super.deleteById(id);
			responseMessage.setSuccess("SUCCESS");
			responseMessage.setErrors(errors);
			responseMessage.setWarnings(warnings);
			return responseMessage;
		}catch(Exception e) {
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to delete workspace with error : " + e.getMessage());
			errors.add(message);
			responseMessage.setErrors(errors);
			responseMessage.setSuccess("FAILED");
			log.error("Error occurred:{} while deleting fabric workspace project {} ", id);
			return responseMessage;
		}
	}

	@Override
	@Transactional
	public FabricWorkspaceVO updateFabricProject(FabricWorkspaceVO existingFabricWorkspace) {
		WorkspaceUpdateDto updateRequest = new WorkspaceUpdateDto();
		try {
			updateRequest.setDescription(existingFabricWorkspace.getDescription());
			updateRequest.setDisplayName(existingFabricWorkspace.getName());
			WorkspaceDetailDto updateResponse = fabricWorkspaceClient.updateWorkspace(existingFabricWorkspace.getId(), updateRequest);
		}catch(Exception e) {
			log.error("Failed to update project {} details in MicrosoftFabric, Will be updated in next action.", existingFabricWorkspace.getId());
		}
		FabricWorkspaceNsql updatedEntity = assembler.toEntity(existingFabricWorkspace);
		updateTags(existingFabricWorkspace);
		jpaRepo.save(updatedEntity);
		return existingFabricWorkspace;
	}

	private void updateTags(FabricWorkspaceVO vo) {
		List<String> tags = vo.getTags();
		if (tags != null && !tags.isEmpty()) {
			tags.forEach(tag -> {
				TagVO existingTagVO = tagService.getByUniqueliteral("name", tag);
				if (existingTagVO != null && existingTagVO.getName() != null
						&& existingTagVO.getName().equalsIgnoreCase(tag))
					return;
				else {
					TagVO newTagVO = new TagVO();
					newTagVO.setId(null);
					newTagVO.setName(tag);
					tagService.create(newTagVO);
				}
			});
		}
	}

}
