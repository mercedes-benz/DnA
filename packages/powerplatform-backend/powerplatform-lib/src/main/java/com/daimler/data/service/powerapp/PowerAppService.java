package com.daimler.data.service.powerapp;

import java.util.List;

import com.daimler.data.db.entities.PowerAppNsql;
import com.daimler.data.dto.powerapps.PowerAppVO;
import com.daimler.data.service.common.CommonService;

public interface PowerAppService extends CommonService<PowerAppVO, PowerAppNsql, String> {

	PowerAppVO findbyUniqueLiteral(String name);

	List<PowerAppVO> getAll(int limit, int offset, String name, String state, String user, String sortBy,
			String sortOrder);

	Long getCount(String name, String state, String user);

}
