package com.daimler.data.service.adaProjects;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.assembler.ADAProjectsAssembler;
import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.db.entities.ADAProjectsNsql;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.repo.adaProjects.ADAProjectsCustomRepository;
import com.daimler.data.db.repo.adaProjects.ADAProjectsRepository;
import com.daimler.data.db.repo.forecast.FabricWorkspaceRepository;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsCollectionVO;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.common.BaseCommonService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
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

	public BaseADAProjectsService() {
		super();
	}

	@Override
	public ADAProjectDetailsCollectionVO getAllProjects(int limit, int offset) {
		ADAProjectDetailsCollectionVO collection = new ADAProjectDetailsCollectionVO();
		GenericMessage message = new GenericMessage();
		List<MessageDescription> errors = null;
		List<MessageDescription> warnings = null;
		try {

			List<ADAProjectsNsql> adaProjects = customRepo.findAll(limit, offset);
			List<ADAProjectDetailsVO> projects = adaProjects.stream()
					.map(project -> assembler.toVo(project))
					.collect(Collectors.toList());
			collection.setRecords(projects);
		} catch (Exception e) {
			log.error("Error fetching ADA Projects", e);
			errors = List.of(new MessageDescription("Failed to fetch projects with error : " + e.getMessage()));
			message.setErrors(errors);
			message.setSuccess("ERROR");
			collection.responses(message);
		}
		message.setSuccess("SUCCESS");
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
			workspace.setProjectId(projectId);
			FabricWorkspaceNsql entity = fabricWorkspaceAssembler.toEntity(workspace);
			fabricWorkspaceRepo.save(entity);
			return new GenericMessage("SUCCESS");
		} catch (Exception e) {
			log.error("Error creating workspace-project association", e);
			GenericMessage message = new GenericMessage("ERROR");
			message.setErrors(List.of(new MessageDescription("Failed to create workspace-project association with id : " + projectId +", with error : "+e.getMessage())));
			return message;
		}
	}

}