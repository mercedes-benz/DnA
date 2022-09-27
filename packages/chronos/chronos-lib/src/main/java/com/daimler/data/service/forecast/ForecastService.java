package com.daimler.data.service.forecast;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.dto.forecast.CreatedByVO;
import com.daimler.data.dto.forecast.ForecastRunRequestVO;
import com.daimler.data.dto.forecast.ForecastRunResponseVO;
import com.daimler.data.dto.forecast.ForecastVO;
import com.daimler.data.dto.forecast.InputFileVO;
import com.daimler.data.dto.forecast.RunVO;
import com.daimler.data.dto.storage.FileUploadResponseDto;
import com.daimler.data.service.common.CommonService;

public interface ForecastService extends CommonService<ForecastVO, ForecastNsql, String> {

	List<InputFileVO> getSavedFiles(String id);

	List<ForecastVO> getAll(int limit, int offset, String user);

	Long getCount(String user);

	ForecastVO createForecast(ForecastVO vo) throws Exception;

	FileUploadResponseDto saveFile(MultipartFile file, String bucketName);

	ForecastRunResponseVO createJobRun(String savedInputPath, Boolean saveRequestPart, String runName,
			String configurationFile, String frequency, BigDecimal forecastHorizon, String comment,
			ForecastVO existingForecast, String triggeredBy, Date triggeredOn);

	Long getRunsCount(String id);

	List<RunVO> getAllRunsForProject( Integer limit,  Integer offset, ForecastVO existingForecast);


}
