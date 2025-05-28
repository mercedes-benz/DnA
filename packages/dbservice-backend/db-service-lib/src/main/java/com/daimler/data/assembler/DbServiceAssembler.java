package com.daimler.data.assembler;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.daimler.data.application.config.VaultConfig;
import com.daimler.data.db.entities.DbServiceNsql;
import com.daimler.data.db.json.CodeServerLeanGovernanceFeilds;
import com.daimler.data.db.json.DbService;
import com.daimler.data.db.json.UserInfo;
import com.daimler.data.dto.dbService.GovernanceVO;
import com.daimler.data.dto.dbService.CredentialsVO;
import com.daimler.data.dto.dbService.DbServiceVO;
import com.daimler.data.dto.dbService.UserInfoVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DbServiceAssembler implements GenericAssembler<DbServiceVO, DbServiceNsql> {

	@Value("${dbService.port}")
    private String port;

    @Value("${dbService.host.url}")
    private String hostBaseUrl;

	@Autowired
    private VaultConfig vault;

    @Override
    public DbServiceVO toVo(DbServiceNsql entity) {
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		DbServiceVO vo = new DbServiceVO();
		try {
			if(entity != null){
				vo.setId(entity.getId());
				DbService data = entity.getData();
				if (data != null) {
					BeanUtils.copyProperties(data, vo);
					CodeServerLeanGovernanceFeilds governance = data.getDataGovernance();
					if (governance != null) {
						GovernanceVO governanceVO = this.toGovernanceVo(governance);
						vo.setDataGovernance(governanceVO);
					}
					if (data.getCreatedOn() != null)
						vo.setCreatedOn(isoFormat.parse(isoFormat.format(data.getCreatedOn())));
					if (data.getModifiedOn() != null)
						vo.setModifiedOn(isoFormat.parse(isoFormat.format(data.getModifiedOn())));	
					List<UserInfo> collabs = data.getProjectCollaborators();
							 if(collabs!=null && !collabs.isEmpty()) {
								 List<UserInfoVO> collabsVO = collabs.stream().map(
									n -> toUserInfoVO(n)).collect(Collectors.toList());
								 vo.setProjectCollaborators(collabsVO);
							 }
							 UserInfoVO projectOwnerVO = this.toUserInfoVO(data.getProjectOwner());
							 vo.setProjectOwner(projectOwnerVO);
							 UserInfoVO modifiedBy = this.toUserInfoVO(data.getModifiedBy());
							 vo.setModifiedBy(modifiedBy);

							 vo.setPort(port);
							 vo.setUrl(data.getServiceName().toLowerCase()+hostBaseUrl);							 
							 
				}
			}
		} catch (Exception e) {
			log.error("Failed in assembler while parsing date into iso format with exception {}", e.getMessage());
		}

		return vo;
       
    }



    @Override
    public DbServiceNsql toEntity(DbServiceVO vo) {
        DbServiceNsql entity = null;
		 if (vo != null) {
			 entity = new DbServiceNsql();
			 DbService data = new DbService();
			 entity.setId(vo.getId());
			 BeanUtils.copyProperties(vo, data);
			 GovernanceVO governanceVO = vo.getDataGovernance();
				 if (governanceVO != null) {
					 CodeServerLeanGovernanceFeilds governance = this.toGovernanceEntity(governanceVO);
					 data.setDataGovernance(governance);
				 }
				 UserInfoVO projectOwnerVO = vo.getProjectOwner();
				 if (projectOwnerVO != null) {
					 UserInfo projectOwner = this.toUserInfo(projectOwnerVO);
					 data.setProjectOwner(projectOwner);					
				 }
				 UserInfoVO modifiedBy = vo.getModifiedBy();
				 if (modifiedBy != null) {
					 UserInfo modifiedByUser = this.toUserInfo(modifiedBy);
					 data.setModifiedBy(modifiedByUser);					
				 }
				 List<UserInfoVO> projectCollabsVO = vo.getProjectCollaborators();
				 if (projectCollabsVO != null && !projectCollabsVO.isEmpty()) {
					 List<UserInfo> projectCollabs = projectCollabsVO.stream().map(n -> toUserInfo(n))
							 .collect(Collectors.toList());
					 data.setProjectCollaborators(projectCollabs);
				 }	
				 entity.setData(data); 
		 }
		 return entity;
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
			 if(userInfo.getIsRead()!=null){
				vo.setIsRead(userInfo.getIsRead());
			 }
			 else{
				vo.setIsRead(false);
			 }
			 if(userInfo.getIsWrite()!=null){
				vo.setIsWrite(userInfo.getIsWrite());
			 }
			 else{
				vo.setIsWrite(false);
			 }
		 }
		 return vo;
	 }
 
	 public UserInfo toUserInfo(UserInfoVO userInfo) {
		 UserInfo entity = new UserInfo();
		 if (userInfo != null) {
			 BeanUtils.copyProperties(userInfo, entity);
			 if(userInfo.getIsAdmin()){
				entity.setIsAdmin(userInfo.getIsAdmin());
			 }
			 else{
				entity.setIsAdmin(false);
			 }
			 if(userInfo.getIsRead()){
				entity.setIsRead(userInfo.getIsRead());
			 }
			 else{
				entity.setIsRead(false);
			 }
			 if(userInfo.getIsWrite()){
				entity.setIsWrite(userInfo.getIsWrite());
			 }
			 else{
				entity.setIsWrite(false);
			 }
		 }
		
		 return entity;
	 }

	 public GovernanceVO toGovernanceVo(CodeServerLeanGovernanceFeilds governance) {
		GovernanceVO governanceVo = new GovernanceVO();
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

	public CodeServerLeanGovernanceFeilds toGovernanceEntity(GovernanceVO governanceVO) {
		CodeServerLeanGovernanceFeilds governanceFeilds = new CodeServerLeanGovernanceFeilds();
		if (governanceVO != null) {
			BeanUtils.copyProperties(governanceVO, governanceFeilds);
			if (governanceVO.getPiiData() != null && governanceVO.getPiiData()) {
				governanceFeilds.setPiiData(governanceVO.getPiiData());
			}
			else
			{
				governanceFeilds.setPiiData(false);
			}
		}
		return governanceFeilds;
	}

}
