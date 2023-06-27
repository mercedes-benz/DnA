package com.daimler.data.service.forecast;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.daimler.data.dto.forecast.*;
import com.daimler.data.dto.storage.BucketObjectsCollectionWrapperDto;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.dto.storage.FileUploadResponseDto;
import com.daimler.data.service.common.CommonService;

public interface ForecastService extends CommonService<ForecastVO, ForecastNsql, String> {

	List<ForecastVO> getAll(int limit, int offset, String user);

	Long getCount(String user);

	ForecastVO createForecast(ForecastVO vo) throws Exception;

	ForecastRunResponseVO createJobRun(MultipartFile file, String savedInputPath, Boolean saveRequestPart, String runName,
			String configurationFile, String frequency, BigDecimal forecastHorizon, String hierarchy, String comment,
			Boolean runOnPowerfulMachines, ForecastVO existingForecast, String triggeredBy, Date triggeredOn, String infotext);


	Object[] getAllRunsForProject( int limit,  int offset, String forecastId, String sortBy, String sortOrder);

	GenericMessage deletRunByUUID(String id, String rid);

	RunVisualizationVO getRunVisualizationsByUUID(String id, String rid);

	GenericMessage updateForecastByID(String id, ForecastProjectUpdateRequestVO forecastUpdateRequestVO, ForecastVO existingForecast );

	GenericMessage deleteForecastByID(String id);

	GenericMessage generateApiKey(String id);

	ApiKeyVO getApiKey(String id);

	Boolean isBucketExists(String bucketName);
	
	List<String> getAllForecastIds();

	public BucketObjectsCollectionWrapperDto getBucketObjects(String path, String bucketType);

	public ForecastComparisonCreateResponseVO createComparison(String id, ForecastVO existingForecast, List<String> validRunsPath, String comparisionId, String comparisonName,
			String actualsFilePath, String targetFolder, Date createdOn, String requestUser);
	public Object[]  getAllForecastComparisons(int limit, int offset,String id,String sortBy,String sortOrder);
	public GenericMessage  deleteComparison(String id,List<String> validComparisonIds);
	public ForecastComparisonResultVO  getForecastComparisonById(String id,String comparisonId);

	public void processForecastComparision(String forecastId, String comparisonId);
	public ForecastConfigFileUploadResponseVO uploadConfigFile(ForecastVO existingForecast, String configFileId,String requestUser,Date createdOn, String configFilePath, String configFileName);
	public Object[]  getForecastConfigFiles(String id);
	public ForecastConfigFileResultVO  getForecastConfigFileById(String id,String configFileId);


	Integer getTotalCountOfForecastProjects();

	Integer getTotalCountOfForecastUsers();


}
