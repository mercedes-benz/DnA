package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.json.Capacity;
import com.daimler.data.db.json.FabricWorkspace;
import com.daimler.data.db.json.ProjectDetails;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.dto.fabricWorkspace.CapacityVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.ProjectReferenceDetailsVO;

@Component
public class FabricWorkspaceAssembler implements GenericAssembler<FabricWorkspaceVO, FabricWorkspaceNsql> {

	@Override
	public FabricWorkspaceVO toVo(FabricWorkspaceNsql entity) {
		FabricWorkspaceVO vo = null;
		if(entity!=null) {
			vo = new FabricWorkspaceVO();
			vo.setId(entity.getId());
			FabricWorkspace data = entity.getData();
			if(data!=null) {
				BeanUtils.copyProperties(data, vo);
				Capacity capacity = data.getCapacity();
				CapacityVO capacityVO = new CapacityVO();
				if(capacity != null) {
					BeanUtils.copyProperties(capacity, capacityVO);
				}
				vo.setCapacity(capacityVO);
				
				UserDetails creator = data.getCreatedBy();
				CreatedByVO createdByVO = new CreatedByVO();
				if(creator!=null) {
					BeanUtils.copyProperties(creator, createdByVO);
				}
				
				List<ProjectReferenceDetailsVO> relatedReportsVO = toProjectDetailVOs(data.getRelatedReports());
				vo.setRelatedReports(relatedReportsVO);
				
				List<ProjectReferenceDetailsVO> relatedSolutionsVO = toProjectDetailVOs(data.getRelatedSolutions());
				vo.setRelatedSolutions(relatedSolutionsVO);
				
				vo.setCreatedBy(createdByVO);
				vo.setHasPii(data.getHasPii());
			}
		}
		return vo;
	}
	
	private List<ProjectReferenceDetailsVO> toProjectDetailVOs(List<ProjectDetails> projectsDetails){
		List<ProjectReferenceDetailsVO> relatedProjectVOs = new ArrayList<>();
		if(projectsDetails!=null &&!projectsDetails.isEmpty()) {
			for(ProjectDetails projectDetail : projectsDetails) {
				ProjectReferenceDetailsVO relatedProjectVO = new ProjectReferenceDetailsVO();
				BeanUtils.copyProperties(projectDetail, relatedProjectVO);
				relatedProjectVOs.add(relatedProjectVO);
			}
		}
		return relatedProjectVOs;
	}
	
	private List<ProjectDetails> toProjectDetails(List<ProjectReferenceDetailsVO> projectDetailVOs){
		List<ProjectDetails> relatedProjects = new ArrayList<>();
		if(projectDetailVOs!=null &&!projectDetailVOs.isEmpty()) {
			for(ProjectReferenceDetailsVO vo : projectDetailVOs) {
				ProjectDetails relatedProject = new ProjectDetails();
				BeanUtils.copyProperties(vo, relatedProject);
				relatedProjects.add(relatedProject);
			}
		}
		return relatedProjects;
	}
	
	@Override
	public FabricWorkspaceNsql toEntity(FabricWorkspaceVO vo) {
		FabricWorkspaceNsql entity = null;
		if(vo!=null) {
			entity = new FabricWorkspaceNsql();
			entity.setId(vo.getId());
			FabricWorkspace data = new FabricWorkspace();
			BeanUtils.copyProperties(vo, data);
			CapacityVO capacityVO = vo.getCapacity();
			Capacity capacity = new Capacity();
			if(capacityVO!=null) {
				BeanUtils.copyProperties(capacityVO, capacity);
			}
			data.setCapacity(capacity);
			UserDetails createdBy = new UserDetails();
			CreatedByVO createdByVO = vo.getCreatedBy();
			if(createdByVO!=null) {
				BeanUtils.copyProperties(createdByVO, createdBy);
			}
			data.setCreatedBy(createdBy);
			data.setHasPii(vo.isHasPii());

			List<ProjectDetails> relatedReports = toProjectDetails(vo.getRelatedReports());
			data.setRelatedReports(relatedReports);
			
			List<ProjectDetails> relatedSolutions = toProjectDetails(vo.getRelatedSolutions());
			data.setRelatedSolutions(relatedSolutions);
			
			entity.setData(data);
		}
		return entity;
	}

}
