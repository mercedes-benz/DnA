package com.mb.dna.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mb.dna.data.dataiku.api.dto.CollaboratorDetailsDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectDto;
import com.mb.dna.data.dataiku.db.entities.CollaboratorSql;
import com.mb.dna.data.dataiku.db.entities.DataikuSql;

import jakarta.inject.Singleton;

@Singleton
public class DataikuAssembler {

	public DataikuSql toEntity(DataikuProjectDto vo) {
		DataikuSql entity = new DataikuSql();
		if(vo!=null) {
			entity.setId(vo.getId());
			entity.setCloudProfile(vo.getCloudProfile());
			entity.setDescription(vo.getDescription());
			entity.setProjectName(vo.getProjectName());
			List<CollaboratorDetailsDto> collabsDto = vo.getCollaborators();
			List<CollaboratorSql> collabs = new ArrayList<>();
			if(collabsDto!=null) {
				collabs = collabsDto.stream().map(n -> this.toCollaboratorsData(n,entity.getId())).collect(Collectors.toList());
			}
			entity.setCollaborators(collabs);
			entity.setCreatedBy(vo.getCreatedBy());
			entity.setCreatedOn(vo.getCreatedOn());
			
			entity.setStatus(vo.getStatus());
			entity.setClassificationType(vo.getClassificationType());
			entity.setHasPii(vo.getHasPii());
			entity.setDivisionId(vo.getDivisionId());
			entity.setDivisionName(vo.getDivisionName());
			entity.setSubdivisionId(vo.getSubdivisionId());
			entity.setSubdivisionName(vo.getSubdivisionName());
			entity.setDepartment(vo.getDepartment());
			}
		return entity;
	}
	
	
	public DataikuProjectDto toVo(DataikuSql entity) {
		DataikuProjectDto vo = new DataikuProjectDto();
		if(entity!=null) {
			vo.setId(entity.getId());
			vo.setCloudProfile(entity.getCloudProfile());
			vo.setDescription(entity.getDescription());
			vo.setProjectName(entity.getProjectName());
			List<CollaboratorSql> collabs = entity.getCollaborators();
			List<CollaboratorDetailsDto> collabsDto = new ArrayList<>();
			if(collabs!=null) {
				collabsDto = collabs.stream().map(n -> this.toCollaboratorsVO(n)).collect(Collectors.toList());
			}
			vo.setCollaborators(collabsDto);
			vo.setCreatedOn(entity.getCreatedOn());
			vo.setCreatedBy(entity.getCreatedBy());
			
			vo.setStatus(entity.getStatus());
			vo.setClassificationType(entity.getClassificationType());
			vo.setHasPii(entity.getHasPii());
			vo.setDivisionId(entity.getDivisionId());
			vo.setDivisionName(entity.getDivisionName());
			vo.setSubdivisionId(entity.getSubdivisionId());
			vo.setSubdivisionName(entity.getSubdivisionName());
			vo.setDepartment(entity.getDepartment());
		}
		return vo;
	}
	
	public CollaboratorDetailsDto toCollaboratorsVO(CollaboratorSql collaborator) {
		CollaboratorDetailsDto collabDto = new CollaboratorDetailsDto();
		if(collaborator!=null) {
			collaborator.getDataikuProject();
			collabDto.setGivenName(collaborator.getGivenName());
			collabDto.setPermission(collaborator.getPermission());
			collabDto.setSurName(collaborator.getSurName());
			collabDto.setUserId(collaborator.getUserId());
		}
		return collabDto;
	}
	
	public CollaboratorSql toCollaboratorsData(CollaboratorDetailsDto collaborator, String id) {
		CollaboratorSql collabData = new CollaboratorSql();
		if(collaborator!=null) {
			String collabId = UUID.randomUUID().toString();
			collabData.setId(collabId);
			collabData.setGivenName(collaborator.getGivenName());
			collabData.setPermission(collaborator.getPermission());
			collabData.setSurName(collaborator.getSurName());
			collabData.setUserId(collaborator.getUserId());
			collabData.setDataikuId(id);
		}
		return collabData;
	}
}
