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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.api.attachment.AttachmentsApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.attachment.FileDetailsCollectionVO;
import com.daimler.data.dto.attachment.FileDetailsVO;
import com.daimler.data.dto.attachment.UploadResponseVO;
import com.daimler.data.service.attachment.AttachmentService;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Attachments API", tags = { "attachments" })
@RequestMapping("/api")
@Slf4j
public class AttachmentController implements AttachmentsApi {

	private static Logger LOGGER = LoggerFactory.getLogger(AttachmentController.class);

	@Autowired
	private AttachmentService attachmentService;

	@RequestMapping(value = "/attachments", produces = { "application/json" }, consumes = {
			"multipart/form-data" }, method = RequestMethod.POST)
	public ResponseEntity<UploadResponseVO> uploadFile(MultipartFile file) {
		UploadResponseVO responseVO = new UploadResponseVO();
		List<MessageDescription> errors = new ArrayList<>();
		HttpStatus httpStatus;
		try {
			FileDetailsVO fileDetails = this.attachmentService.uploadFileToS3Bucket(file, null);
			responseVO.setFileDetails(fileDetails);
			if (!ObjectUtils.isEmpty(fileDetails.getErrors())) {
				responseVO.setErrors(fileDetails.getErrors());
				fileDetails.setErrors(null);
				httpStatus = HttpStatus.CONFLICT;
			} else {
				httpStatus = HttpStatus.CREATED;
			}
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription(
					"Failed to delete due to internal error. " + e.getMessage());
			errors.add(exceptionMsg);
			responseVO.setErrors(errors);
			log.error("Failed to uploadfile {} to bucket",file.getName());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info("File {} uploaded to bucket successfully",file.getName());
		return new ResponseEntity<>(responseVO, httpStatus);
	}

	@RequestMapping(value = "/attachments", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<FileDetailsCollectionVO> getAllFileDetails() {
		FileDetailsCollectionVO collection = new FileDetailsCollectionVO();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			List<FileDetailsVO> files = this.attachmentService.getAvailableFilesFromBucket();
			collection.setFiles(files);
			log.debug("Returning available files from bucket");
			return new ResponseEntity<>(collection, HttpStatus.OK);
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription(
					"Failed to delete due to internal error. " + e.getMessage());
			errors.add(exceptionMsg);
			collection.setFiles(null);
			collection.setErrors(errors);
			log.error("Exception {} while fetching files from bucket", e.getMessage());
			return new ResponseEntity<>(collection, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/attachments/{id}", produces = { "application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> deleteFile(@PathVariable("id") String keyName) {
		GenericMessage returnMessage = new GenericMessage();
		try {
			this.attachmentService.deleteFileFromS3Bucket(keyName);
			returnMessage.setSuccess("success");
			log.info("File with keyName {} deleted from bucket successfully",keyName);
			return new ResponseEntity<>(returnMessage, HttpStatus.OK);
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription(
					"Failed to delete due to internal error. " + e.getMessage());
			returnMessage.addErrors(exceptionMsg);
			returnMessage.setSuccess("Failed");
			log.error("Failed to delete file from bucket with exception {}", e.getMessage());
			return new ResponseEntity<>(returnMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/attachments/{id}", method = RequestMethod.GET)
	public ResponseEntity<ByteArrayResource> getFile(@PathVariable("id") String nameDetails) {
		try {
			String[] details = nameDetails.split("~");
			String keyName = details[0];
			String fileName = details[1];
			byte[] content = this.attachmentService.getFile(keyName);
			ByteArrayResource resource = new ByteArrayResource(content);
			log.debug("Returning file {} ", nameDetails);
			return ResponseEntity.ok().contentLength(content.length).contentType(contentType(fileName))
					.header("Content-disposition", "attachment; filename=\"" + fileName + "\"").body(resource);
		} catch (Exception e) {
			log.error("Failed with exception {} while returning file {}", e.getMessage(), nameDetails);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	private MediaType contentType(String fileName) {
		String[] arr = fileName.split("\\.");
		String type = arr[arr.length - 1];
		switch (type) {
		case "txt":
			return MediaType.TEXT_PLAIN;
		case "png":
			return MediaType.IMAGE_PNG;
		case "jpg":
			return MediaType.IMAGE_JPEG;
		default:
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

}
