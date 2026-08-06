package com.daimler.data.assembler;

import java.util.Date;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.AzureKeyVaultNsql;
import com.daimler.data.db.json.AzureKeyVault;
import com.daimler.data.db.json.AzureKeyVaultCollaborator;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.KeyVaultVO;
import com.daimler.data.dto.fabricWorkspace.KeyVaultCollaboratorVO;

@Component
public class AzureKeyVaultAssembler implements GenericAssembler<KeyVaultVO, AzureKeyVaultNsql> {

	@Override
	public KeyVaultVO toVo(AzureKeyVaultNsql entity) {
		KeyVaultVO vo = null;
		if (entity != null) {
			vo = new KeyVaultVO();
			vo.setId(entity.getId());
			AzureKeyVault data = entity.getData();
			if (data != null) {
				BeanUtils.copyProperties(data, vo);
				vo.setHasPii(data.getHasPii());
				UserDetails creator = data.getCreatedBy();
				CreatedByVO createdByVO = new CreatedByVO();
				if (creator != null) {
					BeanUtils.copyProperties(creator, createdByVO);
				}
				vo.setCreatedBy(createdByVO);
				if (data.getCollaborators() != null) {
					vo.setCollaborators(data.getCollaborators().stream().map(collaborator -> {
						KeyVaultCollaboratorVO collaboratorVO = new KeyVaultCollaboratorVO();
						BeanUtils.copyProperties(collaborator, collaboratorVO);
						return collaboratorVO;
					}).collect(java.util.stream.Collectors.toList()));
				}
			}
		}
		return vo;
	}

	@Override
	public AzureKeyVaultNsql toEntity(KeyVaultVO vo) {
		AzureKeyVaultNsql entity = null;
		if (vo != null) {
			entity = new AzureKeyVaultNsql();
			entity.setId(vo.getId());
			
			AzureKeyVault data = new AzureKeyVault();
			BeanUtils.copyProperties(vo, data);
			data.setHasPii(vo.isHasPii());
			CreatedByVO createdByVO = vo.getCreatedBy();
			UserDetails creator = new UserDetails();
			if (createdByVO != null) {
				BeanUtils.copyProperties(createdByVO, creator);
			}
			data.setCreatedBy(creator);
			if (vo.getCollaborators() != null) {
				data.setCollaborators(vo.getCollaborators().stream().map(collaborator -> {
					AzureKeyVaultCollaborator collaboratorData = new AzureKeyVaultCollaborator();
					BeanUtils.copyProperties(collaborator, collaboratorData);
					return collaboratorData;
				}).collect(java.util.stream.Collectors.toList()));
			}
			
			if (vo.getCreatedOn() == null) {
				data.setCreatedOn(new Date());
			}
			
			entity.setData(data);
		}
		return entity;
	}
}
