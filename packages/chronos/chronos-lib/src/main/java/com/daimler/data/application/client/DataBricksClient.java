package com.daimler.data.application.client;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataBricksClient {

	//@Value("${databricks.uri.base}")
	private String dataBricksBaseUri;
	
	//@Value("${databricks.uri.runnow}")
	private String dataBricksJobTriggerRunNowPath;
	
	//@Value("${databricks.uri.deleterun}")
	private String dataBricksJobDeleteRunPath;
	
	//@Value("${databricks.uri.cancelrun}")
	private String dataBricksJobCancelRunPath;
	
	//@Value("${databricks.uri.getrun}")
	private String dataBricksJobGetRunPath;
	
	//@Value("${databricks.jobId}")
	private String dataBricksJobId;
	
	//@Value("${databricks.defaultConfigYml}")
	private String dataBricksJobDefaultConfigYml;
	
	@Autowired
	HttpServletRequest httpRequest;
	
	@Autowired
	private RestTemplate restClient;
	
	

}
