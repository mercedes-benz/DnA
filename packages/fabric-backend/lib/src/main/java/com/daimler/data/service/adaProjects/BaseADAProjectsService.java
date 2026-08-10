package com.daimler.data.service.adaProjects;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
			} else if (existingEntity.getData() != null && existingEntity.getData().getRegion() != null && 
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

			Optional<FabricWorkspaceNsql> existingEntityOpt = fabricWorkspaceRepo.findById(workspace.getId());
			if (existingEntityOpt.isPresent()) {
				FabricWorkspaceNsql existingEntity = existingEntityOpt.get();
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
			List<FabricWorkspaceNsql> workspaces = fabricWorkspaceCustomRepository.getAllByProjectId(adaProject.getProjectID());
			if(adaProject.getCapacity() == null && adaProject.getRegion() == null){
				log.info("Capacity details and region are null for project id {}. Using default capacity values.", adaProject.getProjectID());
				adaProject.setCapacity(buildDefaultCapacityVO());
				adaProject.setRegion(capacityRegion);
			} else if (adaProject.getRegion() != null && adaProject.getCapacity() == null){
				CapacityNsql capacityNsql = capacityRepo.findById(adaProject.getRegion().toLowerCase()).orElse(null);
				if(capacityNsql == null) {
					log.warn("No capacity details found in DB for region {}. Using default capacity values for project id {}.", adaProject.getRegion(), adaProject.getProjectID());
					adaProject.setCapacity(buildDefaultCapacityVO());
					adaProject.setRegion(capacityRegion);
				} else {
					CapacityVO capacityVO = new CapacityVO();
					capacityVO.setId(capacityNsql.getData().getId());
					capacityVO.setName(capacityNsql.getData().getName());
					capacityVO.setRegion(capacityNsql.getData().getRegion());
					capacityVO.setSku(capacityNsql.getData().getSku());
					capacityVO.setState(capacityNsql.getData().getState());
					capacityVO.setCreatedOn(capacityNsql.getData().getCreatedOn());
					capacityVO.setModifiedOn(capacityNsql.getData().getModifiedOn());
					adaProject.setCapacity(capacityVO);
					adaProject.setRegion(capacityNsql.getData().getRegion());
					log.info("Region {} provided for project id {}. Using capacity details from DB for the region.", adaProject.getRegion(), adaProject.getProjectID());
				}
			}
			if(workspaces != null && !workspaces.isEmpty()) {
				for(FabricWorkspaceNsql workspace : workspaces) {
					Capacity capacity = new Capacity();
					capacity.setId(adaProject.getCapacity().getId());
					capacity.setName(adaProject.getCapacity().getName());
					capacity.setRegion(adaProject.getCapacity().getRegion());
					capacity.setSku(adaProject.getCapacity().getSku());
					capacity.setState(adaProject.getCapacity().getState());
					workspace.getData().setCapacity(capacity);
					fabricWorkspaceRepo.save(workspace);
				}
			}
		} catch (Exception e) {
			log.error("Error updating capacity for associated Fabric workspaces for project id {}: {}", adaProject.getProjectID(), e.getMessage(), e);
			throw new RuntimeException("Failed to update capacity for associated Fabric workspaces for project id " + adaProject.getProjectID() + " with error: " + e.getMessage(), e);
		}
	}

	private CapacityVO buildDefaultCapacityVO() {
		CapacityVO capacityVO = new CapacityVO();
		capacityVO.setId(fabricCapacityId);
		capacityVO.setName(fabricCapacityName);
		capacityVO.setRegion(capacityRegion);
		capacityVO.setSku(capacitySku);
		capacityVO.setState(capacityState);
		capacityVO.setCreatedOn(new Date());
		capacityVO.setModifiedOn(new Date());
		return capacityVO;
	}
	
}
