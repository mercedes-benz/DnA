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

package com.daimler.data.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.daimler.data.api.logo.LogoApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.logo.LogoDetailsVO;
import com.daimler.data.dto.logo.LogoVO;
import com.daimler.data.dto.logo.UploadResponseVO;
import com.daimler.data.service.attachment.AttachmentService;
import com.daimler.data.util.ConstantsUtility;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Logo API", tags = {"logo"})
@RequestMapping("/api")
@Slf4j
public class LogoController implements LogoApi{

	@Autowired
    private AttachmentService attachmentService;
	@RequestMapping(value = "/logo",
	    produces = {"application/json"},
	    consumes = {"application/json"},
	    method = RequestMethod.POST)
    public ResponseEntity<UploadResponseVO> uploadLogo(LogoVO logoVO)
    {	
		UploadResponseVO responseVO = new UploadResponseVO();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			LogoDetailsVO fileDetails = this.attachmentService.uploadLogoToS3Bucket(logoVO);
			responseVO.setLogoDetails(fileDetails);
		}catch(Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error. "+ e.getMessage());
			errors.add(exceptionMsg);
			responseVO.setErrors(errors);
			return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(responseVO,HttpStatus.CREATED);
    }

	@RequestMapping(value = "/logo/{id}",
		    produces = {"application/json"},
		    method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> deleteLogo(@PathVariable("id") String keyName){
		GenericMessage returnMessage = new GenericMessage();
		String path = ConstantsUtility.S3_PATH_TO_UPLOAD_LOGO + keyName;
		try {
			this.attachmentService.deleteFileFromS3Bucket(path);
			returnMessage.setSuccess("success");
			return new ResponseEntity<>(returnMessage,HttpStatus.OK);
		}catch(Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error. "+ e.getMessage());
			returnMessage.addErrors(exceptionMsg);
			returnMessage.setSuccess("Failed");
			return new ResponseEntity<>(returnMessage,HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
	
	@RequestMapping(value = "/logo/{id}",
		    method = RequestMethod.GET)
    public ResponseEntity<LogoVO> getLogo(@PathVariable("id") String id){
			try {
			String path = ConstantsUtility.S3_PATH_TO_UPLOAD_LOGO + id;
			 this.attachmentService.getAvailableFilesFromBucket();
			byte[] content = this.attachmentService.getFile(path);
			LogoVO logoVO = new LogoVO();
				logoVO.setLogo(content);
				logoVO.setFileSize(this.attachmentService.formatedSize(content.length));
			return new ResponseEntity<>(logoVO,HttpStatus.OK);
			}catch(Exception e) {
				return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
			}
    }
}
