package com.daimler.data.service.adaProjects;


import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.ADAProjectsNsql;

import com.daimler.data.dto.adaProjects.ADAProjectDetailsVO;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsCollectionVO;
import com.daimler.data.service.common.CommonService;


public interface ADAProjectsService extends CommonService<ADAProjectDetailsVO, ADAProjectsNsql, String> {

	ADAProjectDetailsCollectionVO getAllProjects(int limit, int offset);
	GenericMessage createNewProject(ADAProjectDetailsVO project);
	GenericMessage updateProject(String id, ADAProjectDetailsVO project);
}
