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
import org.springframework.transaction.annotation.Isolation;
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
import com.daimler.data.dto.fabricWorkspace.CapacityVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceResponseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.common.BaseCommonService;

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
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public List<FabricWorkspaceVO> getAll( int limit,  int offset, String user) {
		List<FabricWorkspaceNsql> entities = customRepo.getAll(user, offset, limit);
		if (entities != null && !entities.isEmpty())
			return entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Long getCount(String user) {
		return customRepo.getTotalCount(user);
	}
	
	@Override
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
					data.setCapacity(capacityVO);
					FabricWorkspaceVO savedRecord = super.create(data);
					log.info("created workspace project {} with id {} saved to database successfully", vo.getName(), createResponse.getId());
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

}
