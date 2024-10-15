package com.daimler.data.service.powerapp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.application.client.PowerAppsClient;
import com.daimler.data.assembler.PowerAppsAssembler;
import com.daimler.data.db.entities.PowerAppNsql;
import com.daimler.data.db.repo.powerapp.PowerAppCustomRepository;
import com.daimler.data.db.repo.powerapp.PowerAppRepository;
import com.daimler.data.dto.powerapps.PowerAppVO;
import com.daimler.data.service.common.BaseCommonService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BasePowerAppService extends BaseCommonService<PowerAppVO, PowerAppNsql, String> implements PowerAppService{

	@Autowired
	private PowerAppsClient powerAppsClient;
	
	@Autowired
	private PowerAppCustomRepository customRepo;
	
	@Autowired
	private PowerAppRepository jpaRepo;
	
	@Autowired
	private PowerAppsAssembler assembler;
	
	public BasePowerAppService() {
		super();
	}
	
	@Override
	public List<PowerAppVO> getAll( int limit,  int offset, String user) {
		List<PowerAppNsql> entities = customRepo.getAll(user, offset, limit);
		List<PowerAppVO> vos = new ArrayList<>();
		vos = entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
		return vos;
	}

	@Override
	public Long getCount(String user) {
		return customRepo.getTotalCount(user);
	}
	
	@Override
	public PowerAppVO getById(String id) {
		PowerAppVO voFromDb =  super.getById(id);
		log.info("Fetched fabric project record from db successfully for id {} ", id);
		return voFromDb;
	}

	@Override
	public PowerAppVO findbyUniqueLiteral(String name) {
		return assembler.toVo(customRepo.findbyUniqueLiteral(name));
	}

}
