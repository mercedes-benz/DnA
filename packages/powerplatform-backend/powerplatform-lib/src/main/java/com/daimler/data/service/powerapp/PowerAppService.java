package com.daimler.data.service.powerapp;

import java.util.List;

import com.daimler.data.db.entities.PowerAppNsql;
import com.daimler.data.dto.powerapps.PowerAppVO;
import com.daimler.data.service.common.CommonService;

public interface PowerAppService extends CommonService<PowerAppVO, PowerAppNsql, String> {

	Long getCount(String user);

	List<PowerAppVO> getAll(int limit, int offset, String user);

	PowerAppVO findbyUniqueLiteral(String name);

}
