package com.daimler.data.application.client;

import javax.servlet.http.HttpServletRequest;

import com.daimler.data.dto.databricks.DataBricksJobRunOutputResponseWrapperDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.databricks.DatabricksJobRunNowRequestDto;
import com.daimler.data.dto.databricks.DatabricksRunGenericRequestDto;
import com.daimler.data.dto.databricks.RunNowNotebookParamsDto;
import com.daimler.data.dto.forecast.DataBricksErrorResponseVO;
import com.daimler.data.dto.forecast.JobRunsListVO;
import com.daimler.data.dto.forecast.RunDetailsVO;
import com.daimler.data.dto.forecast.RunNowResponseVO;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataBricksClient {

	@Value("${databricks.uri.base}")
	private String dataBricksBaseUri;

	@Value("${databricks.uri.runnow}")
	private String dataBricksJobTriggerRunNowPath;

	@Value("${databricks.uri.deleterun}")
	private String dataBricksJobDeleteRunPath;

	@Value("${databricks.uri.getrun}")
	private String dataBricksJobGetRunPath;
	@Value("${databricks.uri.cancelrun}")
	private String dataBricksJobCancelRunPath;

	@Value("${databricks.uri.jobrunlist}")
	private String dataBricksJobRunList;

	@Value("${databricks.uri.jobrunoutput}")
	private String dataBricksJobRunOutputUri;

	@Value("${databricks.jobId}")
	private String dataBricksJobId;

	@Value("${databricks.powerfulMachinesJobId}")
	private String dataBricksPowerfulMachinesJobId;

	@Value("${databricks.defaultConfigYml}")
	private String dataBricksJobDefaultConfigYml;

	@Value("${databricks.pat}")
	private String dataBricksPAT;

	@Autowired
	HttpServletRequest httpRequest;

	@Autowired
	private RestTemplate proxyRestTemplate;


	public RunNowResponseVO runNow(String runCorrelationUUID, RunNowNotebookParamsDto notebookParams, boolean runOnPowerfulMachines) {
		RunNowResponseVO runNowResponse = null;
		try {
			String dataBricksJobidForRun = dataBricksJobId;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+dataBricksPAT);
			headers.setContentType(MediaType.APPLICATION_JSON);

			String runNowUrl = dataBricksBaseUri + dataBricksJobTriggerRunNowPath;
			DatabricksJobRunNowRequestDto requestWrapper = new DatabricksJobRunNowRequestDto();
			if(notebookParams.getConfig()==null || "".equalsIgnoreCase(notebookParams.getConfig()))
				notebookParams.setConfig(dataBricksJobDefaultConfigYml);
			if(runOnPowerfulMachines) {
				dataBricksJobidForRun = dataBricksPowerfulMachinesJobId;
			}
			requestWrapper.setJob_id(dataBricksJobidForRun);
			requestWrapper.setNotebook_params(notebookParams);
			HttpEntity<DatabricksJobRunNowRequestDto> requestEntity = new HttpEntity<>(requestWrapper,headers);
			ResponseEntity<RunNowResponseVO> response = proxyRestTemplate.exchange(runNowUrl, HttpMethod.POST,
					requestEntity, RunNowResponseVO.class);
			if (response.hasBody()) {
				runNowResponse = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to invoke databricks run {} with {} ", runCorrelationUUID,e.getMessage());
		}
		return runNowResponse;
	}


	public DataBricksErrorResponseVO deleteRun(String runId) {
		DataBricksErrorResponseVO deleteRunResponse = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+dataBricksPAT);
			headers.setContentType(MediaType.APPLICATION_JSON);

			String deleteRunUrl = dataBricksBaseUri + dataBricksJobDeleteRunPath;
			DatabricksRunGenericRequestDto requestWrapper = new DatabricksRunGenericRequestDto();
			requestWrapper.setRun_id(runId);
			HttpEntity<DatabricksRunGenericRequestDto> requestEntity = new HttpEntity<>(requestWrapper,headers);
			ResponseEntity<DataBricksErrorResponseVO> response = proxyRestTemplate.exchange(deleteRunUrl, HttpMethod.POST,
					requestEntity, DataBricksErrorResponseVO.class);
			if (response.hasBody()) {
				deleteRunResponse = response.getBody();
			}
		}catch(Exception e) {
			deleteRunResponse = new DataBricksErrorResponseVO();
			deleteRunResponse.setMessage(e.getMessage());
		}
		return deleteRunResponse;
	}

	public DataBricksJobRunOutputResponseWrapperDto getSingleRunOutput(String runId) {
		DataBricksJobRunOutputResponseWrapperDto getSingleRunOutputResponse = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+dataBricksPAT);
			headers.setContentType(MediaType.APPLICATION_JSON);

			String getSingleOutputRunUrl = dataBricksBaseUri + dataBricksJobRunOutputUri + "?run_id=" + runId;
			log.info("getSingleOutputRunUrl" + getSingleOutputRunUrl);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<DataBricksJobRunOutputResponseWrapperDto> response = proxyRestTemplate.exchange(getSingleOutputRunUrl, HttpMethod.GET,
					requestEntity, DataBricksJobRunOutputResponseWrapperDto.class);
			if (response.hasBody()) {
				getSingleRunOutputResponse = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to invoke databricks get run output {} with {} ", runId,e.getMessage());
		}
		return getSingleRunOutputResponse;
	}

	public RunDetailsVO getSingleRun(String runId) {
		RunDetailsVO getSingleRunResponse = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+dataBricksPAT);
			headers.setContentType(MediaType.APPLICATION_JSON);

			String getSingleRunUrl = dataBricksBaseUri + dataBricksJobGetRunPath + "?run_id=" + runId;
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<RunDetailsVO> response = proxyRestTemplate.exchange(getSingleRunUrl, HttpMethod.GET,
					requestEntity, RunDetailsVO.class);

			if (response.hasBody()) {
				getSingleRunResponse = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to invoke databricks get run  {} with {} ", runId,e.getMessage());
		}
		return getSingleRunResponse;
	}


	public JobRunsListVO getJobRuns() {
		JobRunsListVO getJobRunsResponse = null;
		try {
			String dataBricksJobidForRun = dataBricksJobId;
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+dataBricksPAT);
			headers.setContentType(MediaType.APPLICATION_JSON);
			String getJobRunsUrl = dataBricksBaseUri + dataBricksJobRunList + "?active_only=true&expand_tasks=false&run_type=JOB_RUN&job_id="+dataBricksJobidForRun;
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<JobRunsListVO> response = proxyRestTemplate.exchange(getJobRunsUrl, HttpMethod.POST,
					requestEntity, JobRunsListVO.class);
			if (response.hasBody()) {
				getJobRunsResponse = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to invoke databricks get run {}", e.getMessage());
		}
		return getJobRunsResponse;
	}

	public DataBricksErrorResponseVO cancelDatabricksRun(String runId) {
		DataBricksErrorResponseVO deleteRunResponse = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+dataBricksPAT);
			headers.setContentType(MediaType.APPLICATION_JSON);
			String cancelJobRunUrl = dataBricksBaseUri + dataBricksJobCancelRunPath;
			DatabricksRunGenericRequestDto requestWrapper = new DatabricksRunGenericRequestDto();
			requestWrapper.setRun_id(runId);
			HttpEntity requestEntity = new HttpEntity<>(requestWrapper,headers);
			ResponseEntity<DataBricksErrorResponseVO> response = proxyRestTemplate.exchange(cancelJobRunUrl, HttpMethod.POST,
					requestEntity, DataBricksErrorResponseVO.class);
			if (response.hasBody()) {
				deleteRunResponse = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to invoke databricks get run {}", e.getMessage());
		}
		return deleteRunResponse;
	}
}






