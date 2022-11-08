package com.daimler.data.service.forecast;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.dto.forecast.ForecastRunResponseVO;
import com.daimler.data.dto.forecast.ForecastVO;
import com.daimler.data.dto.forecast.RunVO;
import com.daimler.data.dto.forecast.RunVisualizationVO;
import com.daimler.data.dto.storage.FileUploadResponseDto;
import com.daimler.data.service.common.CommonService;

public interface ForecastService extends CommonService<ForecastVO, ForecastNsql, String> {

	List<ForecastVO> getAll(int limit, int offset, String user);

	Long getCount(String user);

	ForecastVO createForecast(ForecastVO vo) throws Exception;

	FileUploadResponseDto saveFile(MultipartFile file, String bucketName);

	ForecastRunResponseVO createJobRun(String savedInputPath, Boolean saveRequestPart, String runName,
			String configurationFile, String frequency, BigDecimal forecastHorizon, int hierarchy, String comment,
			Boolean runOnPowerfulMachines, ForecastVO existingForecast, String triggeredBy, Date triggeredOn);

	Long getRunsCount(String id);

	List<RunVO> getAllRunsForProject( int limit,  int offset, ForecastVO existingForecast);

	GenericMessage deletRunByUUID(String id, String rid);

	RunVisualizationVO getRunVisualizationsByUUID(String id, String rid);


}
