package com.daimler.data.service.forecast;

import java.util.List;

import javax.validation.Valid;

import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.dto.forecast.ForecastVO;
import com.daimler.data.dto.forecast.InputFileVO;
import com.daimler.data.service.common.CommonService;

public interface ForecastService extends CommonService<ForecastVO, ForecastNsql, String> {

	List<InputFileVO> getSavedFiles(String id);

	List<ForecastVO> getAll(int limit, int offset, String user);

	Long getCount(String user);

	ForecastVO createForecast(ForecastVO vo) throws Exception;

}
