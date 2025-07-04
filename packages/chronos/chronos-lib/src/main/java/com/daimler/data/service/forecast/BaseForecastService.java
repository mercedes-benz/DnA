package com.daimler.data.service.forecast;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.application.client.ChronosComparisonClient;
import com.daimler.data.application.client.DataBricksClient;
import com.daimler.data.application.client.StorageServicesClient;
import com.daimler.data.assembler.ForecastAssembler;
import com.daimler.data.auth.vault.VaultAuthClientImpl;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.db.json.ComparisonDetails;
import com.daimler.data.db.json.ComparisonState;
import com.daimler.data.db.json.File;
import com.daimler.data.db.json.Forecast;
import com.daimler.data.db.json.LeanGovernanceFeilds;
import com.daimler.data.db.json.NotificationDetails;
import com.daimler.data.db.json.RunDetails;
import com.daimler.data.db.json.RunState;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.db.repo.forecast.ForecastCustomRepository;
import com.daimler.data.db.repo.forecast.ForecastRepository;
import com.daimler.data.dto.comparison.ChronosComparisonRequestDto;
import com.daimler.data.dto.comparison.CreateComparisonResponseWrapperDto;
import com.daimler.data.dto.databricks.DataBricksJobRunOutputResponseWrapperDto;
import com.daimler.data.dto.databricks.RunNowNotebookParamsDto;
import com.daimler.data.dto.forecast.ApiKeyVO;
import com.daimler.data.dto.forecast.BannerResponseVO;
import com.daimler.data.dto.forecast.CancelRunResponseVO;
import com.daimler.data.dto.forecast.CollaboratorVO;
import com.daimler.data.dto.forecast.ComparisonStateVO;
import com.daimler.data.dto.forecast.DataBricksErrorResponseVO;
import com.daimler.data.dto.forecast.ForecastComparisonCreateResponseVO;
import com.daimler.data.dto.forecast.ForecastComparisonResultVO;
import com.daimler.data.dto.forecast.ForecastComparisonVO;
import com.daimler.data.dto.forecast.ForecastConfigFileResultVO;
import com.daimler.data.dto.forecast.ForecastConfigFileUploadResponseVO;
import com.daimler.data.dto.forecast.ForecastProjectUpdateRequestVO;
import com.daimler.data.dto.forecast.ForecastRunResponseVO;
import com.daimler.data.dto.forecast.ForecastVO;
import com.daimler.data.dto.forecast.InputFileVO;
import com.daimler.data.dto.forecast.RunDetailsVO;
import com.daimler.data.dto.forecast.RunNowResponseVO;
import com.daimler.data.dto.forecast.RunStateVO;
import com.daimler.data.dto.forecast.RunStateVO.ResultStateEnum;
import com.daimler.data.dto.forecast.RunVO;
import com.daimler.data.dto.forecast.RunVisualizationVO;
import com.daimler.data.dto.storage.BucketObjectDetailsDto;
import com.daimler.data.dto.storage.BucketObjectsCollectionWrapperDto;
import com.daimler.data.dto.storage.CreateBucketResponseWrapperDto;
import com.daimler.data.dto.storage.DeleteBucketResponseWrapperDto;
import com.daimler.data.dto.storage.FileDownloadResponseDto;
import com.daimler.data.dto.storage.FileUploadResponseDto;
import com.daimler.data.dto.storage.GetBucketByNameResponseWrapperDto;
import com.daimler.data.dto.storage.UpdateBucketResponseWrapperDto;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.util.MultipartFileConverter;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;
import com.google.gson.JsonArray;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseForecastService extends BaseCommonService<ForecastVO, ForecastNsql, String> implements ForecastService{

	@Value("${databricks.jobId}")
	private String dataBricksJobId;

	@Value("${databricks.powerfulMachinesJobId}")
	private String dataBricksPowerfulMachinesJobId;
	
	@Value("${databricks.defaultConfigYml}")
	private String dataBricksJobDefaultConfigYml;

	private static final String EXOGENOUS_FILE_NAME = "X.csv";
	
	@Autowired
	private StorageServicesClient storageClient;
	
	@Autowired
	private DataBricksClient dataBricksClient;
	
	@Autowired
	private ForecastCustomRepository customRepo;
	@Autowired
	private ForecastRepository jpaRepo;
	
	@Autowired
	private ForecastAssembler assembler;
	@Autowired
	private RestTemplate restTemplate;
	@Lazy
	@Autowired
	private VaultAuthClientImpl vaultAuthClient;
	
	@Autowired
	private KafkaProducerService kafkaProducer;

	@Autowired
	HttpServletRequest httpRequest;



	private static String chronosComparisontopicName = "dnaChronosComparisonTopic";

	@Autowired
	private ChronosComparisonClient comparisonClient;
	
	public BaseForecastService() {
		super();
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public List<ForecastVO> getAll( int limit,  int offset, String user) {
		List<ForecastNsql> entities = customRepo.getAll(user, offset, limit);
		if (entities != null && !entities.isEmpty())
			return entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Long getCount(String user) {
		return customRepo.getTotalCount(user);
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public ForecastVO createForecast(ForecastVO vo) throws Exception {
		CreateBucketResponseWrapperDto bucketCreationResponse = storageClient.createBucket(vo.getBucketName(),vo.getCreatedBy(),vo.getCollaborators());
		if(bucketCreationResponse!= null && "SUCCESS".equalsIgnoreCase(bucketCreationResponse.getStatus())) {
			// To store data on minio once bucket is created.
			vo.setBucketId(bucketCreationResponse.getData().getId());
			new ForecastVO();
			ForecastVO forecastVO = super.create(vo);
			return forecastVO;
		}else {
			throw new Exception("Failed while creating bucket for Forecast project artifacts to be stored.");
		}
	}
	
	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public ForecastRunResponseVO createJobRun(MultipartFile file,String savedInputPath, Boolean saveRequestPart, String runName,
			String configurationFile, String frequency, BigDecimal forecastHorizon, String hierarchy, String comment, Boolean runOnPowerfulMachines,
			ForecastVO existingForecast,String triggeredBy, Date triggeredOn,String chronosVersion, String backtesting, InputFileVO savedInputToRemove,
			MultipartFile drivers,MultipartFile mapping,MultipartFile planning_data) {

		String dataBricksJobidForRun = dataBricksJobId;
		ForecastRunResponseVO responseWrapper = new ForecastRunResponseVO();
		RunNowResponseVO runNowResponseVO = new RunNowResponseVO();
		GenericMessage responseMessage = new GenericMessage();
		RunNowNotebookParamsDto noteboookParams = new RunNowNotebookParamsDto();
		String correlationId = UUID.randomUUID().toString();
		String bucketName = existingForecast.getBucketName();
		String resultFolder = bucketName+"/results/"+correlationId + "-" + runName;
		String inputOrginalFolder= "/results/"+correlationId + "-" + runName + "/input_original";
		FileUploadResponseDto fileUploadResponse =  null;
		boolean configValidation = false;
		if(configurationFile!=null) {
			try {
				String[] splits = configurationFile.split("/");
				if(splits!=null && splits.length>1) {
					String configFilebucketName = splits[0];
					String fileNamePrefix = splits[1]+ "/" +splits[2];
					if("chronos-core".equalsIgnoreCase(configFilebucketName) || bucketName.equalsIgnoreCase(configFilebucketName)) {
						List<BucketObjectDetailsDto>  configFiles = storageClient.getFilesPresent(configFilebucketName, "configs/");
						configValidation = storageClient.isFilePresent(fileNamePrefix, configFiles);
					}
				}
			}catch(Exception e)	{
				log.error("Invalid configuration file");
			}
		}
		if(!configValidation) {
			log.error("Failed while fetching config file {} for project name {} and id {} ",configurationFile, existingForecast.getName(), existingForecast.getId());
			MessageDescription invalidMsg = new MessageDescription("Failed while fetching config file " + configurationFile + " unable to trigger run");
			List<MessageDescription> errors = new ArrayList<>();
			errors.add(invalidMsg);
			responseMessage.setErrors(errors);
			responseWrapper.setData(null);
			responseWrapper.setResponse(responseMessage);
			return responseWrapper;
		}
		if(file!=null) {
			fileUploadResponse = storageClient.uploadFile(inputOrginalFolder, file,existingForecast.getBucketName());
		}else {
				if(savedInputPath!=null && savedInputPath.contains("/")) {
					try {
						String path = savedInputPath;
						String[] SavedfileDetails = path.split("/",2);
						FileDownloadResponseDto savedFileResponse = storageClient.getFileContents(SavedfileDetails[0], "/"+SavedfileDetails[1]);
						if(savedFileResponse!=null && savedFileResponse.getData()!=null && 
								(savedFileResponse.getErrors()==null || savedFileResponse.getErrors().size()<1)) {
							fileUploadResponse = storageClient.uploadFile(inputOrginalFolder, savedFileResponse.getData(),existingForecast.getBucketName());
						}
					}catch(Exception e) {
						log.error("Failed while reusing savedinputfile {} attached for project name {} and id {} ",savedInputPath, existingForecast.getName(), existingForecast.getId());
						MessageDescription invalidMsg = new MessageDescription("Failed while reusing savedinputfile " + savedInputPath);
						List<MessageDescription> errors = new ArrayList<>();
						errors.add(invalidMsg);
						responseMessage.setErrors(errors);
						responseWrapper.setData(null);
						responseWrapper.setResponse(responseMessage);
						return responseWrapper;	
					}
					
				}else {
					log.error("Invalid savedinputfilepath {} attached for project name {} and id {} ",savedInputPath, existingForecast.getName(), existingForecast.getId());
					MessageDescription invalidMsg = new MessageDescription("Invalid saved file path attached.");
					List<MessageDescription> errors = new ArrayList<>();
					errors.add(invalidMsg);
					responseMessage.setErrors(errors);
					responseWrapper.setData(null);
					responseWrapper.setResponse(responseMessage);
					return responseWrapper;	
				}
		}
		if(fileUploadResponse==null || (fileUploadResponse!=null && (fileUploadResponse.getErrors()!=null || !"SUCCESS".equalsIgnoreCase(fileUploadResponse.getStatus())))) {
			log.error("Error in uploading file to {} for forecast project {}",inputOrginalFolder,existingForecast.getName() );
			MessageDescription msg = new MessageDescription("Failed to  upload file to " + inputOrginalFolder + "for" + existingForecast.getName() );
			List<MessageDescription> errors = new ArrayList<>();
			errors.add(msg);
			responseMessage.setErrors(errors);
			responseWrapper.setData(null);
			responseWrapper.setResponse(responseMessage);

		return responseWrapper;

		}
		if(drivers!=null) {
			fileUploadResponse = storageClient.uploadFile(inputOrginalFolder, drivers,existingForecast.getBucketName());
			if(fileUploadResponse==null || (fileUploadResponse!=null && (fileUploadResponse.getErrors()!=null || !"SUCCESS".equalsIgnoreCase(fileUploadResponse.getStatus())))) {
				log.error("Error in uploading file to {} for forecast project {}",inputOrginalFolder,existingForecast.getName() );
				MessageDescription msg = new MessageDescription("Failed to  upload file to " + inputOrginalFolder + "for" + existingForecast.getName() );
				List<MessageDescription> errors = new ArrayList<>();
				errors.add(msg);
				responseMessage.setErrors(errors);
				responseWrapper.setData(null);
				responseWrapper.setResponse(responseMessage);
	
			return responseWrapper;
			}
		}
		if(mapping!=null) {
			fileUploadResponse = storageClient.uploadFile(inputOrginalFolder, mapping,existingForecast.getBucketName());
			if(fileUploadResponse==null || (fileUploadResponse!=null && (fileUploadResponse.getErrors()!=null || !"SUCCESS".equalsIgnoreCase(fileUploadResponse.getStatus())))) {
				log.error("Error in uploading file to {} for forecast project {}",inputOrginalFolder,existingForecast.getName() );
				MessageDescription msg = new MessageDescription("Failed to  upload file to " + inputOrginalFolder + "for" + existingForecast.getName() );
				List<MessageDescription> errors = new ArrayList<>();
				errors.add(msg);
				responseMessage.setErrors(errors);
				responseWrapper.setData(null);
				responseWrapper.setResponse(responseMessage);
	
			return responseWrapper;
			}
		}
		if(planning_data!=null) {
			fileUploadResponse = storageClient.uploadFile(inputOrginalFolder, planning_data,existingForecast.getBucketName());
			if(fileUploadResponse==null || (fileUploadResponse!=null && (fileUploadResponse.getErrors()!=null || !"SUCCESS".equalsIgnoreCase(fileUploadResponse.getStatus())))) {
				log.error("Error in uploading file to {} for forecast project {}",inputOrginalFolder,existingForecast.getName() );
				MessageDescription msg = new MessageDescription("Failed to  upload file to " + inputOrginalFolder + "for" + existingForecast.getName() );
				List<MessageDescription> errors = new ArrayList<>();
				errors.add(msg);
				responseMessage.setErrors(errors);
				responseWrapper.setData(null);
				responseWrapper.setResponse(responseMessage);
	
			return responseWrapper;
			}
		}
		noteboookParams.setConfig(configurationFile);
		noteboookParams.setCorrelationId(correlationId);
		if(savedInputPath!=null) {
			if(savedInputPath.toLowerCase().contains(".xlsx")){
				noteboookParams.setExcel(savedInputPath);
				noteboookParams.setY("");
			}else {
				if(savedInputPath.toLowerCase().contains(".csv")){
					noteboookParams.setExcel("");
					noteboookParams.setY(savedInputPath);
				}
			}
		}
		noteboookParams.setFh(forecastHorizon.toString());
		noteboookParams.setHierarchy(hierarchy);
		noteboookParams.setFreq(this.toFrequencyParam(frequency));

		noteboookParams.setResults_folder(resultFolder);
		if(drivers==null){
			noteboookParams.setX("");
		}else{
			noteboookParams.setX(existingForecast.getBucketName() + "/inputs/"
			+ drivers.getOriginalFilename());
		}
		if(mapping==null){
			noteboookParams.setMapping("");
		}else{
			noteboookParams.setMapping(existingForecast.getBucketName() + "/inputs/"
			+ mapping.getOriginalFilename());
		}
		if(planning_data==null){
			noteboookParams.setPlanning_data("");
		}else{
			noteboookParams.setPlanning_data(existingForecast.getBucketName() + "/inputs/"
			+ planning_data.getOriginalFilename());
		}
		noteboookParams.setChronos_version(chronosVersion);

		noteboookParams.setUser_id(triggeredBy);

		if(backtesting!=null && !backtesting.trim().isEmpty() ){
			noteboookParams.setBacktesting(backtesting);
		}


		log.info("notebookParams for bactesting"+ noteboookParams);

		RunNowResponseVO runNowResponse = dataBricksClient.runNow(correlationId, noteboookParams, runOnPowerfulMachines);
		if(runNowResponse!=null) {
			if(runNowResponse.getErrorCode()!=null || runNowResponse.getRunId()==null) 
				responseMessage.setSuccess("FAILED");
			else {
				responseMessage.setSuccess("SUCCESS");
				runNowResponse.setCorrelationId(correlationId);
				Optional<ForecastNsql> anyEntity = this.jpaRepo.findById(existingForecast.getId());
				ForecastNsql entity = null;
				if(anyEntity.isPresent())
					entity = anyEntity.get();
				List<RunDetails> existingRuns = entity.getData().getRuns();
				if(existingRuns==null || existingRuns.isEmpty())
					existingRuns = new ArrayList<>();
				RunDetails currentRun = new RunDetails();
				currentRun.setComment(comment);
				currentRun.setConfigurationFile(configurationFile);
				currentRun.setForecastHorizon(forecastHorizon.toString());
				currentRun.setHierarchy(hierarchy.toString());
				currentRun.setFrequency(frequency);
				currentRun.setId(correlationId);
				currentRun.setInputFile(savedInputPath);
				currentRun.setIsDelete(false);
				if (runOnPowerfulMachines) {
					dataBricksJobidForRun = dataBricksPowerfulMachinesJobId;
				} 
				currentRun.setJobId(dataBricksJobidForRun);
				currentRun.setNumberInJob(runNowResponse.getNumberInJob());
				currentRun.setRunId(runNowResponse.getRunId());
				currentRun.setRunName(runName);
				currentRun.setTriggeredBy(triggeredBy);
				currentRun.setTriggeredOn(triggeredOn);
				currentRun.setIsDelete(false);
				currentRun.setExogenData(false);
				RunState newRunState = new RunState();
				newRunState.setLife_cycle_state("PENDING");
				newRunState.setUser_cancelled_or_timedout(false);
				currentRun.setRunState(newRunState);
				currentRun.setResultFolderPath(resultFolder);
				currentRun.setInfotext(chronosVersion);
				currentRun.setBacktesting(backtesting);
				runNowResponse.setResultFolderPath(resultFolder);;
				existingRuns.add(currentRun);
				entity.getData().setRuns(existingRuns);
				entity.getData().setSavedInputs(this.assembler.toFiles(existingForecast.getSavedInputs()));
				try {
					this.jpaRepo.save(entity);
					if(savedInputToRemove!=null) {
						List<String> memberIds = new ArrayList<>();
						List<String> memberEmails = new ArrayList<>();
						if (entity.getData().getCollaborators() != null) {
							memberIds = entity.getData().getCollaborators().stream()
									.map(UserDetails::getId).collect(Collectors.toList());
							memberEmails = entity.getData().getCollaborators().stream()
									.map(UserDetails::getEmail).collect(Collectors.toList());
						}
						String ownerId = entity.getData().getCreatedBy().getId();
						memberIds.add(ownerId);
						String ownerEmail = entity.getData().getCreatedBy().getEmail();
						memberEmails.add(ownerEmail);
						String message = "Input file " + savedInputToRemove.getName() + " uploaded by " + triggeredBy + " for chronos-project " + existingForecast.getName() + " overwritten successfully";
						String notificationEventName = "Chronos Upload Saved Input File Overwrite";
						notifyUsers(existingForecast.getId(), memberIds, memberEmails, message, "", notificationEventName, null);
					}
					}catch(Exception e) {
					log.error("Failed while saving details of run {} and correaltionId {} to database for project {}",runNowResponse.getRunId(),correlationId
							, existingForecast.getName());
					MessageDescription msg = new MessageDescription("Failed to save run details to table after creating databricks job run with runid "+runNowResponse.getRunId());
					List<MessageDescription> errors = new ArrayList<>();
					errors.add(msg);
					responseMessage.setErrors(errors);
				}
			}
			responseWrapper.setData(runNowResponse);
			responseWrapper.setResponse(responseMessage);
		}
		return responseWrapper;
	}
	
	private String toFrequencyParam(String value) {
		switch(value) {
		case "Daily" : return "D";
		case "Weekly" : return "W";
		case "Monthly" : return "M";
		case "Yearly" : return "Y";
		default: return value;
		}
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Object[] getAllRunsForProject(int limit, int offset, String forecastId,String sortBy, String sortOrder) {
		Object[] runCollectionWrapper = new Object[2];
		
		List<RunDetails> updatedRuns = new ArrayList<>();
		List<RunVO> updatedRunVOList = new ArrayList<>();
		
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(forecastId);
		long totalCount = 0L;
		if(entityOptional.isPresent()) {
			ForecastNsql entity = entityOptional.get();
			String forecastName = entity.getData().getName();
			if(entity!=null && entity.getData()!=null && 	
					entity.getData().getRuns()!=null && !entity.getData().getRuns().isEmpty()) {
				List<RunDetails> existingRuns = entity.getData().getRuns();
				if(existingRuns!=null && !existingRuns.isEmpty()) {
				List<NotificationDetails> notificationDetails = new ArrayList<>();

					String configFileName="";
					List<String> memberIds = new ArrayList<>();
					List<String> memberEmails = new ArrayList<>();
					if (entity.getData().getCollaborators() != null) {
						memberIds = entity.getData().getCollaborators().stream()
								.map(UserDetails::getId).collect(Collectors.toList());
						memberEmails = entity.getData().getCollaborators().stream()
								.map(UserDetails::getEmail).collect(Collectors.toList());
					}

					String ownerId = entity.getData().getCreatedBy().getId();
					memberIds.add(ownerId);
					String ownerEmail = entity.getData().getCreatedBy().getEmail();
					memberEmails.add(ownerEmail);
				String bucketName = entity.getData().getBucketName();
				String resultsPrefix = "results/";
				List<RunDetails> newSubList =new ArrayList<>();
				
				//logic to remove all deleted runs from list
				List<RunDetails> tempExistingRuns = new ArrayList<>(existingRuns);
				for(int i=0; i<existingRuns.size(); i++) {
					RunDetails details= existingRuns.get(i);
					if(details.getIsDelete() != null) {
						boolean isDelete = details.getIsDelete();
						if(isDelete) {
							tempExistingRuns.remove(details);
						}
					}
										
				}

             log.info("sorting runs by sortOrder as {} , order by {}", sortBy,sortOrder);
						switch (sortBy) {
							case "createdOn":
								Comparator<RunDetails> runCreatedOn = (v1, v2) -> (v2.getTriggeredOn().compareTo(v1.getTriggeredOn()));
								if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
									Collections.sort(tempExistingRuns, runCreatedOn);
								}
								else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
									Collections.sort(tempExistingRuns, Collections.reverseOrder(runCreatedOn));
								}
								break;
							case "runName":
								Comparator<RunDetails> runName = (v1, v2) -> (v2.getRunName().compareTo(v1.getRunName()));
								if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
									Collections.sort(tempExistingRuns, runName);
								}
								else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
									Collections.sort(tempExistingRuns, Collections.reverseOrder(runName));
								}
								break;
							case "status":
								Comparator<RunDetails> runStatus = (v1, v2) -> (v2.getRunState().getResult_state().compareTo(v1.getRunState().getResult_state()));
								if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
									Collections.sort(tempExistingRuns, runStatus);
								}
								else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
									Collections.sort(tempExistingRuns, Collections.reverseOrder(runStatus));
								}
								break;
							case "createdBy":
								Comparator<RunDetails> runCreatedBy = (v1, v2) -> (v2.getTriggeredBy().compareTo(v1.getTriggeredBy()));
								if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
									Collections.sort(tempExistingRuns, runCreatedBy);
								}
								else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
									Collections.sort(tempExistingRuns, Collections.reverseOrder(runCreatedBy));
								}
								break;
							case "inputFile":
								Comparator<RunDetails> inputFile = (v1, v2) -> (v2.getInputFile().compareTo(v1.getInputFile()));
								if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
									Collections.sort(tempExistingRuns, inputFile);
								}
								else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
									Collections.sort(tempExistingRuns, Collections.reverseOrder(inputFile));
								}
								break;
							case "forecastHorizon":
								Comparator<RunDetails> forecastHorizon = (v1, v2) -> Integer.parseInt(v2.getForecastHorizon()) - Integer.parseInt(v1.getForecastHorizon());
								if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
									Collections.sort(tempExistingRuns, forecastHorizon);
								}
								else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
									Collections.sort(tempExistingRuns, Collections.reverseOrder(forecastHorizon));
								}
								break;
							case "exogenData":
								Comparator<RunDetails> exogenData = (v1, v2) -> (v2.getExogenData().compareTo(v1.getExogenData()));
								if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
									Collections.sort(tempExistingRuns, exogenData);
								}
								else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
									Collections.sort(tempExistingRuns, Collections.reverseOrder(exogenData));
								}
								break;
								default:
								log.info("Case not found");
								break;
						}
				log.info("runs sorted successfully");
				totalCount = tempExistingRuns.size();
				int endLimit = offset + limit;
				if (endLimit > tempExistingRuns.size()) {
					endLimit = tempExistingRuns.size();
				}
				newSubList = tempExistingRuns.subList(offset, endLimit);
				if (limit == 0)
					newSubList = tempExistingRuns;
				for(RunDetails run: newSubList) {
					RunState state = run.getRunState();
					String runId = run.getRunId();
					String correlationId= run.getId();
					String existingLifecycleState = run.getRunState().getLife_cycle_state();  
//					if(run.getExogenData()==null)
//						run.setExogenData(false);
					if(runId!=null && (run.getIsDelete() == null || !run.getIsDelete()) &&
							(state==null || state.getResult_state()==null || state.getLife_cycle_state()==null ||
									"PENDING".equalsIgnoreCase(state.getLife_cycle_state()) ||
									"RUNNING".equalsIgnoreCase(state.getLife_cycle_state()))) {
						RunDetailsVO updatedRunResponse = this.dataBricksClient.getSingleRun(runId);
						if(updatedRunResponse!=null && runId.equals(updatedRunResponse.getRunId())) {
							log.info("Able to fetch updated run details for forecast {} and correlation {} which was in {}", forecastId,correlationId,state.getLife_cycle_state());
							RunDetails updatedRunDetail = new RunDetails();
							BeanUtils.copyProperties(run, updatedRunDetail);



							updatedRunDetail.setCreatorUserName(updatedRunResponse.getCreatorUserName());
							if(updatedRunResponse.getEndTime()!=null)
								updatedRunDetail.setEndTime(updatedRunResponse.getEndTime().longValue());
							if(updatedRunResponse.getExecutionDuration()!=null)
								updatedRunDetail.setExecutionDuration(updatedRunResponse.getExecutionDuration().longValue());
							if(updatedRunResponse.getSetupDuration()!=null)
								updatedRunDetail.setSetupDuration(updatedRunResponse.getSetupDuration().longValue());
							if(updatedRunResponse.getStartTime()!=null)
								updatedRunDetail.setStartTime(updatedRunResponse.getStartTime().longValue());
							if(updatedRunResponse.getState()!=null) {
								RunStateVO updatedState = updatedRunResponse.getState();
								RunState newState = new RunState();								
								if(updatedState.getLifeCycleState()!=null)
									newState.setLife_cycle_state(updatedState.getLifeCycleState().name());
								String updatedStateMsg = "";
								if(updatedRunResponse.getState().getStateMessage()!=null) {
									updatedStateMsg = updatedState.getStateMessage();
								}
								if(updatedState.getResultState()!=null) {
									newState.setResult_state(updatedState.getResultState().name());
									if("SUCCESS".equalsIgnoreCase(updatedState.getResultState().name())) {
										//check if .SUCCESS file exists
										String resultFolderPathForRun = resultsPrefix + updatedRunDetail.getId()+"-"+updatedRunDetail.getRunName()+"/";
										String configFileRecommendationPath=resultFolderPathForRun+"proposed_config/";
										List<BucketObjectDetailsDto> bucketObjectDetails=storageClient.getFilesPresent(bucketName,resultFolderPathForRun);

										Boolean successFileFlag = storageClient.isFilePresent(resultFolderPathForRun+ "SUCCESS", bucketObjectDetails);
										Boolean warningsFileFlag = storageClient.isFilePresent(resultFolderPathForRun+ "WARNINGS.txt", bucketObjectDetails);
										Boolean warningsInfoFileFlag = storageClient.isFilePresent(resultFolderPathForRun+ "run_info.txt", bucketObjectDetails);
										Boolean exogenousFileFlag = storageClient.isFilePresent(resultFolderPathForRun+ EXOGENOUS_FILE_NAME, bucketObjectDetails);

										//upload recommended config file to project is proposed_config folder is present

										List<BucketObjectDetailsDto> bucketObjectDetailsForConfigRecommendation = storageClient.getFilesPresent(bucketName, configFileRecommendationPath);
										if (bucketObjectDetailsForConfigRecommendation != null && bucketObjectDetailsForConfigRecommendation.size() > 0) {
											Optional<String> objectName = bucketObjectDetailsForConfigRecommendation.stream().map(BucketObjectDetailsDto::getObjectName).findFirst();
											configFileName = objectName.get();
											String fileName = configFileName.substring(configFileName.lastIndexOf('/') + 1);
											ResponseEntity<ByteArrayResource> configRecommendationFileDownloadResponse = storageClient.getDownloadFile(bucketName, configFileName);
											log.info("successfully retrieved configRecommendationFile contents for forecast {} and correaltionid{} and runname{}",
													bucketName, correlationId, run.getRunName());
											ByteArrayResource byteArrayResource = configRecommendationFileDownloadResponse.getBody();
											byte[] bytes = byteArrayResource.getByteArray();
											MultipartFile multipartFile = new MultipartFileConverter(bytes, byteArrayResource.getFilename(), "configFile", "application/octet-stream");
											Date createdOn = new Date();
											SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
											try {
												createdOn = isoFormat.parse(isoFormat.format(createdOn));
											} catch (Exception e) {
												log.warn("Failed to format createdOn date to ISO format");
											}

											// Use the multipartFile in your application

											boolean duplicateFile = false;
											NotificationDetails configRecommendationNotification = new NotificationDetails();
											String uploadConfigMessage = "";
											String uploadConfigNotificationEventName = "Chronos:  Recommendation file generated" +  " for run '" + run.getRunName() + "'";
											String configFileIdId = UUID.randomUUID().toString();
											List<File> configFiles = entity.getData().getConfigFiles();
											if (configFiles != null && !configFiles.isEmpty()) {
												log.error("File with name already exists in uploaded config files list. Project {} and file {}", forecastName, fileName);
												File configToRemove = null;
												for (File configFileDetails : configFiles) {
													if (configFileDetails.getName().equals(multipartFile.getOriginalFilename())) {
														duplicateFile = true;
														configToRemove = configFileDetails;
														break;
													}
												}
												if (configToRemove != null) {
													configFiles.remove(configToRemove);
												}

											} else {
												configFiles = new ArrayList<>();
											}


												FileUploadResponseDto fileUploadResponse = storageClient.uploadFile("/configs/", multipartFile, bucketName);
												if (fileUploadResponse == null || (fileUploadResponse != null && (fileUploadResponse.getErrors() != null || !"SUCCESS".equalsIgnoreCase(fileUploadResponse.getStatus())))) {
													log.error("Failed to upload config file {} to storage bucket", fileName);
													uploadConfigMessage = "New recommendation file generated based on "+run.getRunName()+ " inputs, Failed to upload recommendation " +fileName +  " to project specific configs with exception.";
													configRecommendationNotification.setNotificationEventName(uploadConfigNotificationEventName);
													configRecommendationNotification.setMessage(uploadConfigMessage);
													notificationDetails.add(configRecommendationNotification);

												} else if ("SUCCESS".equalsIgnoreCase(fileUploadResponse.getStatus())) {
													List<InputFileVO> configFilesVOList = new ArrayList<>();
													configFilesVOList = this.assembler.toFilesVO(configFiles);
													log.info("Successfully to uploaded config file {} to storage bucket", fileName);
													InputFileVO currentConfigInput = new InputFileVO();
													configFileName=multipartFile.getOriginalFilename();
													currentConfigInput.setName(multipartFile.getOriginalFilename());
													currentConfigInput.setPath(bucketName + "/configs/" + multipartFile.getOriginalFilename());
													currentConfigInput.setId(configFileIdId);
													currentConfigInput.setCreatedOn(createdOn);
													currentConfigInput.setCreatedBy(ownerId);
													configFilesVOList.add(currentConfigInput);
													entity.getData().setConfigFiles(this.assembler.toConfigFiles(configFilesVOList));
													configRecommendationNotification.setNotificationEventName(uploadConfigNotificationEventName);
													if(!duplicateFile) {
														uploadConfigMessage = "New recommendation file generated based on " + run.getRunName() + " inputs, Successfully uploaded recommendation " + fileName + " to project specific configs.";
													}
													else if(duplicateFile) {
														uploadConfigMessage = "Recommendation File with name " + fileName+ "already exists in uploaded config files list, hence overwritten.";
													}
													configRecommendationNotification.setMessage(uploadConfigMessage);
													notificationDetails.add(configRecommendationNotification);
												}

										}

										//check if exogenous data is present
										if(exogenousFileFlag){
											updatedRunDetail.setExogenData(true);
										}else{
											updatedRunDetail.setExogenData(false);
										}
										log.info("Run state is success from databricks and successFileFlag value is {} and warningsFileFlag is {} , for bucket {} and prefix {} ", successFileFlag, warningsFileFlag, bucketName, resultFolderPathForRun);
										if(warningsFileFlag || warningsInfoFileFlag){
											newState.setResult_state(ResultStateEnum.WARNINGS.name());
											//fetch file content from warnings.txt file
											String commonPrefix = "/results/"+run.getId() + "-" + run.getRunName();
											String warningsPrefix = commonPrefix +"/WARNINGS.txt";
											String warningsInfoPrefix = commonPrefix +"/run_info.txt";
											String warningsResult = "";
											String warningsInfoResult = "";

											FileDownloadResponseDto warningsTextDownloadResponse = storageClient.getFileContents(bucketName, warningsPrefix);
											FileDownloadResponseDto warningsInfoTextDownloadResponse = storageClient.getFileContents(bucketName, warningsInfoPrefix);
											if(warningsFileFlag && (warningsTextDownloadResponse!= null && warningsTextDownloadResponse.getData()!=null && (warningsTextDownloadResponse.getErrors()==null || warningsTextDownloadResponse.getErrors().isEmpty()))) {
												warningsResult = new String(warningsTextDownloadResponse.getData().getByteArray());
												log.info("successfully retrieved warnings.txt file contents for forecast {} and correaltionid{} and runname{}",
														bucketName, correlationId, run.getRunName());
											}
											if(warningsInfoFileFlag && (warningsInfoTextDownloadResponse!= null && warningsInfoTextDownloadResponse.getData()!=null && (warningsInfoTextDownloadResponse.getErrors()==null || warningsInfoTextDownloadResponse.getErrors().isEmpty()))) {
												warningsInfoResult = new String(warningsInfoTextDownloadResponse.getData().getByteArray());
												newState.setResult_state(ResultStateEnum.INFO.name());
												log.info("successfully retrieved run_info.txt file contents for forecast {} and correaltionid{} and runname{}",
														bucketName, correlationId, run.getRunName());
											}
											updatedRunDetail.setWarnings(warningsResult);
											updatedRunDetail.setWarningsInfo(warningsInfoResult);
											updatedStateMsg = "Run was completed with warnings."; 
										}
										else{
											if(!successFileFlag) {
												if(bucketObjectDetails!=null && !bucketObjectDetails.isEmpty()) {
													newState.setResult_state(ResultStateEnum.FAILED.name());
													String errMsg = "Run failed as there was no SUCCESS file under results folder.";
													updatedRunDetail.setError(errMsg);
													updatedStateMsg = errMsg;
												}else {
													newState.setResult_state(ResultStateEnum.FAILED.name());
													String errMsg = "Run failed, couldnt find files/connection to validate results at storage";
													updatedRunDetail.setError(errMsg);
													updatedStateMsg = errMsg;
												}
											}
										}
									}else {
										//add error message if run fails
										String taskRunId= "";
										String errorMessage="";
										if(updatedRunResponse.getTasks()!=null && updatedRunResponse.getTasks().size()>=1  && updatedRunResponse.getTasks().get(0)!=null && updatedRunResponse.getTasks().get(0).getRunId()!=null) {
										 taskRunId=updatedRunResponse.getTasks().get(0).getRunId();
										 errorMessage=processErrorMessages(taskRunId);
										}else {
											errorMessage="Run does not exist.";
										}
										updatedRunDetail.setError(errorMessage);
										updatedRunDetail.setTaskRunId(taskRunId);
										updatedStateMsg = errorMessage;
									}
									

									String message ="";
									NotificationDetails runUpdateNotification = new NotificationDetails();
									message="Run '" + run.getRunName() + "' triggered by " + run.getTriggeredBy() +" for Chronos project '"+ forecastName + "' completed with ResultState " + newState.getResult_state() +". Please check forecast results for more details.";
									String notificationEventName = "Chronos: " + newState.getResult_state() + " for run '" + run.getRunName() + "'" ;
									runUpdateNotification.setMessage(message);
									runUpdateNotification.setNotificationEventName(notificationEventName);
									notificationDetails.add(runUpdateNotification);
								}
								
								newState.setState_message(updatedStateMsg);
								newState.setUser_cancelled_or_timedout(updatedState.isUserCancelledOrTimedout());
								updatedRunDetail.setRunState(newState);

							}
							log.info("updating new pending run {} of project {} ", run.getRunName(), forecastName);
							updatedRuns.add(updatedRunDetail);

						}else {
							log.debug("Adding pending run {} of project {} without update, since getrun response is failed or null ", run.getRunName(), forecastName);
							updatedRuns.add(run);
						}
					}
					else {
						if(runId != null && (run.getIsDelete() == null || !run.getIsDelete()) && (state != null &&
								("TERMINATED".equalsIgnoreCase(state.getLife_cycle_state()) ||
								"INTERNAL_ERROR".equalsIgnoreCase(state.getLife_cycle_state()) ||
								"SKIPPED".equalsIgnoreCase(state.getLife_cycle_state()))) &&
								(run.getResultFolderPath() == null || "".equalsIgnoreCase(run.getResultFolderPath()))
						) {
							String resultFolderPathForRun = bucketName + "/" + resultsPrefix + run.getId()+"-"+run.getRunName();
							run.setResultFolderPath(resultFolderPathForRun);
	          			}
						RunDetails updatedRunDetail = new RunDetails();
						if (runId != null && (run.getIsDelete() == null || !run.getIsDelete()) && (state != null &&
								("TERMINATED".equalsIgnoreCase(state.getLife_cycle_state()) ||
								"INTERNAL_ERROR".equalsIgnoreCase(state.getLife_cycle_state()) ||
								"SKIPPED".equalsIgnoreCase(state.getLife_cycle_state()))) &&
								!"SUCCESS".equalsIgnoreCase(state.getResult_state()) && !"INFO".equalsIgnoreCase(state.getResult_state()) && !"WARNINGS".equalsIgnoreCase(state.getResult_state()) && !"CANCELED".equalsIgnoreCase(state.getResult_state()) && (run.getError() == null || "".equalsIgnoreCase(run.getError())
								|| run.getRunState().getState_message() == null || "".equalsIgnoreCase(run.getRunState().getState_message())
								|| ". ".equalsIgnoreCase(run.getRunState().getState_message()))
						){
							RunDetailsVO updatedRunResponse = this.dataBricksClient.getSingleRun(runId);
							if(updatedRunResponse!=null && runId.equals(updatedRunResponse.getRunId())) {
								log.info(" Updating error msg for failed old run {} of forecast project {} after getting errorMessage from output", run.getRunName(), bucketName);
								BeanUtils.copyProperties(run, updatedRunDetail);
								String taskRunId="";
								String errorMessage="";
								if(updatedRunResponse.getTasks()!=null && updatedRunResponse.getTasks().size()>=1 && updatedRunResponse.getTasks().get(0)!=null && updatedRunResponse.getTasks().get(0).getRunId()!=null) {
									taskRunId = updatedRunResponse.getTasks().get(0).getRunId();
									errorMessage=processErrorMessages(taskRunId);
								}else {
									errorMessage="Run does not exist.";
								}
								updatedRunDetail.setError(errorMessage);
								updatedRunDetail.setTaskRunId(taskRunId);
								updatedRunDetail.getRunState().setState_message(errorMessage);
								updatedRuns.add(updatedRunDetail);
							}
							else {
								log.debug("Adding old failed run {} of project {} without update, since getSingleRun response is null ", run.getRunName(), forecastName);
								updatedRuns.add(run);
							}
						} else {
							//check if exogenous data is present
							if(("SUCCESS".equalsIgnoreCase(state.getResult_state()) || "INFO".equalsIgnoreCase(state.getResult_state()) || "WARNINGS".equalsIgnoreCase(state.getResult_state())) && run.getExogenData()==null) {
								String resultFolderPathForRun = resultsPrefix + run.getId()+"-"+run.getRunName()+"/";
								List<BucketObjectDetailsDto> bucketObjectDetails=storageClient.getFilesPresent(bucketName,resultFolderPathForRun);
								Boolean exogenousFilePresent = storageClient.isFilePresent(resultFolderPathForRun+ EXOGENOUS_FILE_NAME, bucketObjectDetails);
								if(exogenousFilePresent){
									run.setExogenData(true);
									log.info("Adding old success run {} of project {} with exogenous data to true ", run.getRunName(), forecastName);
								}else {
									run.setExogenData(false);
									log.info("Adding old success run {} of project {} with exogenous data to false ", run.getRunName(), forecastName);
								}
							}else {
								log.debug("Adding old success run {} of project {} without update ", run.getRunName(), forecastName);
							}
								updatedRuns.add(run);
						}
					}
				}
				List<RunDetails> updatedDbRunRecords = new ArrayList<>();

					for(RunDetails existingrunRecord: existingRuns) {
						RunDetails updatedRecord = updatedRuns.stream().filter(x -> existingrunRecord.getId().equals(x.getId())).findAny().orElse(null);
						if(updatedRecord!=null) {
							updatedDbRunRecords.add(updatedRecord);
						}
						else {
							updatedDbRunRecords.add(existingrunRecord);
						}
					}
				entity.getData().setRuns(updatedDbRunRecords);
				this.jpaRepo.save(entity);
				for(NotificationDetails notification: notificationDetails){

					notifyUsers(forecastId, memberIds, memberEmails,notification.getMessage(),"",notification.getNotificationEventName(),null);
				}
				updatedRunVOList = this.assembler.toRunsVO(updatedRuns);
			}
			}
		}
		runCollectionWrapper[0] = updatedRunVOList;
		runCollectionWrapper[1] = totalCount;
		return runCollectionWrapper;
	}
	
	
	private void notifyUsers(String forecastId, List<String> memberIds, List<String> memberEmails,String message, String messageDetails, String eventName, String destinationTopicName) {
		kafkaProducer.send(eventName, forecastId, messageDetails, "DnaSystemUser", message,
				true, memberIds, memberEmails, null, destinationTopicName);
	}

	private String processErrorMessages(String taskRunId) {
		DataBricksJobRunOutputResponseWrapperDto updatedRunOutputResponse = this.dataBricksClient.getSingleRunOutput(taskRunId);
		String errMessage=null;
		if(updatedRunOutputResponse!=null){
			if(updatedRunOutputResponse.getError()!=null && !"".equalsIgnoreCase(updatedRunOutputResponse.getError())){
				errMessage=updatedRunOutputResponse.getError();
			}
			else {
				errMessage = updatedRunOutputResponse.getMetadata().getState().getStateMessage();
			}
		}

		return errMessage;
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public RunVisualizationVO getRunVisualizationsByUUID(String id, String rid) {
		RunVisualizationVO visualizationVO = new RunVisualizationVO();
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);
		ForecastNsql entity = entityOptional.get();
		String bucketName = entity.getData().getBucketName();
		Optional<RunDetails>  requestedRun = entity.getData().getRuns().stream().filter(x -> rid.equalsIgnoreCase(x.getId())).findFirst();
		RunDetails run = requestedRun.get();
		visualizationVO.setForecastHorizon(run.getForecastHorizon());
		visualizationVO.setHierarchy(run.getHierarchy());
		visualizationVO.setFrequency(run.getFrequency());
		visualizationVO.setId(run.getId());
		visualizationVO.setRunId(run.getRunId());
		visualizationVO.setRunName(run.getRunName());
		RunState state = run.getRunState();
		if(state!=null) {
			if(!(state.getResult_state()!=null && ("SUCCESS".equalsIgnoreCase(state.getResult_state()) ||"WARNINGS".equalsIgnoreCase(state.getResult_state()) ||"INFO".equalsIgnoreCase(state.getResult_state())))) {
				visualizationVO.setVisualsData("");
//				visualizationVO.setEda("");
//				visualizationVO.setY("");
//				visualizationVO.setYPred("");
				return visualizationVO;
			}
		}
		String commonPrefix = "/results/"+rid + "-" + run.getRunName();
		try {
			String yPrefix = commonPrefix +"/y.csv";
			String yPredPrefix = commonPrefix +"/y_pred.csv";
			String edaJsonPrefix = commonPrefix +"/eda.json";
			String visualsdataJson = commonPrefix +"/visuals/data.json";
//			FileDownloadResponseDto yDownloadResponse = storageClient.getFileContents(bucketName, yPrefix);
//			FileDownloadResponseDto yPredDownloadResponse = storageClient.getFileContents(bucketName, yPredPrefix);
//			FileDownloadResponseDto edaJsonDownloadResponse = storageClient.getFileContents(bucketName, edaJsonPrefix);
			FileDownloadResponseDto visualsDataJsonDownloadResponse = storageClient.getFileContents(bucketName, visualsdataJson);
			JsonArray jsonArray = new JsonArray();
//			String yResult = "";
//			String yPredResult = "";
//			String edaResult = "";
			String visualsDataResult = "";
//			if(yDownloadResponse!= null && yDownloadResponse.getData()!=null && (yDownloadResponse.getErrors()==null || yDownloadResponse.getErrors().isEmpty())) {
//				 yResult = new String(yDownloadResponse.getData().getByteArray()); 
//			 }
//			if(yPredDownloadResponse!= null && yPredDownloadResponse.getData()!=null && (yPredDownloadResponse.getErrors()==null || yPredDownloadResponse.getErrors().isEmpty())) {
//				  yPredResult = new String(yPredDownloadResponse.getData().getByteArray()); 
//			 }
//			if(edaJsonDownloadResponse!= null && edaJsonDownloadResponse.getData()!=null && (edaJsonDownloadResponse.getErrors()==null || edaJsonDownloadResponse.getErrors().isEmpty())) {
//				edaResult = new String(edaJsonDownloadResponse.getData().getByteArray()); 
//			 }
				if(visualsDataJsonDownloadResponse!= null && visualsDataJsonDownloadResponse.getData()!=null && (visualsDataJsonDownloadResponse.getErrors()==null || visualsDataJsonDownloadResponse.getErrors().isEmpty())) {
					visualsDataResult = new String(visualsDataJsonDownloadResponse.getData().getByteArray());
			}
//			visualizationVO.setEda(edaResult);
//			visualizationVO.setY(yResult);
//			visualizationVO.setYPred(yPredResult);
			visualizationVO.setVisualsData(visualsDataResult);
		}catch(Exception e) {
			log.error("Failed while parsing results data for run rid {} with exception {} ",rid, e.getMessage());
		}
		return visualizationVO;
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public GenericMessage generateApiKey(String id) {
		GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);
		if (entityOptional != null) {
			try {
				ForecastNsql entity = entityOptional.get();
			
				String apiKey = UUID.randomUUID().toString();
				if (apiKey != null && id != null) {
					GenericMessage createApiKeyResponseMessage = vaultAuthClient.updateApiKey(id, apiKey);
						if (createApiKeyResponseMessage != null && "FAILED".equalsIgnoreCase(createApiKeyResponseMessage.getSuccess())) {
						throw new Exception("Failed to generate an Api key");
					}
				}
				responseMessage.setSuccess("SUCCESS");
			} catch(Exception e) {
				log.error("Failed to generate an API key for " + id);
				MessageDescription msg = new MessageDescription("Failed to generate an API key for " + id);
				errors.add(msg);
				responseMessage.setSuccess("FAILED");
				responseMessage.setErrors(errors);
				return responseMessage;
			}
		}
		return responseMessage;
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public ApiKeyVO getApiKey(String id) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		ApiKeyVO response = new ApiKeyVO();
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);
		if (entityOptional != null) {
			try {
				ForecastNsql entity = entityOptional.get();

				String apiKey = vaultAuthClient.getApiKeys(id);
				if (apiKey == null) {
					throw new Exception("Failed to get an Api key for " + id);
				}
				response.setApiKey(apiKey);
				responseMessage.setSuccess("SUCCESS");
			} catch(Exception e) {
				log.error("Failed to get an API key for " + id);
				MessageDescription msg = new MessageDescription("Failed to get an API key for " + id);
				errors.add(msg);
				responseMessage.setSuccess("FAILED");
				responseMessage.setErrors(errors);
				return response;
			}
		}
		return response;
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public GenericMessage updateForecastByID(String id, ForecastProjectUpdateRequestVO forecastUpdateRequestVO,
			ForecastVO existingForecast) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);

		if (entityOptional != null) {
			try {
				ForecastNsql entity = entityOptional.get();
				List<UserDetails> exstingcollaborators = entity.getData().getCollaborators();
				LeanGovernanceFeilds governanceFeilds = entity.getData().getLeanGovernanceFeilds();

					LeanGovernanceFeilds updatedValues = assembler.toGovernceEntity(forecastUpdateRequestVO.getLeanGovernanceFeilds());
					entity.getData().setLeanGovernanceFeilds(updatedValues);

				List<UserDetails> addCollabrators = forecastUpdateRequestVO.getAddCollaborators().stream().map(n -> {
					UserDetails collaborator = new UserDetails();
					BeanUtils.copyProperties(n, collaborator);
					return collaborator;
				}).collect(Collectors.toList());

				List<UserDetails> removeCollabrators = forecastUpdateRequestVO.getRemoveCollaborators().stream()
						.map(n -> {
							UserDetails collaborator = new UserDetails();
							BeanUtils.copyProperties(n, collaborator);
							return collaborator;
						}).collect(Collectors.toList());

				if (exstingcollaborators != null) {
					exstingcollaborators.addAll(addCollabrators);
				} else {
					exstingcollaborators = addCollabrators;
				}

				// To remove collaborators from existing collaborators.
				for (UserDetails user : removeCollabrators) {
					UserDetails userToRemove = null;
					for (UserDetails usr : exstingcollaborators) {
						if (usr.getId().equals(user.getId())) {
							userToRemove = usr;
							break;
						}
					}

					if (userToRemove != null) {
						exstingcollaborators.remove(userToRemove);
					} else {
						MessageDescription msg = new MessageDescription("User ID not found for deleting " + user.getId());
						responseMessage.setSuccess("FAILED");
						errors.add(msg);
						responseMessage.setErrors(errors);
						log.error("User ID not found for deleting" + user.getId());
						return responseMessage;
					}
				}

				entity.getData().setCollaborators(exstingcollaborators);
				List<CollaboratorVO> addCollabratorsList = exstingcollaborators.stream().map(n -> {
					CollaboratorVO collaborator = new CollaboratorVO();
					BeanUtils.copyProperties(n, collaborator);
					return collaborator;
				}).collect(Collectors.toList());

				if (entity.getData().getBucketId() != null) {
					UpdateBucketResponseWrapperDto updateBucketResponse = storageClient.updateBucket(entity.getData().getBucketName(), entity.getData().getBucketId(), existingForecast.getCreatedBy(), addCollabratorsList);
					if (updateBucketResponse.getErrors() != null) {
						log.error("Failed while saving details of collaborator {} Caused due to Exception {}", existingForecast.getName(), updateBucketResponse.getErrors().get(0).getMessage());
						MessageDescription msg = new MessageDescription("Failed to save collaborator details.");
						errors.add(msg);
						responseMessage.setSuccess("FAILED");
						responseMessage.setErrors(errors);
						return responseMessage;
					}
				} else {
					GetBucketByNameResponseWrapperDto getBucketBynameResponse = storageClient.getBucketDetailsByName(entity.getData().getBucketName());
					if (getBucketBynameResponse != null && getBucketBynameResponse.getId() != null) {
						// setting bucket Id for the entity.
						entity.getData().setBucketId(getBucketBynameResponse.getId());
						UpdateBucketResponseWrapperDto updateBucketResponse = storageClient.updateBucket(entity.getData().getBucketName(), getBucketBynameResponse.getId(), existingForecast.getCreatedBy(), addCollabratorsList);
						if (updateBucketResponse.getErrors() != null) {
							log.error("Failed while saving details of collaborator {} Caused due to Exception {}", existingForecast.getName(), updateBucketResponse.getErrors().get(0).getMessage());
							MessageDescription msg = new MessageDescription("Failed to save collaborator details.");
							errors.add(msg);
							responseMessage.setSuccess("FAILED");
							responseMessage.setErrors(errors);
							return responseMessage;
						}
					} else {
						if (getBucketBynameResponse.getErrors() != null) {
							log.error("Failed while saving details of collaborator {} Caused due to Exception {}", existingForecast.getName(), getBucketBynameResponse.getErrors().get(0).getMessage());
							MessageDescription msg = new MessageDescription("Failed to save collaborator details.");
							errors.add(msg);
							responseMessage.setSuccess("FAILED");
							responseMessage.setErrors(errors);
							return responseMessage;
						}
					}
				}

				this.jpaRepo.save(entity);
				responseMessage.setSuccess("SUCCESS");
			} catch (Exception e) {
				log.error("Failed while saving details of collaborator " + existingForecast.getName());
				MessageDescription msg = new MessageDescription("Failed to save collaborator details.");
				errors.add(msg);
				responseMessage.setSuccess("FAILED");
				responseMessage.setErrors(errors);
				return responseMessage;
			}

		}

		return responseMessage;
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public GenericMessage deleteForecastByID(String id) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);
		if (entityOptional != null) {
			ForecastNsql entity = entityOptional.get();
			List<RunDetails> existingRuns = entity.getData().getRuns();
			String bucketName = entity.getData().getBucketName();

			// To delete all the runs which are associated to the entity.
			if (existingRuns != null && !existingRuns.isEmpty()) {
				for (RunDetails run : existingRuns) {
						run.setIsDelete(true);
				}
			}

			// To delete bucket in minio storage.
			if (bucketName != null) {
				DeleteBucketResponseWrapperDto response = storageClient.deleteBucketCascade(bucketName);

				if (response != null && "FAILED".equalsIgnoreCase(response.getStatus())) {
					String msg = "Failed to delete Bucket.";
					MessageDescription errMsg = new MessageDescription(msg);
					errors.add(errMsg);
					responseMessage.setSuccess("FAILED");
					responseMessage.setErrors(errors);
					log.error("Failed to delete Bucket Please try again.");
					return responseMessage;
				}
			}

			// To delete an Entity.
			this.jpaRepo.delete(entity);

			responseMessage.setErrors(null);
			responseMessage.setSuccess("SUCCESS");
		}
		return responseMessage;
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public GenericMessage deletRunByUUID(String id, String rid) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> warnings = new ArrayList<>();
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);
		if(entityOptional!=null) {
			ForecastNsql entity = entityOptional.get();
			List<RunDetails> existingRuns = entity.getData().getRuns();
			String bucketName = entity.getData().getBucketName();
			List<RunDetails> updatedRuns = new ArrayList<>();
			for(RunDetails run : existingRuns) {
				if(rid.equalsIgnoreCase(run.getId())) {
					String prefix= "results/" + rid + "-" + run.getRunName();
					DeleteBucketResponseWrapperDto deleteRunResponse = storageClient.deleteFilePresent(bucketName,prefix);
					if(deleteRunResponse==null || (deleteRunResponse!=null && (deleteRunResponse.getErrors()!=null || !"SUCCESS".equalsIgnoreCase(deleteRunResponse.getStatus())))) {
						String msg = "Failed to delete Run folder on Minio." ;
						if(deleteRunResponse.getErrors()!=null) {
							msg+= deleteRunResponse.getErrors();
						}
						MessageDescription warningMsg = new MessageDescription(msg);
						warnings.add(warningMsg);
						responseMessage.setWarnings(warnings);
					}else {
						run.setIsDelete(true);
					}
				}
				updatedRuns.add(run);
			}
			entity.getData().setRuns(updatedRuns);
			jpaRepo.save(entity);
		}
		responseMessage.setSuccess("SUCCESS");
		return responseMessage;
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public CancelRunResponseVO cancelRunById(ForecastVO existingForecast, String correlationid) {
		CancelRunResponseVO cancelRunResponseVO = new CancelRunResponseVO();
		RunVO currentRun= new RunVO();
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		Optional<ForecastNsql> anyEntity = this.jpaRepo.findById(existingForecast.getId());
		ForecastNsql entity = null;
		if(anyEntity.isPresent())
			entity = anyEntity.get();
		List<RunDetails> existingRuns = entity.getData().getRuns();
		List<RunDetails> updatedRuns = new ArrayList<>();
		if (existingRuns != null && !existingRuns.isEmpty()) {
			for (RunDetails run : existingRuns) {
				RunState state = run.getRunState();
				if (correlationid.equalsIgnoreCase(run.getId())) {
					currentRun = this.assembler.toRunVO(run);
					if("PENDING".equalsIgnoreCase(state.getLife_cycle_state()) || "RUNNING".equalsIgnoreCase(state.getLife_cycle_state())){
						DataBricksErrorResponseVO cancelRunResponse = this.dataBricksClient.cancelDatabricksRun(run.getRunId());
						if (cancelRunResponse != null && (cancelRunResponse.getErrorCode() != null || cancelRunResponse.getMessage() != null)) {
							log.error("Failed to cancel run as  databricks reponse is not success for run id {} ", run.getRunId());
							String msg = "Failed to cancel Run.";
							if (cancelRunResponse.getErrorCode() != null) {
								msg += cancelRunResponse.getErrorCode();
							}
							if (cancelRunResponse.getMessage() != null) {
								msg += cancelRunResponse.getMessage();
							}
							MessageDescription errMsg = new MessageDescription(msg);
							errors.add(errMsg);
							responseMessage.setSuccess("FAILED");
							responseMessage.setErrors(errors);

						} else {
							RunState newRunState = new RunState();
							newRunState.setLife_cycle_state("TERMINATED");
							newRunState.setResult_state("CANCELED");
							newRunState.setState_message("Run cancelled.");
							newRunState.setUser_cancelled_or_timedout(true);
							currentRun.setState(this.assembler.toStateVO(newRunState));
							currentRun.setError("Run cancelled.");
							run.setRunState(newRunState);
							run.setError("Run cancelled.");
							responseMessage.setSuccess("SUCCESS");
						}
					}
					else{
						log.error("Failed to cancel run as  Run is not in PENDING or Running state for run  of id {}", correlationid);
						String msg = "Run is not in PENDING or Running state. Failed to cancel Run." ;
						MessageDescription errMsg = new MessageDescription(msg);
						errors.add(errMsg);
						responseMessage.setSuccess("FAILED");
						responseMessage.setErrors(errors);
					}
				}
				updatedRuns.add(run);
			}
			entity.getData().setRuns(updatedRuns);
			try {
				jpaRepo.save(entity);

			} catch (Exception e) {
				log.error("Failed while cancelling run ", existingForecast.getName());
				MessageDescription msg = new MessageDescription("Failed to cancel run ");
				errors.add(msg);
				responseMessage.setErrors(errors);
			}
			cancelRunResponseVO.setData(currentRun);
			cancelRunResponseVO.setResponse(responseMessage);
		}
		return cancelRunResponseVO;
	}


	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Boolean isBucketExists(String bucketName) {
		return storageClient.isBucketExists(bucketName);
	}	

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public List<String> getAllForecastIds() {
		return customRepo.getAllForecastIds();
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public BucketObjectsCollectionWrapperDto getBucketObjects(String path, String bucketType){
		return storageClient.getBucketObjects(path,bucketType) ;

	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public List<BucketObjectDetailsDto> getProjectSpecificObjects(List<InputFileVO> configFiles){
		BucketObjectsCollectionWrapperDto projectSpecificConfigFiles = new BucketObjectsCollectionWrapperDto();

		return this.assembler.toProjectSpecificConfigFiles(configFiles);

	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public ForecastComparisonCreateResponseVO createComparison(String id, ForecastVO existingForecast, List<String> validRunsPath, String comparisionId, String comparisonName,
			String actualsFilePath, String businessFilePath, String targetFolder, Date createdOn, String requestUser) {
		GenericMessage responseMessage = new GenericMessage();
		ForecastComparisonVO forecastComparisonsVO = new ForecastComparisonVO();
		ForecastComparisonCreateResponseVO responseWrapperVO = new ForecastComparisonCreateResponseVO();
		GenericMessage response = new GenericMessage();
		Optional<ForecastNsql> anyEntity = this.jpaRepo.findById(id);
		ComparisonDetails comparisonDetails = new ComparisonDetails();
		ComparisonState comparisonState = new ComparisonState();
		comparisonState.setLifeCycleState("CREATED");
		comparisonState.setStateMessage("Accepted run comparison");

		ComparisonStateVO state= new ComparisonStateVO();
		state.setLifeCycleState(ComparisonStateVO.LifeCycleStateEnum.CREATED);
		state.setStateMessage("Accepted run comparison");
		if(anyEntity!=null && anyEntity.isPresent()) {
			ForecastNsql entity = anyEntity.get();
			Forecast data = entity.getData();
			 List<ComparisonDetails> existingComparisons = data.getComparisons();
			if(existingComparisons==null || existingComparisons.isEmpty())
				existingComparisons = new ArrayList<>();
			 comparisonDetails.setComparisonId(comparisionId);
			 comparisonDetails.setActualsFile(actualsFilePath);
			 comparisonDetails.setBusinessFile(businessFilePath);
			 comparisonDetails.setComparisonName(comparisonName);
			 comparisonDetails.setComparisonState(comparisonState);
			 comparisonDetails.setIsDelete(false);
			 comparisonDetails.setRunsList(validRunsPath);
			 comparisonDetails.setTargetFolder(targetFolder);
			 comparisonDetails.setTriggeredBy(requestUser);
			 comparisonDetails.setTriggeredOn(createdOn);
			 existingComparisons.add(comparisonDetails);

			forecastComparisonsVO.setComparisonId(comparisionId);
			forecastComparisonsVO.setActualsFile(actualsFilePath);
			forecastComparisonsVO.setBusinessFile(businessFilePath);
			forecastComparisonsVO.setComparisonName(comparisonName);
			forecastComparisonsVO.setState(state);
			forecastComparisonsVO.setIsDeleted(false);
			forecastComparisonsVO.setRunsList(validRunsPath);
			forecastComparisonsVO.setTargetFolder(targetFolder);
			forecastComparisonsVO.setTriggeredBy(requestUser);
			forecastComparisonsVO.setTriggeredOn(createdOn);

			 data.setComparisons(existingComparisons);
			 entity.setData(data);

			try {
				jpaRepo.save(entity);
				responseMessage.setSuccess("SUCCESS");
				String message="Comparison " + comparisonName + " triggered by " + requestUser +" for chronos-project "+ data.getName() + " is created successfully.";
				String notificationEventName = "Chronos Forecast Comparison LifeCycleStatus update";
				notifyUsers(entity.getId(), new ArrayList<>(), new ArrayList<>() ,message,comparisionId,notificationEventName, chronosComparisontopicName);
				
			}catch(Exception e) {
				log.error("Failed while saving details of comparison {} to database for project {}, triggered by {}",comparisonName, existingForecast.getName(), requestUser);
				MessageDescription msg = new MessageDescription("Failed to save comparison details to table ");
				List<MessageDescription> errors = new ArrayList<>();
				errors.add(msg);
				responseMessage.setErrors(errors);
			}
			responseWrapperVO.setData(forecastComparisonsVO);
			responseWrapperVO.setResponse(responseMessage);
		}
		return responseWrapperVO;
	}
	
	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void processForecastComparision(String forecastId, String comparisonId) {
		Optional<ForecastNsql> anyEntity = this.jpaRepo.findById(forecastId);
		ComparisonDetails comparisonDetails = new ComparisonDetails();
		if(anyEntity!=null && anyEntity.isPresent()) {
			ForecastNsql entity = anyEntity.get();
			Forecast data = entity.getData();
			 List<ComparisonDetails> existingComparisons = data.getComparisons();
			 List<ComparisonDetails> updatedComparisons = new ArrayList();
			if(existingComparisons!=null && !existingComparisons.isEmpty()) {
				for(ComparisonDetails tempComparison : existingComparisons) {
					if((comparisonId!=null && comparisonId.equalsIgnoreCase(tempComparison.getComparisonId()) && "CREATED".equalsIgnoreCase(tempComparison.getComparisonState().getLifeCycleState())) 
							|| "CREATED".equalsIgnoreCase(tempComparison.getComparisonState().getLifeCycleState())) {
						ChronosComparisonRequestDto comparisonRequestDto = new ChronosComparisonRequestDto();
						comparisonRequestDto.setRuns_list(tempComparison.getRunsList());
						comparisonRequestDto.setActuals_file(tempComparison.getActualsFile());
						comparisonRequestDto.setBusiness_file(tempComparison.getBusinessFile());
						comparisonRequestDto.setTarget_folder(tempComparison.getTargetFolder());
						log.info("calling Chronos Comparison API for comparison {} , triggeredBy {}  ", tempComparison.getComparisonName() ,tempComparison.getTriggeredBy());
						CreateComparisonResponseWrapperDto createComparisonResponse = comparisonClient.createComparison(tempComparison.getComparisonName(),tempComparison.getTriggeredBy(),comparisonRequestDto);
						ComparisonState resultState = createComparisonResponse.getData();
						tempComparison.setComparisonState(resultState);
						List<String> memberIds = new ArrayList<>();
						List<String> memberEmails = new ArrayList<>();
						if (entity.getData().getCollaborators() != null) {
							memberIds = entity.getData().getCollaborators().stream()
									.map(UserDetails::getId).collect(Collectors.toList());
							memberEmails = entity.getData().getCollaborators().stream()
									.map(UserDetails::getEmail).collect(Collectors.toList());
						}
						String ownerId = entity.getData().getCreatedBy().getId();
						memberIds.add(ownerId);
						String ownerEmail = entity.getData().getCreatedBy().getEmail();
						memberEmails.add(ownerEmail);
						String message ="";
						message="Comparison " + tempComparison.getComparisonName() + " triggered by " + tempComparison.getTriggeredBy() +" for chronos-project "+ data.getName() + " completed with ResultState " + resultState.getLifeCycleState() +". Please check forecast-comparisons for more details";
						String notificationEventName = "Chronos Forecast Comparison LifeCycleStatus update";
						notifyUsers(forecastId, memberIds, memberEmails,message,"",notificationEventName,null);
					}
					updatedComparisons.add(tempComparison);
				}
			}
			data.setComparisons(updatedComparisons);
			entity.setData(data);
			log.debug("Aync job saving updated comparisons with new state ");
			this.jpaRepo.save(entity);
		}
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Integer getTotalCountOfForecastProjects() {
		return customRepo.getTotalCountOfForecastProjects();
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Integer getTotalCountOfForecastUsers() {
		return customRepo.getTotalCountOfForecastUsers();
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Object[] getAllForecastComparisons(int limit, int offset, String id,String sortBy,String sortOrder) {
		Object[] getForecastComparisonsArr = new Object[2];
		List<ForecastComparisonVO> forecastComparisonsVOList = new ArrayList<>();
		Optional<ForecastNsql> anyEntity = this.jpaRepo.findById(id);
		Integer totalCount = 0;
		if(anyEntity!=null && anyEntity.isPresent()) {
			ForecastNsql entity = anyEntity.get();
			Forecast data = entity.getData();
			List<ComparisonDetails> existingComparisons = data.getComparisons();
			//logic to remove all deleted comparisons from list
			if(existingComparisons!=null && !existingComparisons.isEmpty()) {
			List<ComparisonDetails> tempExistingComparisons = new ArrayList<>(existingComparisons);
			for(int i=0; i<tempExistingComparisons.size(); i++) {
				ComparisonDetails details= tempExistingComparisons.get(i);
				if(details.getIsDelete() != null) {
					boolean isDelete = details.getIsDelete();
					if(isDelete) {
						tempExistingComparisons.remove(details);
					}
				}
			}
				log.info("sorting comparisons by sortOrder as {} , order by {}", sortBy,sortOrder);
					switch (sortBy) {
						case "createdOn":
							Comparator<ComparisonDetails> comparatorCreatedOn = (v1, v2) -> (v2.getTriggeredOn().compareTo(v1.getTriggeredOn()));
							if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingComparisons, comparatorCreatedOn);
							}
							else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
								Collections.sort(tempExistingComparisons, Collections.reverseOrder(comparatorCreatedOn));
							}
							break;
						case "comparisonName":
							Comparator<ComparisonDetails> comparatorRunName = (v1, v2) -> (v2.getComparisonName().compareTo(v1.getComparisonName()));
							if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingComparisons, comparatorRunName);
							}
							else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
								Collections.sort(tempExistingComparisons, Collections.reverseOrder(comparatorRunName));
							}
							break;
						case "status":
							Comparator<ComparisonDetails> comparatorStatus = (v1, v2) -> (v2.getComparisonState().getLifeCycleState().compareTo(v1.getComparisonState().getLifeCycleState()));
							if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingComparisons, comparatorStatus);
							}
							else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
								Collections.sort(tempExistingComparisons, Collections.reverseOrder(comparatorStatus));
							}
							break;
						case "createdBy":
							Comparator<ComparisonDetails> comparatorCreatedBy = (v1, v2) -> (v2.getTriggeredBy().compareTo(v1.getTriggeredBy()));
							if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingComparisons, comparatorCreatedBy);
							}
							else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
								Collections.sort(tempExistingComparisons, Collections.reverseOrder(comparatorCreatedBy));
							}
							break;
						case "actualsFile":
							Comparator<ComparisonDetails> comparatorActualsFile = (v1, v2) -> (v2.getActualsFile().compareTo(v1.getActualsFile()));
							if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingComparisons, comparatorActualsFile);
							}
							else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
								Collections.sort(tempExistingComparisons, Collections.reverseOrder(comparatorActualsFile));
							}
							break;
						case "businessFile":
							Comparator<ComparisonDetails> comparatorBusinessFile = (v1, v2) -> (v2.getBusinessFile().compareTo(v1.getBusinessFile()));
							if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingComparisons, comparatorBusinessFile);
							}
							else if(sortOrder != null && sortOrder.equalsIgnoreCase("asc")){
								Collections.sort(tempExistingComparisons, Collections.reverseOrder(comparatorBusinessFile));
							}
							break;
							default:
							log.info("Case not found");
							break;
					}
				log.info("comparisons sorted successfully");
			//pagination
			totalCount = tempExistingComparisons.size();
			int endLimit = offset + limit;
			if (endLimit > tempExistingComparisons.size()) {
				endLimit = tempExistingComparisons.size();
			}
			List<ComparisonDetails> newSubList =new ArrayList<>();
			newSubList = tempExistingComparisons.subList(offset, endLimit);
			if (limit == 0)
				newSubList = tempExistingComparisons;
			forecastComparisonsVOList = this.assembler.toComparisonsVO(newSubList);
			}
			getForecastComparisonsArr[0] = forecastComparisonsVOList;
			getForecastComparisonsArr[1] = totalCount;
		}
		return getForecastComparisonsArr;
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public GenericMessage deleteComparison(String id, List<String> validComparisonIds) {
		GenericMessage responseMessage = new GenericMessage();
		try {
			Optional<ForecastNsql> anyEntity = this.jpaRepo.findById(id);
			if(anyEntity!=null && anyEntity.isPresent()) {
				ForecastNsql entity = anyEntity.get();
				Forecast data = entity.getData();
				List<ComparisonDetails> existingComparisons = data.getComparisons();
				List<ComparisonDetails> updatedComparisons = new ArrayList<>();
				List<ComparisonDetails> updatedComparisonRecords = new ArrayList<>();
				ComparisonDetails comparisonDetails = new ComparisonDetails();
				if(existingComparisons!= null && !existingComparisons.isEmpty() && validComparisonIds!= null && !validComparisonIds.isEmpty() ) {
					for(ComparisonDetails tempComparison : existingComparisons) {
						if(validComparisonIds.contains(tempComparison.getComparisonId())){
								tempComparison.setIsDelete(true);
						}
						updatedComparisons.add(tempComparison);
						log.info("Updated comparison Id {} of project {} as deleted TRUE",tempComparison.getComparisonId(),entity.getId()) ;
					}
				}
				entity.getData().setComparisons(updatedComparisons);
				this.jpaRepo.save(entity);
			}
			responseMessage.setSuccess("SUCCESS");
		}catch(Exception e) {
			responseMessage.setSuccess("FAILED");
			List<MessageDescription> errors = new ArrayList<>();
			MessageDescription errMsg = new MessageDescription("Failed to delete given comparison ids with exception " + e.getMessage());
			log.error("Failed to delete given comparison ids with exception ", e.getMessage());
		}
		return responseMessage;
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public ForecastComparisonResultVO getForecastComparisonById(String id, String comparisonId) {
		ForecastComparisonResultVO forecastComparisonsVO = new ForecastComparisonResultVO();
		Optional<ForecastNsql> anyEntity = this.jpaRepo.findById(id);
		if(anyEntity!=null && anyEntity.isPresent()) {
			ForecastNsql entity = anyEntity.get();
			Optional<ComparisonDetails>  requestedComparison = entity.getData().getComparisons().stream().filter(x -> comparisonId.equalsIgnoreCase(x.getComparisonId())).findFirst();
			String bucketName= entity.getData().getBucketName();
			ComparisonDetails comparison = requestedComparison.get();
			try{
			String commonPrefix = "/comparisons/"+comparisonId;
			String comparisonHTML = commonPrefix +"/comparison.html";
			FileDownloadResponseDto comparisonHTMLResponse = storageClient.getFileContents(bucketName, comparisonHTML);
			String comparisonHTMLResult = "";
			if(comparisonHTMLResponse!= null && comparisonHTMLResponse.getData()!=null && (comparisonHTMLResponse.getErrors()==null || comparisonHTMLResponse.getErrors().isEmpty())) {
				comparisonHTMLResult = new String(comparisonHTMLResponse.getData().getByteArray());
			}
				forecastComparisonsVO.setComparisonName(comparison.getComparisonName());
				forecastComparisonsVO.setComparisonData(comparisonHTMLResult);
		}catch(Exception e) {
			log.error("Failed while parsing results data for comparison id {} with exception {} ",comparisonId, e.getMessage());
		}
		}
		return forecastComparisonsVO;
	}


	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public ForecastConfigFileUploadResponseVO uploadConfigFile(ForecastVO existingForecast, String configFileId, String requestUser, Date createdOn, String configFilePath, String configFileName, InputFileVO configToRemove) {
		InputFileVO forecastConfigFileVO = new InputFileVO();
		GenericMessage responseMessage = new GenericMessage();
		ForecastConfigFileUploadResponseVO responseWrapperVO = new ForecastConfigFileUploadResponseVO();

		Optional<ForecastNsql> anyEntity = this.jpaRepo.findById(existingForecast.getId());
		if (anyEntity != null && anyEntity.isPresent()) {
			ForecastNsql entity = anyEntity.get();
			forecastConfigFileVO.setId(configFileId);
			forecastConfigFileVO.setCreatedBy(requestUser);
			forecastConfigFileVO.setCreatedOn(createdOn);
			forecastConfigFileVO.setPath(configFilePath);
			forecastConfigFileVO.setName(configFileName);
			entity.getData().setConfigFiles(this.assembler.toConfigFiles(existingForecast.getConfigFiles()));
			try {

				jpaRepo.save(entity);
				responseMessage.setSuccess("SUCCESS");
				if(configToRemove!=null){
					List<String> memberIds = new ArrayList<>();
					List<String> memberEmails = new ArrayList<>();
					if (entity.getData().getCollaborators() != null) {
						memberIds = entity.getData().getCollaborators().stream()
								.map(UserDetails::getId).collect(Collectors.toList());
						memberEmails = entity.getData().getCollaborators().stream()
								.map(UserDetails::getEmail).collect(Collectors.toList());
					}
					String ownerId = entity.getData().getCreatedBy().getId();
					memberIds.add(ownerId);
					String ownerEmail = entity.getData().getCreatedBy().getEmail();
					memberEmails.add(ownerEmail);
					String message="Config " +configFileName + " uploaded by " + requestUser +" for chronos-project "+ existingForecast.getName() + " overwritten successfully";
					String notificationEventName = "Chronos Upload Config File Overwrite";
					notifyUsers(existingForecast.getId(), memberIds, memberEmails,message,"",notificationEventName,null);
				}

			} catch (Exception e) {
				log.error("Failed while uploading config file forecast project {}", existingForecast.getName());
				MessageDescription msg = new MessageDescription("Failed to save config details to table ");
				List<MessageDescription> errors = new ArrayList<>();
				errors.add(msg);
				responseMessage.setErrors(errors);
			}
			responseWrapperVO.setData(forecastConfigFileVO);
			responseWrapperVO.setResponse(responseMessage);
		}
		return responseWrapperVO;
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Object[] getForecastConfigFiles(String id) {
		Object[] getForecastConfigsFilesArr = new Object[2];
		List<InputFileVO> forecastConfigsFilesVOList = new ArrayList<>();
		Optional<ForecastNsql> anyEntity = this.jpaRepo.findById(id);
		Integer totalCount = 0;
		if (anyEntity != null && anyEntity.isPresent()) {
			ForecastNsql entity = anyEntity.get();
			Forecast data = entity.getData();
			List<File> configFilesList = data.getConfigFiles();
			if (configFilesList != null && !configFilesList.isEmpty()) {
				totalCount=configFilesList.size();
				forecastConfigsFilesVOList	= this.assembler.toConfigFilesVO(configFilesList);
			}
			getForecastConfigsFilesArr[0] = forecastConfigsFilesVOList;
			getForecastConfigsFilesArr[1] = totalCount;
			log.info("Get uploaded forecast config files", getForecastConfigsFilesArr);
		}
		return getForecastConfigsFilesArr;
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public ForecastConfigFileResultVO getForecastConfigFileById(String id, String configFileId) {
		ForecastConfigFileResultVO forecastConfigFileVO = new ForecastConfigFileResultVO();
		Optional<ForecastNsql> anyEntity = this.jpaRepo.findById(id);
		if (anyEntity != null && anyEntity.isPresent()) {
			ForecastNsql entity = anyEntity.get();
			Optional<File> requestedConfigFile = entity.getData().getConfigFiles().stream().filter(x -> configFileId.equalsIgnoreCase(x.getId())).findFirst();
			String bucketName = entity.getData().getBucketName();
			File configFile = requestedConfigFile.get();
			try {
				String commonPrefix = "/configs/" ;
				String configPath = commonPrefix + configFile.getName();
				FileDownloadResponseDto configDataResponse = storageClient.getFileContents(bucketName, configPath);
				String configDataResult = "";
				if (configDataResponse != null && configDataResponse.getData() != null && (configDataResponse.getErrors() == null || configDataResponse.getErrors().isEmpty())) {
					configDataResult = new String(configDataResponse.getData().getByteArray());
				}
				forecastConfigFileVO.setConfigFileName(configFile.getName());
				forecastConfigFileVO.setConfigFileData(configDataResult);
			} catch (Exception e) {
				log.error("Failed while parsing results data for config file id {} with exception {} ", configFileId, e.getMessage());
			}
		}
		return forecastConfigFileVO;
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public ResponseEntity<ByteArrayResource> getRunResultsFile(String id, String correlationid, String file){
		Optional<ForecastNsql> anyEntity = this.jpaRepo.findById(id);

		if (anyEntity != null && anyEntity.isPresent()) {
			ForecastNsql entity = anyEntity.get();
			String bucketName = entity.getData().getBucketName();
			Optional<RunDetails> requestedRun = entity.getData().getRuns().stream().filter(x -> correlationid.equalsIgnoreCase(x.getId())).findFirst();
			RunDetails run = requestedRun.get();
			String prefix = "results/" + correlationid + "-" + run.getRunName();
			String resultFilePrefix = prefix +  "/" + file;
			ResponseEntity<ByteArrayResource> downloadFile = storageClient.getDownloadFile(bucketName, resultFilePrefix);
			return downloadFile;

		}
		return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Object[] getAllRuns(int limit, int offset, String forecastId, String sortBy, String sortOrder) {
		Object[] runCollectionWrapper = new Object[2];

		log.info(" method called from controller/ UI ");

		List<RunDetails> updatedRuns = new ArrayList<>();
		List<RunVO> updatedRunVOList = new ArrayList<>();
		List<RunDetails> newSubList = new ArrayList<>();
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(forecastId);
		long totalCount = 0L;
		if (entityOptional.isPresent()) {
			ForecastNsql entity = entityOptional.get();
			String forecastName = entity.getData().getName();
			if (entity != null && entity.getData() != null &&
					entity.getData().getRuns() != null && !entity.getData().getRuns().isEmpty()) {
				List<RunDetails> existingRuns = entity.getData().getRuns();
				if (existingRuns != null && !existingRuns.isEmpty()) {
					
					// logic to remove all deleted runs from list
					List<RunDetails> tempExistingRuns = new ArrayList<>(existingRuns);
					for (int i = 0; i < existingRuns.size(); i++) {
						RunDetails details = existingRuns.get(i);
						if (details.getIsDelete() != null) {
							boolean isDelete = details.getIsDelete();
							if (isDelete) {
								tempExistingRuns.remove(details);
							}
						}

					}

					log.info("sorting runs by sortOrder as {} , order by {}", sortBy, sortOrder);
					switch (sortBy) {
						case "createdOn":
							Comparator<RunDetails> runCreatedOn = (v1,
									v2) -> (v2.getTriggeredOn().compareTo(v1.getTriggeredOn()));
							if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingRuns, runCreatedOn);
							} else if (sortOrder != null && sortOrder.equalsIgnoreCase("asc")) {
								Collections.sort(tempExistingRuns, Collections.reverseOrder(runCreatedOn));
							}
							break;
						case "runName":
							Comparator<RunDetails> runName = (v1, v2) -> (v2.getRunName().compareTo(v1.getRunName()));
							if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingRuns, runName);
							} else if (sortOrder != null && sortOrder.equalsIgnoreCase("asc")) {
								Collections.sort(tempExistingRuns, Collections.reverseOrder(runName));
							}
							break;
						case "status":
							Comparator<RunDetails> runStatus = (v1, v2) -> (v2.getRunState().getResult_state()
									.compareTo(v1.getRunState().getResult_state()));
							if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingRuns, runStatus);
							} else if (sortOrder != null && sortOrder.equalsIgnoreCase("asc")) {
								Collections.sort(tempExistingRuns, Collections.reverseOrder(runStatus));
							}
							break;
						case "createdBy":
							Comparator<RunDetails> runCreatedBy = (v1,
									v2) -> (v2.getTriggeredBy().compareTo(v1.getTriggeredBy()));
							if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingRuns, runCreatedBy);
							} else if (sortOrder != null && sortOrder.equalsIgnoreCase("asc")) {
								Collections.sort(tempExistingRuns, Collections.reverseOrder(runCreatedBy));
							}
							break;
						case "inputFile":
							Comparator<RunDetails> inputFile = (v1,
									v2) -> (v2.getInputFile().compareTo(v1.getInputFile()));
							if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingRuns, inputFile);
							} else if (sortOrder != null && sortOrder.equalsIgnoreCase("asc")) {
								Collections.sort(tempExistingRuns, Collections.reverseOrder(inputFile));
							}
							break;
						case "forecastHorizon":
							Comparator<RunDetails> forecastHorizon = (v1,
									v2) -> Integer.parseInt(v2.getForecastHorizon())
											- Integer.parseInt(v1.getForecastHorizon());
							if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingRuns, forecastHorizon);
							} else if (sortOrder != null && sortOrder.equalsIgnoreCase("asc")) {
								Collections.sort(tempExistingRuns, Collections.reverseOrder(forecastHorizon));
							}
							break;
						case "exogenData":
							Comparator<RunDetails> exogenData = (v1,
									v2) -> (v2.getExogenData().compareTo(v1.getExogenData()));
							if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
								Collections.sort(tempExistingRuns, exogenData);
							} else if (sortOrder != null && sortOrder.equalsIgnoreCase("asc")) {
								Collections.sort(tempExistingRuns, Collections.reverseOrder(exogenData));
							}
							break;
						default:
							log.info("Case not found");
							break;
					}
					log.info("runs sorted successfully");
					int endLimit = offset + limit;
					if (endLimit > tempExistingRuns.size()) {
						endLimit = tempExistingRuns.size();
					}
					newSubList = tempExistingRuns.subList(offset, endLimit);
					if (limit == 0){
						newSubList = tempExistingRuns.subList(offset,tempExistingRuns.size());
					}
					totalCount = tempExistingRuns.size();
					updatedRunVOList = this.assembler.toRunsVO(newSubList);
				}
			}
		}
		runCollectionWrapper[0] = updatedRunVOList;
		runCollectionWrapper[1] = totalCount;
		return runCollectionWrapper;
	}

	@Override
	public BannerResponseVO getBannerText(){
		BannerResponseVO bannerResponseVO = new BannerResponseVO();

		try{
			BucketObjectsCollectionWrapperDto bucketObjectsCollectionWrapperDto = storageClient.getBucketObjectDetails("chronos-core/objects");

			if(bucketObjectsCollectionWrapperDto!=null){
				List<BucketObjectDetailsDto> bucketObjects = bucketObjectsCollectionWrapperDto.getData().getBucketObjects();
				for(BucketObjectDetailsDto object :bucketObjects){
					if("bannertext.txt".equalsIgnoreCase(object.getObjectName())){
						bannerResponseVO.setLastchangedtime(object.getLastModified());
						try{
						FileDownloadResponseDto fileDownloadResponseDto = storageClient.getFileContents("chronos-core", "bannertext.txt");
						ByteArrayResource byteArrayResource = fileDownloadResponseDto.getData();
						String bannerText = new String(byteArrayResource.getByteArray());
						bannerResponseVO.setBannerText(bannerText);
						break;
						}catch(Exception e){
							log.error("Exception while downloading bannertext: {}",e.getMessage());
						}
					}
				}

			}
		}catch(Exception e){
			log.error("Exception while getting bucketobjects: {}",e.getMessage());
		}
		return bannerResponseVO;
	}
}
