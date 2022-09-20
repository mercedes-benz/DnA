package com.daimler.data.service.forecast;

import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.dto.forecast.ForecastVO;
import com.daimler.data.service.common.CommonService;

public interface ForecastService extends 
	CommonService<ForecastVO, ForecastNsql, String>{

}
