package com.daimler.data.assembler;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;

import com.daimler.data.db.entities.DbServiceNsql;
import com.daimler.data.db.json.CodeServerLeanGovernanceFeilds;
import com.daimler.data.db.json.DbService;
import com.daimler.data.db.json.UserInfo;
import com.daimler.data.dto.dbService.CodeServerGovernanceVO;
import com.daimler.data.dto.dbService.DbServiceVO;
import com.daimler.data.dto.dbService.UserInfoVO;

public class DbServiceAssembler implements GenericAssembler<DbServiceVO, DbServiceNsql> {

    @Override
    public DbServiceVO toVo(DbServiceNsql entity) {
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		DbServiceVO vo = new DbServiceVO();
		if(entity != null){
			vo.setId(entity.getId());
			CodeServerWorkspace data = entity.getData();
			if (data != null) {
				BeanUtils.copyProperties(data, vo);
				CodeServerLeanGovernanceFeilds governance = data.getDataGovernance();
				if (governance != null) {
					CodeServerGovernanceVO governanceVO = this.toGovernanceVo(governance);
					vo.setDataGovernance(governanceVO);
				}
				if (data.getCreatedOn() != null)
					vo.setCreatedOn(isoFormat.parse(isoFormat.format(data.getCreatedOn())));
				if (data.getModifiedOn() != null)
					vo.setModifiedOn(isoFormat.parse(isoFormat.format(data.getModifiedOn())));	
				List<UserInfo> collabs = data.getProjectCollaborators();
						 if(collabs!=null && !collabs.isEmpty()) {
							 List<UserInfoVO> collabsVO = collabs.stream().map
									 (n -> this.toUserInfoVO(n))
									 .collect(Collectors.toList());
							 vo.setProjectCollaborators(collabsVO);
						 }
						 UserInfoVO projectOwnerVO = this.toUserInfoVO(data.getProjectOwner());
						 vo.setProjectOwner(projectOwnerVO);
						 UserInfoVO modifiedBy = this.toUserInfoVO(data.getModifiedBy());
						 vo.setModifiedBy(modifiedBy);						 	
			}
		}
       
    }

    @Override
    public DbServiceNsql toEntity(DbServiceVO vo) {
        DbServiceNsql entity = null;
		 if (vo != null) {
			 entity = new DbServiceNsql();
			 DbService data = new DbService();
			 entity.setId(vo.getId());
			 BeanUtils.copyProperties(vo, data);
			 CodeServerGovernanceVO governanceVO = vo.getDataGovernance();
				 if (governanceVO != null) {
					 CodeServerLeanGovernanceFeilds governance = this.toGovernanceEntity(governanceVO);
					 data.setDataGovernance(governance);
				 }
				 UserInfoVO projectOwnerVO = vo.getProjectOwner();
				 if (projectOwnerVO != null) {
					 UserInfo projectOwner = this.toUserInfo(projectOwnerVO);
					 data.setProjectOwner(projectOwner);
					 data.setModifiedBy(projectOwner);
				 }
				 List<UserInfoVO> projectCollabsVO = vo.getProjectCollaborators();
				 if (projectCollabsVO != null && !projectCollabsVO.isEmpty()) {
					 List<UserInfo> projectCollabs = projectCollabsVO.stream().map(n -> toUserInfo(n))
							 .collect(Collectors.toList());
					 data.setProjectCollaborators(projectCollabs);
				 }	 
		 }
    }

     private UserInfoVO toUserInfoVO(UserInfo userInfo) {
		 UserInfoVO vo = new UserInfoVO();
		 if (userInfo != null) {
			 BeanUtils.copyProperties(userInfo, vo);
			 if(userInfo.getIsAdmin()!=null){
				vo.setIsAdmin(userInfo.getIsAdmin());
			 }
			 else{
				vo.setIsAdmin(false);
			 }
		 }
		 return vo;
	 }
 
	 public UserInfo toUserInfo(UserInfoVO userInfo) {
		 UserInfo entity = new UserInfo();
		 if (userInfo != null) {
			 BeanUtils.copyProperties(userInfo, entity);
			 if(userInfo.isIsAdmin()!=null){
				entity.setIsAdmin(userInfo.isIsAdmin());
			 }
			 else{
				entity.setIsAdmin(false);
			 }
		 }
		
		 return entity;
	 }

	 public CodeServerGovernanceVO toGovernanceVo(CodeServerLeanGovernanceFeilds governance) {
		CodeServerGovernanceVO governanceVo = new CodeServerGovernanceVO();
		if (governance != null) {
			BeanUtils.copyProperties(governance, governanceVo);
			if (governance.getPiiData() != null) {
				governanceVo.setPiiData(governance.getPiiData());
			}
			else
			{
				governanceVo.setPiiData(false);
			}
		}
		return governanceVo;
	}

	public CodeServerLeanGovernanceFeilds toGovernanceEntity(CodeServerGovernanceVO governanceVO) {
		CodeServerLeanGovernanceFeilds governanceFeilds = new CodeServerLeanGovernanceFeilds();
		if (governanceVO != null) {
			BeanUtils.copyProperties(governanceVO, governanceFeilds);
			if (governanceVO.isPiiData()) {
				governanceFeilds.setPiiData(governanceVO.isPiiData());
			}
			else
			{
				governanceFeilds.setPiiData(false);
			}
		}
		return governanceFeilds;
	}

}
