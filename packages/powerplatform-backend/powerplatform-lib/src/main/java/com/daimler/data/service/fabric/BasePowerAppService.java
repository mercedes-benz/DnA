package com.daimler.data.service.fabric;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.application.client.PowerAppsClient;
import com.daimler.data.assembler.PowerAppsAssembler;
import com.daimler.data.db.entities.PowerAppNsql;
import com.daimler.data.db.repo.forecast.PowerAppCustomRepository;
import com.daimler.data.db.repo.forecast.PowerAppRepository;
import com.daimler.data.dto.powerapps.PowerAppCollectionVO;
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
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	
	public BasePowerAppService() {
		super();
	}
	
	@Override
	@Transactional
	public PowerAppCollectionVO getAll( int limit,  int offset, String user) {
		PowerAppCollectionVO collectionVO = new PowerAppCollectionVO();
		List<PowerAppVO> vos = new ArrayList<>();
		
		List<PowerAppVO> paginatedVOs = new ArrayList<>();
		int totalCount = 0;
		if(vos!=null && !vos.isEmpty()) {
			totalCount = vos.size();
			int newOffset = offset>vos.size() ? 0 : offset;
			if(limit == 0) {
				limit = totalCount;
			}
			int newLimit = offset+limit > vos.size() ? vos.size() : offset+limit;
			paginatedVOs = vos.subList(newOffset, newLimit);
		}
		collectionVO.setRecords(paginatedVOs);
		collectionVO.setTotalCount(totalCount);
		return collectionVO;
	}

	@Override
	public Long getCount(String user) {
		return customRepo.getTotalCount(user);
	}
	
	@Override
	@Transactional
	public PowerAppVO getById(String id) {
		PowerAppVO voFromDb =  super.getById(id);
		log.info("Fetched fabric project record from db successfully for id {} ", id);
		return voFromDb;
	}

	

}
