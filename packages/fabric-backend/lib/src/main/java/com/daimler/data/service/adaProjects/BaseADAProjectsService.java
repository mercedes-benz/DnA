package com.daimler.data.service.adaProjects;

import org.springframework.stereotype.Service;

import com.daimler.data.db.entities.ADAProjectsNsql;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVO;
import com.daimler.data.service.common.BaseCommonService;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class BaseADAProjectsService extends BaseCommonService<ADAProjectDetailsVO, ADAProjectsNsql, String> implements ADAProjectsService{


	public BaseADAProjectsService() {
		super();
	}
}
