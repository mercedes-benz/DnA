package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.json.Capacity;
import com.daimler.data.db.json.EntitlementDetails;
import com.daimler.data.db.json.FabricWorkspace;
import com.daimler.data.db.json.FabricWorkspaceStatus;
import com.daimler.data.db.json.GroupDetails;
import com.daimler.data.db.json.Lakehouse;
import com.daimler.data.db.json.ProjectDetails;
import com.daimler.data.db.json.RoleDetails;
import com.daimler.data.db.json.Shortcut;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.dto.fabric.LakehouseDto;
import com.daimler.data.dto.fabric.LakehouseS3ShortcutDto;
import com.daimler.data.dto.fabricWorkspace.CapacityVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.EntitlementDetailsVO;
import com.daimler.data.dto.fabricWorkspace.FabricLakehouseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceStatusVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.GroupDetailsVO;
import com.daimler.data.dto.fabricWorkspace.ProjectReferenceDetailsVO;
import com.daimler.data.dto.fabricWorkspace.RoleDetailsVO;
import com.daimler.data.dto.fabricWorkspace.ShortcutVO;

@Component
public class FabricWorkspaceAssembler implements GenericAssembler<FabricWorkspaceVO, FabricWorkspaceNsql> {
	
	public FabricLakehouseVO toLakehouseVOFromDto(LakehouseDto lakehouseDto) {
		FabricLakehouseVO vo = new FabricLakehouseVO();
		if(lakehouseDto!=null) {
			vo.setId(lakehouseDto.getId());
			vo.setName(lakehouseDto.getDisplayName());
			vo.setDescription(lakehouseDto.getDescription());
			if(lakehouseDto.getDisplayName()!=null && lakehouseDto.getDisplayName().toLowerCase().contains("dev")) {
				vo.setSensitivityLabel("Internal");
			}else {
				vo.setSensitivityLabel("Confidential");
			}
		}
		return vo;
	}
	
	public ShortcutVO toLakehouseShortcutVOFromDto(LakehouseS3ShortcutDto dto) {
		ShortcutVO vo = new ShortcutVO();
		vo.setName(dto.getName());
		vo.setBucketpath(null);
		vo.setPath(dto.getPath());
		if (dto.getTarget() != null && dto.getTarget().getS3Compatible() != null)
			vo.setBucketname(dto.getTarget().getS3Compatible().getBucket());
		else
			vo.setBucketname(null);
		return vo;
	}
	
	
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
				
				List<FabricLakehouseVO> lakehouseVOs = new ArrayList<>();
				List<Lakehouse> lakehouses = data.getLakehouses();
				if(lakehouses!=null && !lakehouses.isEmpty()) {
					lakehouseVOs = lakehouses.stream().map(n -> toLakehouseVO(n)).collect(Collectors.toList());
				}
				vo.setLakehouses(lakehouseVOs);
				
				FabricWorkspaceStatus workspaceStatus = data.getStatus();
				FabricWorkspaceStatusVO workspaceStatusVO = new FabricWorkspaceStatusVO();
				if(workspaceStatus!=null) {
					workspaceStatusVO.setState(workspaceStatus.getState());
					List<EntitlementDetails> entitlements = workspaceStatus.getEntitlements();
					List<EntitlementDetailsVO> entitlementsVO = new ArrayList<>();
					if(entitlements!=null && !entitlements.isEmpty()) {
						entitlementsVO = entitlements.stream().map(n -> toEntitlementDetailsVO(n)).collect(Collectors.toList());
					}
					workspaceStatusVO.setEntitlements(entitlementsVO);
					
					List<RoleDetails> roles = workspaceStatus.getRoles();
					List<RoleDetailsVO> rolesVO = new ArrayList<>();
					if(roles!=null && !roles.isEmpty()) {
						rolesVO = roles.stream().map(n -> toRoleDetailsVO(n)).collect(Collectors.toList());
					}
					workspaceStatusVO.setRoles(rolesVO);
					
					List<GroupDetails> groups = workspaceStatus.getMicrosoftGroups();
					List<GroupDetailsVO> groupDetailsVO = new ArrayList<>();
					if(groups!=null && !groups.isEmpty()) {
						groupDetailsVO = groups.stream().map(n -> toGroupDetailsVO(n)).collect(Collectors.toList());
					}
					workspaceStatusVO.setMicrosoftGroups(groupDetailsVO);
					
				}else {
					workspaceStatusVO.setState(null);
				}
				vo.setStatus(workspaceStatusVO);
				vo.setCreatedBy(createdByVO);
				vo.setHasPii(data.getHasPii());
			}
		}
		return vo;
	}
	
	private EntitlementDetailsVO toEntitlementDetailsVO(EntitlementDetails entitlement) {
		EntitlementDetailsVO vo = new EntitlementDetailsVO();
		if(entitlement!=null) {
			BeanUtils.copyProperties(entitlement, vo);
		}
		return vo;
	}
	
	private RoleDetails fromRoleDetailsVO(RoleDetailsVO vo) {
		RoleDetails role = new RoleDetails();
		if(vo!=null) {
			BeanUtils.copyProperties(vo, role);
			List<EntitlementDetailsVO> entitlementsVO = vo.getEntitlements();
			List<EntitlementDetails> entitlements = new ArrayList<>();
			if(entitlementsVO!=null && !entitlementsVO.isEmpty()) {
				entitlements = entitlementsVO.stream().map(n -> fromEntitlementDetailsVO(n)).collect(Collectors.toList());
			}
			role.setEntitlements(entitlements);
		}
		return role;
	}
	
	private RoleDetailsVO toRoleDetailsVO(RoleDetails role) {
		RoleDetailsVO vo = new RoleDetailsVO();
		if(role!=null) {
			BeanUtils.copyProperties(role, vo);
			List<EntitlementDetails> entitlements = role.getEntitlements();
			List<EntitlementDetailsVO> entitlementsVO = new ArrayList<>();
			if(entitlements!=null && !entitlements.isEmpty()) {
				entitlementsVO = entitlements.stream().map(n -> toEntitlementDetailsVO(n)).collect(Collectors.toList());
			}
			vo.setEntitlements(entitlementsVO);
		}
		return vo;
	}
	
	private EntitlementDetails fromEntitlementDetailsVO(EntitlementDetailsVO entitlementVO) {
		EntitlementDetails entitlement = new EntitlementDetails();
		if(entitlementVO!=null) {
			BeanUtils.copyProperties(entitlementVO, entitlement);
		}
		return entitlement;
	}
	
	
	
	private GroupDetailsVO toGroupDetailsVO(GroupDetails groupDetails) {
		GroupDetailsVO vo = new GroupDetailsVO();
		if(groupDetails!=null) {
			BeanUtils.copyProperties(groupDetails, vo);
		}
		return vo;
	}
	
	private GroupDetails fromGroupDetailsVO(GroupDetailsVO vo) {
		GroupDetails groupDetails = new GroupDetails();
		if(vo!=null) {
			BeanUtils.copyProperties(vo, groupDetails);
		}
		return groupDetails;
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
	
	private Lakehouse toLakehouse(FabricLakehouseVO vo) {
		Lakehouse lakehouse = new Lakehouse();
		if(vo!=null) {
			lakehouse.setId(vo.getId());
			lakehouse.setName(vo.getName());
			lakehouse.setSensitivityLabel(vo.getSensitivityLabel());
			lakehouse.setDescription(vo.getDescription());
			List<ShortcutVO> shortcutVOs = vo.getShortcuts();
			List<Shortcut> shortcuts = new ArrayList<>();
			if(shortcutVOs!=null && !shortcutVOs.isEmpty()) {
				shortcuts = shortcutVOs.stream().map(n -> toShortcut(n)).collect(Collectors.toList());
			}
			lakehouse.setShortcuts(shortcuts);
		}
		return lakehouse;
	}
	
	private Shortcut toShortcut(ShortcutVO vo) {
		Shortcut shortcut = new Shortcut();
		if(vo!=null) {
			BeanUtils.copyProperties(vo, shortcut);
		}
		return shortcut;
	}
	
	private FabricLakehouseVO toLakehouseVO(Lakehouse lakehouse) {
		FabricLakehouseVO  vo = new FabricLakehouseVO();
		if(lakehouse!=null) {
			vo.setId(lakehouse.getId());
			vo.setName(lakehouse.getName());
			vo.setSensitivityLabel(lakehouse.getSensitivityLabel());
			vo.setDescription(lakehouse.getDescription());
			List<Shortcut> shortcuts = lakehouse.getShortcuts();
			List<ShortcutVO> shortcutVOs = new ArrayList<>();
			if(shortcuts!=null && !shortcuts.isEmpty()) {
				shortcutVOs = shortcuts.stream().map(n -> toShortcutVO(n)).collect(Collectors.toList());
			}
			vo.setShortcuts(shortcutVOs);
		}
		return vo;
	}
	
	private ShortcutVO toShortcutVO(Shortcut shortcut) {
		ShortcutVO vo = new ShortcutVO();
		if(shortcut!=null) {
			BeanUtils.copyProperties(shortcut,vo);
		}
		return vo;
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
			
			FabricWorkspaceStatus workspaceStatus = new FabricWorkspaceStatus();
			FabricWorkspaceStatusVO workspaceStatusVO = vo.getStatus(); 
			if(workspaceStatusVO!=null) {
				workspaceStatus.setState(workspaceStatusVO.getState());
				List<EntitlementDetailsVO> entitlementsVO = workspaceStatusVO.getEntitlements();
				List<EntitlementDetails> entitlements = new ArrayList<>();
				if(entitlementsVO!=null && !entitlementsVO.isEmpty()) {
					entitlements = entitlementsVO.stream().map(n -> fromEntitlementDetailsVO(n)).collect(Collectors.toList());
				}
				workspaceStatus.setEntitlements(entitlements);
				
				List<RoleDetails> roles = new ArrayList<>();
				List<RoleDetailsVO> rolesVO = workspaceStatusVO.getRoles();
				if(rolesVO!=null && !rolesVO.isEmpty()) {
					roles = rolesVO.stream().map(n -> fromRoleDetailsVO(n)).collect(Collectors.toList());
				}
				workspaceStatus.setRoles(roles);
				
				List<GroupDetails> groups = new ArrayList<>();
				List<GroupDetailsVO> groupDetailsVO = workspaceStatusVO.getMicrosoftGroups();
				if(groupDetailsVO!=null && !groupDetailsVO.isEmpty()) {
					groups = groupDetailsVO.stream().map(n -> fromGroupDetailsVO(n)).collect(Collectors.toList());
				}
				workspaceStatus.setMicrosoftGroups(groups);
				
			}else {
				workspaceStatus.setState(null);
			}
			data.setStatus(workspaceStatus);
			
			List<FabricLakehouseVO> lakehouseVOs = vo.getLakehouses();
			List<Lakehouse> lakehouses = new ArrayList<>();
			if(lakehouseVOs!=null && !lakehouseVOs.isEmpty()) {
				lakehouses = lakehouseVOs.stream().map(n -> toLakehouse(n)).collect(Collectors.toList());
			}
			data.setLakehouses(lakehouses);
			
			entity.setData(data);
		}
		return entity;
	}

}
