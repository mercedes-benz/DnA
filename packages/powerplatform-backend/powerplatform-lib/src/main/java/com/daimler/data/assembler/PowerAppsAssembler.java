package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.PowerAppNsql;
import com.daimler.data.db.json.Developer;
import com.daimler.data.db.json.PowerAppDetails;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.dto.powerapps.CreatedByVO;
import com.daimler.data.dto.powerapps.DeveloperVO;
import com.daimler.data.dto.powerapps.PowerAppCreateRequestVO;
import com.daimler.data.dto.powerapps.PowerAppVO;

@Component
public class PowerAppsAssembler implements GenericAssembler<PowerAppVO, PowerAppNsql> {

	@Override
	public PowerAppVO toVo(PowerAppNsql entity) {
		PowerAppVO vo = null;
		if(entity!=null) {
			vo = new PowerAppVO();
			vo.setId(entity.getId());
			PowerAppDetails data = entity.getData();
			if(data!=null) {
				BeanUtils.copyProperties(data, vo);
				vo.setRequestedBy(toCreatedByVO(data.getRequestedBy()));
				vo.setRequestedOn(data.getRequestedOn());
				vo.setUpdatedOn(data.getUpdatedOn());
				List<DeveloperVO> developersVO = new ArrayList<>();
				if(data.getDevelopers()!=null && !data.getDevelopers().isEmpty()) {
					developersVO = data.getDevelopers().stream().map(n->toDeveloperVO(n)).collect(Collectors.toList());
				}
				vo.setDevelopers(developersVO);
			}
		}
			return vo;
	}

	public DeveloperVO toDeveloperVO(Developer developer) {
		DeveloperVO vo = new DeveloperVO();
		if(developer!=null) {
			vo.setLicense(developer.getLicense().toUpperCase());
			CreatedByVO userVO = new CreatedByVO();
			if(developer.getUserDetails()!=null) {
				userVO = this.toCreatedByVO(developer.getUserDetails());
				vo.setUserDetails(userVO);
			}
		}
		return vo;
	}
	
	public CreatedByVO toCreatedByVO(UserDetails userdetails) {
		CreatedByVO vo = new CreatedByVO();
		if(userdetails!=null) {
			BeanUtils.copyProperties(userdetails, vo);
		}
		return vo;
	}
	
	public Developer toDeveloper(DeveloperVO developerVO) {
		Developer developer = new Developer();
		if(developerVO!=null) {
			developer.setLicense(developerVO.getLicense().toUpperCase());
			UserDetails user = new UserDetails();
			if(developerVO.getUserDetails()!=null) {
				user = this.toUserDetails(developerVO.getUserDetails());
				developer.setUserDetails(user);
			}
		}
		return developer;
	}
	
	public UserDetails toUserDetails(CreatedByVO vo) {
		UserDetails userdetails = new UserDetails();
		if(vo!=null) {
			BeanUtils.copyProperties(vo,userdetails);
		}
		return userdetails;
	}
	
	@Override
	public PowerAppNsql toEntity(PowerAppVO vo) {
		PowerAppNsql entity = new PowerAppNsql();
		if(vo!=null) {
			entity.setId(vo.getId());
			PowerAppDetails data = new PowerAppDetails();
			BeanUtils.copyProperties(vo, data);
			data.setRequestedBy(toUserDetails(vo.getRequestedBy()));
			data.setRequestedOn(vo.getRequestedOn());
			data.setUpdatedOn(vo.getUpdatedOn());
			List<Developer> developers = new ArrayList<>();
			if(vo.getDevelopers()!=null && !vo.getDevelopers().isEmpty()) {
				developers = vo.getDevelopers().stream().map(n->toDeveloper(n)).collect(Collectors.toList());
			}
			data.setDevelopers(developers);
			entity.setData(data);
		}
		return entity;
	}
	
	public PowerAppVO toVo(PowerAppCreateRequestVO createRequest){
		PowerAppVO vo = new PowerAppVO();
		if(createRequest!=null) {
			vo.setName(createRequest.getName());
			vo.setSubscriptionType(createRequest.getSubscriptionType());
			vo.setEnvOwnerName(createRequest.getEnvOwnerName());
			vo.setEnvOwnerId(createRequest.getEnvOwnerId());
			vo.setDyEnvOwnerId(createRequest.getDyEnvOwnerId());
			vo.setDyEnvOwnerName(createRequest.getDyEnvOwnerName());
			vo.setDepartment(createRequest.getDepartment());
			vo.setEnvironment(createRequest.getEnvironment().toString().toUpperCase());
			vo.setProdEnvAvailability(createRequest.getProdEnvAvailability().toString().toUpperCase());
			vo.setBillingContact(createRequest.getBillingContact());
			vo.setBillingPlant(createRequest.getBillingPlant());
			vo.setBillingCostCentre(createRequest.getBillingCostCentre());
			vo.setCustomRequirements(createRequest.getCustomRequirements());
			vo.setDevelopers(createRequest.getDevelopers());
		}
		return vo;
	}

}
