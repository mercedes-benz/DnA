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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.dto.AVScanResponseWrapperVO;
import com.daimler.data.dto.FileScanDetailsVO;

@Component
public class AVScannerClient {

	private static Logger logger = LoggerFactory.getLogger(AVScannerClient.class);

	@Value("${dna.malwareScan.uri}")
	private String malwareScannerBaseUri;

	@Value("${dna.malwareScan.basicAuth.token}")
	private String malwareScannerBasicAuthToken;

	@Autowired
	RestTemplate restTemplate;

	/**
	 * To scan file for malware
	 * 
	 * @param multiPartFile
	 * @return
	 */
	public Optional<FileScanDetailsVO> scan(MultipartFile file) {
		FileScanDetailsVO fileScanDetailsVO = new FileScanDetailsVO();
		try {
			// setting uri for malware scanner service
			String malwareScannerUri = malwareScannerBaseUri + "/scan/upload";
			LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			map.add("files", file.getResource());

			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			// Setting malware scanner basic auth token
			headers.setBasicAuth(malwareScannerBasicAuthToken);
			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

			logger.info("Scanning file:{} with avscan.", file.getName());
			ResponseEntity<AVScanResponseWrapperVO> response = restTemplate.exchange(malwareScannerUri, HttpMethod.POST,
					requestEntity, AVScanResponseWrapperVO.class);
			if (response.hasBody()) {
				logger.debug("Success from avscan for file {}", file.getName());
				AVScanResponseWrapperVO aVScanResponseWrapperVO = response.getBody();
				fileScanDetailsVO = aVScanResponseWrapperVO != null ? aVScanResponseWrapperVO.getFileDetails().get(0)
						: null;
			}
		} catch (HttpClientErrorException e) {
			logger.error("HttpClientErrorException occured:{} while scanning file {}", e.getMessage(), file.getName());
			fileScanDetailsVO.setErrorMessage(this.getErrorMsg(e.getMessage()));
		} catch (Exception e) {
			logger.error("Error occured while calling malware scanner service:{} while scanning file {} ",
					e.getMessage(), file.getName());
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
}
