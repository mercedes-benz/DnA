package com.daimler.data.service.forecast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.application.client.DataBricksClient;
import com.daimler.data.application.client.StorageServicesClient;
import com.daimler.data.assembler.ForecastAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.db.json.File;
import com.daimler.data.db.json.RunDetails;
import com.daimler.data.db.json.RunState;
import com.daimler.data.db.repo.forecast.ForecastCustomRepository;
import com.daimler.data.db.repo.forecast.ForecastRepository;
import com.daimler.data.dto.databricks.RunNowNotebookParamsDto;
import com.daimler.data.dto.forecast.DataBricksErrorResponseVO;
import com.daimler.data.dto.forecast.ForecastRunResponseVO;
import com.daimler.data.dto.forecast.ForecastVO;
import com.daimler.data.dto.forecast.InputFileVO;
import com.daimler.data.dto.forecast.RunDetailsVO;
import com.daimler.data.dto.forecast.RunNowResponseVO;
import com.daimler.data.dto.forecast.RunStateVO;
import com.daimler.data.dto.forecast.RunVO;
import com.daimler.data.dto.storage.CreateBucketResponseWrapperDto;
import com.daimler.data.dto.storage.FileUploadResponseDto;
import com.daimler.data.service.common.BaseCommonService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseForecastService extends BaseCommonService<ForecastVO, ForecastNsql, String> implements ForecastService{

	@Value("${databricks.jobId}")
	private String dataBricksJobId;
	
	@Value("${databricks.defaultConfigYml}")
	private String dataBricksJobDefaultConfigYml;
	
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

	public BaseForecastService() {
		super();
	}
	
	@Override
	public List<InputFileVO> getSavedFiles(String id) {
		List<File> files = customRepo.getSavedFiles(id);
		return assembler.toFilesVO(files);
	}

	@Override
	public List<ForecastVO> getAll( int limit,  int offset, String user) {
		List<ForecastNsql> entities = customRepo.getAll(user, offset, limit);
		if (entities != null && !entities.isEmpty())
			return entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public Long getCount(String user) {
		return customRepo.getTotalCount(user);
	}

	@Override
	@Transactional
	public ForecastVO createForecast(ForecastVO vo) throws Exception {
		CreateBucketResponseWrapperDto bucketCreationResponse = storageClient.createBucket(vo.getBucketName(),vo.getCreatedBy(),vo.getCollaborators());
		if(bucketCreationResponse!= null && "SUCCESS".equalsIgnoreCase(bucketCreationResponse.getStatus())) {
			return super.create(vo);
		}else {
			throw new Exception("Failed while creating bucket for Forecast project artifacts to be stored.");
		}
	}
	
	@Override
	@Transactional
	public ForecastRunResponseVO createJobRun(String savedInputPath, Boolean saveRequestPart, String runName,
			String configurationFile, String frequency, BigDecimal forecastHorizon, String comment,
			ForecastVO existingForecast,String triggeredBy, Date triggeredOn) {
		this.jpaRepo.findAll();
		ForecastRunResponseVO responseWrapper = new ForecastRunResponseVO();
		RunNowResponseVO runNowResponseVO = new RunNowResponseVO();
		GenericMessage responseMessage = new GenericMessage();
		RunNowNotebookParamsDto noteboookParams = new RunNowNotebookParamsDto();
		String correlationId = UUID.randomUUID().toString();
		String bucketName = existingForecast.getBucketName();
		noteboookParams.setConfig(dataBricksJobDefaultConfigYml);
		noteboookParams.setCorrelationId(correlationId);
		noteboookParams.setExcel("");
		noteboookParams.setFh(forecastHorizon.toString());
		noteboookParams.setFreq(this.toFrequencyParam(frequency));
		noteboookParams.setResults_folder(bucketName+"/results/"+correlationId + "-" + runName);
		noteboookParams.setX("");
		noteboookParams.setX_pred("");
		noteboookParams.setY(savedInputPath);
		RunNowResponseVO runNowResponse = dataBricksClient.runNow(correlationId, noteboookParams);
		if(runNowResponse!=null) {
			if(runNowResponse.getErrorCode()!=null || runNowResponse.getRunId()==null) 
				responseMessage.setSuccess("FAILED");
			else {
//			)
				responseMessage.setSuccess("SUCCESS");
				ForecastNsql entity = this.assembler.toEntity(existingForecast);
				List<RunDetails> existingRuns = entity.getData().getRuns();
				if(existingRuns==null || existingRuns.isEmpty())
					existingRuns = new ArrayList<>();
				RunDetails currentRun = new RunDetails();
				currentRun.setComment(comment);
				currentRun.setConfigurationFile(dataBricksJobDefaultConfigYml);
				currentRun.setForecastHorizon(forecastHorizon.toString());
				currentRun.setFrequency(frequency);
				currentRun.setId(correlationId);
				currentRun.setInputFile(savedInputPath);
				currentRun.setIsDelete(false);
				currentRun.setJobId(dataBricksJobId);
				currentRun.setNumberInJob(runNowResponse.getNumberInJob());
				currentRun.setRunId(runNowResponse.getRunId());
				currentRun.setRunName(runName);
				currentRun.setTriggeredBy(triggeredBy);
				currentRun.setTriggeredOn(triggeredOn);
				currentRun.setIsDelete(false);
				RunState newRunState = new RunState();
				newRunState.setLife_cycle_state("PENDING");
				newRunState.setUser_cancelled_or_timedout(false);
				currentRun.setRunState(newRunState);
				existingRuns.add(currentRun);
				entity.getData().setRuns(existingRuns);
				try {
					this.jpaRepo.save(entity);
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
		default: return "";
		}
	}

	@Override
	public FileUploadResponseDto saveFile(MultipartFile file, String bucketName) {
		FileUploadResponseDto uploadResponse = storageClient.uploadFile(file,bucketName);
		return uploadResponse;
	}

	@Override
	public Long getRunsCount(String id) {
		return customRepo.getTotalRunsCount(id);
	}

	@Override
	@Transactional
	public List<RunVO> getAllRunsForProject(int limit, int offset, ForecastVO existingForecast) {
		
		List<RunDetails> updatedRuns = new ArrayList<>();
		List<RunVO> updatedRunVOList = new ArrayList<>();
		
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(existingForecast.getId());
		if(entityOptional!=null) {
			ForecastNsql entity = entityOptional.get();
			if(entity!=null && entity.getData()!=null && 
					entity.getData().getRuns()!=null && !entity.getData().getRuns().isEmpty()) {
				List<RunDetails> existingRuns = entity.getData().getRuns();
				for(RunDetails run: existingRuns) {
					RunState state = run.getRunState();
					String runId = run.getRunId();
					if(runId!=null && (run.getIsDelete() == null || !run.getIsDelete()) && 
							(state==null || state.getResult_state()==null || state.getLife_cycle_state()==null ||
							"PENDING".equalsIgnoreCase(state.getLife_cycle_state()) || 
							"RUNNING".equalsIgnoreCase(state.getLife_cycle_state()))) {
						RunDetailsVO updatedRunResponse = this.dataBricksClient.getSingleRun(runId);
						if(updatedRunResponse!=null && runId.equals(updatedRunResponse.getRunId())) {
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
								if(updatedState.getResultState()!=null)
									newState.setResult_state(updatedState.getResultState().name());
								String updatedStateMsg = "";
								if(updatedRunResponse.getState().getStateMessage()!=null) {
									updatedStateMsg = updatedRunResponse.getState().getStateMessage() + ". " + updatedState.getStateMessage();
								}
								newState.setState_message(updatedStateMsg);
								newState.setUser_cancelled_or_timedout(updatedState.isUserCancelledOrTimedout());
								updatedRunDetail.setRunState(newState);
							}
							updatedRuns.add(updatedRunDetail);
						}else {
							updatedRuns.add(run);
						}
					}else {
						updatedRuns.add(run);
					}
						
				}
				entity.getData().setRuns(updatedRuns);
				this.jpaRepo.save(entity);
				updatedRunVOList = this.assembler.toRunsVO(updatedRuns);
			}
					
		}
		return updatedRunVOList;
	}

	@Override
	public GenericMessage deletRunByUUID(String id, String rid) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);
		if(entityOptional!=null) {
			ForecastNsql entity = entityOptional.get();
			List<RunDetails> existingRuns = entity.getData().getRuns();
			List<RunDetails> updatedRuns = new ArrayList<>();
			for(RunDetails run : existingRuns) {
				if(rid.equalsIgnoreCase(run.getId())) {
					DataBricksErrorResponseVO errResponse = this.dataBricksClient.deleteRun(run.getRunId());
					if(errResponse!=null && (errResponse.getErrorCode()!=null || errResponse.getMessage()!=null)) {
						String msg = "Failed to delete Run." ;
						if(errResponse.getErrorCode()!=null) {
							msg+= errResponse.getErrorCode();
						}
						if(errResponse.getMessage()!=null) {
							msg+= errResponse.getMessage();
						}
						MessageDescription errMsg = new MessageDescription(msg);
						errors.add(errMsg);
						responseMessage.setSuccess("FAILED");
						responseMessage.setErrors(errors);
						return responseMessage;
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

	
	
	
}
