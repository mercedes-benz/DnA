package com.daimler.data.service.adaProjects;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.application.client.FabricWorkspaceClient;
import com.daimler.data.assembler.ADAProjectsAssembler;
import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.db.entities.ADAProjectsNsql;
import com.daimler.data.db.entities.CapacityNsql;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.json.Capacity;
import com.daimler.data.db.repo.adaProjects.ADAProjectsCustomRepository;
import com.daimler.data.db.repo.adaProjects.ADAProjectsRepository;
import com.daimler.data.db.repo.capacity.CapacityRepository;
import com.daimler.data.db.repo.fabric.FabricWorkspaceCustomRepository;
import com.daimler.data.db.repo.fabric.FabricWorkspaceRepository;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsCollectionVO;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVO;
import com.daimler.data.dto.adaProjects.CapacityVO;
import com.daimler.data.dto.fabric.ErrorResponseDto;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.common.BaseCommonService;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BaseADAProjectsService extends BaseCommonService<ADAProjectDetailsVO, ADAProjectsNsql, String>
		implements ADAProjectsService {

	@Autowired
	private ADAProjectsRepository jpaRepo;

	@Autowired
	private ADAProjectsCustomRepository customRepo;

	@Autowired
	private ADAProjectsAssembler assembler;

	@Autowired
	private FabricWorkspaceAssembler fabricWorkspaceAssembler;

	@Autowired
	private FabricWorkspaceRepository fabricWorkspaceRepo;

	@Autowired
	private FabricWorkspaceCustomRepository fabricWorkspaceCustomRepository;

	@Autowired 
	private FabricWorkspaceClient fabricWorkspaceClient;

	@Value("${fabricWorkspaces.fabricCapacityId}")
	private String fabricCapacityId;

	@Value("${fabricWorkspaces.fabricCapacityName}")
	private String fabricCapacityName;
	
	@Value("${fabricWorkspaces.capacitySku}")
	private String capacitySku;
	
	@Value("${fabricWorkspaces.capacityRegion}")
	private String capacityRegion;
	
	@Value("${fabricWorkspaces.capacityState}")
	private String capacityState;

	@Autowired
	private CapacityRepository capacityRepo;

	public BaseADAProjectsService() {
		super();
	}

	@Override
	public ADAProjectDetailsCollectionVO getAllProjects(int limit, int offset, String createdBy) {
		ADAProjectDetailsCollectionVO collection = new ADAProjectDetailsCollectionVO();
		GenericMessage message = new GenericMessage();
		List<MessageDescription> errors = null;
		List<MessageDescription> warnings = null;

		try {
			List<ADAProjectsNsql> adaProjects;
			
			
			if ("ada".equalsIgnoreCase(createdBy)) {
				adaProjects = customRepo.findAll(limit, offset);
			} else if (createdBy != null && !createdBy.isBlank()) {
				adaProjects = customRepo.findAllByCreator(createdBy, limit, offset);
			} else {
				log.warn("Attempt to fetch projects with no 'createdBy' marker.");
				adaProjects = new ArrayList<>(); 
			}

			List<ADAProjectDetailsVO> projects = adaProjects.stream()
					.map(project -> assembler.toVo(project))
					.collect(Collectors.toList());
			collection.setRecords(projects);
			
			message.setSuccess("SUCCESS");

		} catch (Exception e) {
			log.error("Error fetching ADA Projects", e);
			errors = List.of(new MessageDescription("Failed to fetch projects with error: " + e.getMessage()));
			message.setErrors(errors);
			message.setSuccess("ERROR");
			collection.responses(message);
			
			return collection; 
		}

		if (errors != null) {
			message.setErrors(errors);
		}
		if (warnings != null) {
			message.setWarnings(warnings);
		}
		collection.responses(message);
		return collection;
	}

	@Override
	@Transactional
	public GenericMessage createNewProject(ADAProjectDetailsVO project) {
		GenericMessage message = new GenericMessage();
		try {
			log.info("Creating new ADA Project with projectID {}", project.getProjectID());
			updateCapacityForFabricWorkspaces(project);
			project.setRegion(project.getCapacity().getRegion());
			log.info("Successfully updated capacity for associated Fabric workspaces for project id {}", project.getProjectID());
			ADAProjectsNsql entity = assembler.toEntity(project);
			jpaRepo.save(entity);
			message.setSuccess("CREATED");
		} catch (Exception e) {
			log.error("Error creating ADA Project", e);
			message.setErrors(List.of(new MessageDescription("Failed to create project with id : " + project.getProjectID() +", with error : "+e.getMessage())));
			message.setSuccess("ERROR");
		}
		return message;
	}
	
	@Override
	@Transactional
	public GenericMessage updateProject(String id, ADAProjectDetailsVO project) {
		GenericMessage message = new GenericMessage();
		try {
			
			ADAProjectsNsql existingEntity = customRepo.findByProjectId(project.getProjectID());
			if(existingEntity == null) {
				log.error("ADA Project with projectID {} not found in adaprojects table", project.getProjectID());
				message.setErrors(List.of(new MessageDescription("ADA Project with projectID " + project.getProjectID() + 
				" not found in adaprojects table")));
				message.setSuccess("ERROR");
				return message;
			} 
			if (existingEntity.getData() != null && existingEntity.getData().getRegion() != null && 
			!existingEntity.getData().getRegion().equalsIgnoreCase(project.getRegion())) {
				log.error("Region mismatch for projectID {}. Existing region: {}, New region: {}", 
				project.getProjectID(), existingEntity.getData().getRegion(), project.getRegion());
				message.setErrors(List.of(new MessageDescription("Region mismatch for projectID " + 
				project.getProjectID() + ". Existing region: " + existingEntity.getData().getRegion() + ", New region: " + project.getRegion())));
				message.setSuccess("ERROR");
				return message;
			}

			updateCapacityForFabricWorkspaces(project);
			log.info("Successfully updated capacity for associated Fabric workspaces for project id {}", project.getProjectID());
			ADAProjectsNsql entity = assembler.toEntity(project);
			entity.setId(id);
			jpaRepo.save(entity);
			message.setSuccess("UPDATED");
		} catch (Exception e) {
			log.error("Error updating ADA Project", e);
			message.setErrors(List.of(new MessageDescription("Failed to update project with id : " + project.getProjectID() +", with error : "+e.getMessage())));
			message.setSuccess("ERROR");
		}
		return message;
	}

	@Override
	@Transactional
	public GenericMessage createWorkspaceProjectAssociation(FabricWorkspaceVO workspace, String projectId) {
		
		try {
			ADAProjectsNsql adaProject = customRepo.findbyUniqueLiteral("projectID", projectId);
			if (adaProject == null || adaProject.getData() == null || adaProject.getData().getProjectID() == null) {
				log.error("ADA Project with projectID {} not found in adaprojects table", projectId);
				GenericMessage message = new GenericMessage("ERROR");
				message.setErrors(List.of(new MessageDescription("ADA Project with projectID " + projectId + " not found in adaprojects table")));
				return message;
			}
			log.info("Validated projectID {} exists in adaprojects table", projectId);
			if(adaProject.getData().getCapacity() == null || adaProject.getData().getCapacity().getId() == null){
				log.error("No capacity found associated with the workspace");
				throw new RuntimeException("No capacity found associated with the workspace");
			} else if(workspace.getCapacity() != null && workspace.getCapacity().getRegion() != null && 
					!workspace.getCapacity().getRegion().equalsIgnoreCase(adaProject.getData().getRegion())){
				log.error("Workspace capacity region and the Project region sould be same, workspace region migration is not allowed Please contact ADA Team!");
				throw new IllegalArgumentException("Workspace capacity region : "+ workspace.getCapacity().getRegion() + " and the Project region : " + 
				adaProject.getData().getRegion() + " sould be same, workspace region migration is not allowed Please contact ADA Team!");
			}

			Optional<FabricWorkspaceNsql> existingEntityOpt = fabricWorkspaceRepo.findById(workspace.getId());
			if (existingEntityOpt.isPresent()) {
				FabricWorkspaceNsql existingEntity = existingEntityOpt.get();

				ErrorResponseDto assignCapacityResponse = fabricWorkspaceClient.assignCapacity(existingEntity.getId(), adaProject.getData().getCapacity().getId());
				if(assignCapacityResponse!=null && assignCapacityResponse.getErrorCode()!=null && 
				("500".equalsIgnoreCase(assignCapacityResponse.getErrorCode()) || "Failed".equalsIgnoreCase(assignCapacityResponse.getErrorCode()))) {
					workspace.setCapacity(null);
					log.error("Failed to update capacity for the workspace, please reassign or update workspace to assign capacity automatically.");
					throw new IllegalArgumentException("Failed to assign the capacity for the workspace while updating the ADA project, Please contact ADA team!");
				}
				existingEntity.getData().setProjectId(projectId);
				fabricWorkspaceRepo.save(existingEntity);
				log.info("Successfully associated workspace {} with project {}", workspace.getId(), projectId);
			} else {
				log.error("Workspace entity not found in DB for id {}", workspace.getId());
				GenericMessage message = new GenericMessage("ERROR");
				message.setErrors(List.of(new MessageDescription("Workspace entity not found for id: " + workspace.getId())));
				return message;
			}
			return new GenericMessage("SUCCESS");
		} catch (Exception e) {
			log.error("Error creating workspace-project association", e);
			GenericMessage message = new GenericMessage("ERROR");
			message.setErrors(List.of(new MessageDescription("Failed to create workspace-project association with id : " + projectId +", with error : "+e.getMessage())));
			return message;
		}
	}
	
	private void updateCapacityForFabricWorkspaces(ADAProjectDetailsVO adaProject){
		try {

			if(adaProject.getCapacity() == null && adaProject.getRegion() == null){
				log.info("Capacity details and region are null for project id {}.", adaProject.getProjectID());
				throw new RuntimeException("No capacity or region given with the project details, project creation denied please provide project region or capacity");
			} else {
				CapacityVO capacityVO = null;
				if(adaProject.getCapacity() != null && adaProject.getCapacity().getId() != null){
					capacityVO = fabricWorkspaceClient.getCapacityDetails(adaProject.getCapacity().getId());
				} else if (adaProject.getRegion() != null || !adaProject.getRegion().isEmpty()){
					CapacityNsql capacityNsql = capacityRepo.findById(adaProject.getRegion().toLowerCase()).orElse(null);
					if(capacityNsql == null || capacityNsql.getData() == null) {
						log.warn("No capacity details found in DB for region {}. and project id {}.", adaProject.getRegion(), adaProject.getProjectID());
						throw new IllegalArgumentException("Provided region does not have any default capacity, Please contact ADA team!");
					}
					capacityVO = new CapacityVO();
					capacityVO.setId(capacityNsql.getData().getId());
					capacityVO.setName(capacityNsql.getData().getName());
					capacityVO.setRegion(capacityNsql.getData().getRegion());
					capacityVO.setSku(capacityNsql.getData().getSku());
					capacityVO.setState(capacityNsql.getData().getState());
					capacityVO.setCreatedOn(capacityNsql.getData().getCreatedOn());
					capacityVO.setModifiedOn(capacityNsql.getData().getModifiedOn());
				} else {
					log.error("Provided region or capacity value is invalid, please provide a correct region/capacity value Region : "+ adaProject.getRegion());
					throw new IllegalArgumentException("Provided region value is invalid, please provide a correct region value : "+ adaProject.getRegion());
				}

				if(capacityVO != null && adaProject.getRegion() != null && !capacityVO.getRegion().equalsIgnoreCase(adaProject.getRegion())){
					log.error("Priovided capacity ID region : {} and Ada Project resion : {} does not match, project creation/modification not allowed",
					capacityVO.getRegion(), adaProject.getRegion());
					throw new RuntimeException("Priovided capacity ID region : " + capacityVO.getRegion() + " and Ada Project resion : " + adaProject.getRegion() + " does not match, project creation/modification not allowed");
				}
				adaProject.setCapacity(capacityVO);
				adaProject.setRegion(capacityVO.getRegion());
			}

			List<FabricWorkspaceNsql> workspaces = fabricWorkspaceCustomRepository.getAllByProjectId(adaProject.getProjectID());
			if(workspaces != null && !workspaces.isEmpty()) {
				for(FabricWorkspaceNsql workspace : workspaces) {
					Capacity capacity = new Capacity();
					capacity.setId(adaProject.getCapacity().getId());
					capacity.setName(adaProject.getCapacity().getName());
					capacity.setRegion(adaProject.getCapacity().getRegion());
					capacity.setSku(adaProject.getCapacity().getSku());
					capacity.setState(adaProject.getCapacity().getState());
					
					ErrorResponseDto assignCapacityResponse = fabricWorkspaceClient.assignCapacity(workspace.getId(), capacity.getId());
					if(assignCapacityResponse!=null && assignCapacityResponse.getErrorCode()!=null && 
					("500".equalsIgnoreCase(assignCapacityResponse.getErrorCode()) || "Failed".equalsIgnoreCase(assignCapacityResponse.getErrorCode()))) {
						workspace.getData().setCapacity(null);
						log.error("Failed to update capacity for the workspace, please reassign or update workspace to assign capacity automatically.");
						throw new IllegalArgumentException("Failed to assign the capacity for the workspace while updating the ADA project, Please contact ADA team!");
					}
					workspace.getData().setCapacity(capacity);
					fabricWorkspaceRepo.save(workspace);
				}
			}
		} catch (Exception e) {
			log.error("Error updating capacity for associated Fabric workspaces for project id {}: {}", adaProject.getProjectID(), e.getMessage(), e);
			throw new RuntimeException("ERROR while creating/updating the project with id " + adaProject.getProjectID() + " with error: " + e.getMessage(), e);
		}
	}
	
}
