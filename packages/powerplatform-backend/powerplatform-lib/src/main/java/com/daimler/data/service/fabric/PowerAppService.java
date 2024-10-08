package com.daimler.data.service.fabric;

import com.daimler.data.db.entities.PowerAppNsql;
import com.daimler.data.dto.powerapps.PowerAppCollectionVO;
import com.daimler.data.dto.powerapps.PowerAppVO;
import com.daimler.data.service.common.CommonService;

public interface PowerAppService extends CommonService<PowerAppVO, PowerAppNsql, String> {

	Long getCount(String user);

	PowerAppCollectionVO getAll(int limit, int offset, String user);

}
