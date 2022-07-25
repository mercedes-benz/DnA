/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.application.config;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.dto.AVScanResponseWrapperVO;
import com.daimler.data.dto.FileScanDetailsVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Component
public class AVScannerClient {

	private static Logger LOGGER = LoggerFactory.getLogger(AVScannerClient.class);

	@Value("${avscan.uri}")
	private String avscanBaseUri;

	@Value("${avscan.appid}")
	private String appId;

	@Value("${avscan.apiKey}")
	private String apiKey;

	@Value("${avscan.subscriptionProvisionApi}")
	private String subscriptionProvisionApi;

	@Value("${avscan.scanApi}")
	private String scanApi;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	ServletRequest servletRequest;

	@Autowired
	HttpServletRequest httpRequest;

	/**
	 * To scan file for malware
	 * 
	 * @param multiPartFile
	 * @return
	 */
	public Optional<FileScanDetailsVO> scan(MultipartFile file) {
		FileScanDetailsVO fileScanDetailsVO = new FileScanDetailsVO();
		try {
			String avscanUri = avscanBaseUri + scanApi;
			LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

			if (!file.isEmpty()) {
				map.add("files", file.getResource());
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			headers.set("appId", appId);
			headers.set("apiKey", apiKey);

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

			ResponseEntity<AVScanResponseWrapperVO> response = restTemplate.exchange(avscanUri, HttpMethod.POST,
					requestEntity, AVScanResponseWrapperVO.class);
			if (response != null && response.hasBody()) {
				LOGGER.debug("Success from avscan for file {}", file.getName());
				if (!ObjectUtils.isEmpty(response.getBody().getFileDetails())) {
					fileScanDetailsVO = response.getBody().getFileDetails().get(0);
				}
			}
		} catch (HttpClientErrorException e) {
			LOGGER.error("HttpClientErrorException occured:{} while scanning file {}", e.getMessage(), file.getName());
			fileScanDetailsVO.setErrorMessage(this.getErrorMsg(e.getMessage()));
		} catch (Exception e) {
			LOGGER.error("Error occured while calling avscan service:{} while scanning file {} ", e.getMessage(),
					file.getName());
			fileScanDetailsVO.setErrorMessage(this.getErrorMsg(e.getMessage()));
		}
		return Optional.ofNullable(fileScanDetailsVO);
	}

	/**
	 * To extract error from exception msg
	 * 
	 * @param error
	 * @return result
	 */
	private String getErrorMsg(String error) {
		String result = null;
		Pattern pattern = Pattern.compile("message\":\"(.*?)\"}");
		Matcher matcher = pattern.matcher(error);
		if (matcher.find()) {
			result = matcher.group(1);
		}
		return result;
	}

	public String updateSolIdForSubscribedAppId(String currDnaSubscriptionAppId, String prevDnaSubscriptionAppId,
			String solutionId) {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		JSONObject finalObject = null;
		JSONObject obj = new JSONObject();
		obj.put("currDnaSubscriptionAppId", currDnaSubscriptionAppId);
		obj.put("prevDnaSubscriptionAppId", prevDnaSubscriptionAppId);
		obj.put("solutionId", solutionId);
		finalObject = new JSONObject();
		finalObject.put("data", obj);
		String status = "";
		String jwt = httpRequest.getHeader("Authorization");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		headers.set("Authorization", jwt);
		HttpEntity entity = new HttpEntity<>(finalObject.toString(), headers);
		ResponseEntity<String> response = restTemplate.exchange(avscanBaseUri + subscriptionProvisionApi,
				HttpMethod.PUT, entity, String.class);
		if (response != null && response.hasBody()) {
			status = response.getStatusCode().toString();
		}
		return status;
	}
}
