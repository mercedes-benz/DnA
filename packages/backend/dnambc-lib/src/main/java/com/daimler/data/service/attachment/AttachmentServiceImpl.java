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

package com.daimler.data.service.attachment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.IOUtils;
import com.daimler.data.application.config.AVScannerClient;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.FileScanDetailsVO;
import com.daimler.data.dto.attachment.FileDetailsVO;
import com.daimler.data.dto.logo.LogoDetailsVO;
import com.daimler.data.dto.logo.LogoVO;
import com.daimler.data.util.ConstantsUtility;

@Service
public class AttachmentServiceImpl implements AttachmentService {

	private Logger log = LoggerFactory.getLogger(AttachmentServiceImpl.class);

	private static final List<String> contentTypes = Arrays.asList("bmp", "gif", "jpeg", "jpg", "xls", "xlsx", "doc",
			"docx", "odt", "txt", "pdf", "png", "pptx", "ppt", "rtf", "csv");

	@Autowired
	private AmazonS3 s3Client;

	@Autowired
	private TransferManager transferManager;

	@Value("${storage.bucketname}")
	private String bucketName;

	@Value("${dna.feature.attachmentMalwareScan}")
	private boolean attachmentMalwareScan;

	@Autowired
	private AVScannerClient aVScannerClient;

	@Override
	public FileDetailsVO uploadFileToS3Bucket(MultipartFile multiPartFile, String keyName) throws Exception {
		FileDetailsVO fileDetails = new FileDetailsVO();
		try {
			String fileName = multiPartFile.getOriginalFilename();
			fileDetails.setFileName(fileName);
			long fileSize = multiPartFile.getSize();
			fileDetails.setFileSize(formatedSize(fileSize));
			if (isValidAttachment(fileName)) {
				if (attachmentMalwareScan) {
					log.debug("Scanning for malware for file {}", multiPartFile.getName());
					FileScanDetailsVO fileScanDetailsVO = this.scan(multiPartFile, fileDetails);
					if (ObjectUtils.isEmpty(fileScanDetailsVO)) {
						log.info("File {} is infected with malware.", multiPartFile.getName());
						return fileDetails;
					}
				}
				keyName = keyName != null ? keyName : UUID.randomUUID().toString();
				fileDetails.setId(keyName);
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentLength(fileSize);
				log.info("File {} sent to transferManager", multiPartFile.getName());
				Upload upload = transferManager.upload(bucketName, keyName, multiPartFile.getInputStream(), metadata);
				log.info("TransferManager Started uploading file, with fileName {} and keyName {}", fileName, keyName);
//		        upload.waitForCompletion();
//		        log.debug("Upload completed for file with fileName {} and keyName {} ", fileName, keyName);
			} else {
				List<MessageDescription> errors = new ArrayList<MessageDescription>();
				MessageDescription md = new MessageDescription();
				md.setMessage("File type is not supported");
				log.info("File {} is not of supported file types", multiPartFile.getName());
				errors.add(md);
				fileDetails.setErrors(errors);
			}
		} catch (Exception e) {
			log.error("Failed while uploading file {} with exception {} ", multiPartFile.getName(), e.getMessage());
			throw e;
		}
		return fileDetails;
	}

	/**
	 * To scan file by calling AVscan service
	 * 
	 * @param multiPartFile
	 * @param fileDetails
	 * @return FileScanDetailsVO
	 */
	private FileScanDetailsVO scan(MultipartFile multiPartFile, FileDetailsVO fileDetails) {
		FileScanDetailsVO fileScanDetailsVO = null;
		List<MessageDescription> errors = new ArrayList<MessageDescription>();
		MessageDescription md = null;
		Optional<FileScanDetailsVO> aVScannerRes = aVScannerClient.scan(multiPartFile);
		if (!aVScannerRes.isEmpty()) {
			if (aVScannerRes.get().getDetected() != null && !aVScannerRes.get().getDetected()) {
				log.debug("No malware found for file {}", multiPartFile.getName());
				fileScanDetailsVO = aVScannerRes.get();
			} else {
				log.info("Malware found in file {} ErrorMessage:{}", multiPartFile.getName(),
						aVScannerRes.get().getErrorMessage());
				md = new MessageDescription();
				md.setMessage(aVScannerRes.get().getErrorMessage());
				errors.add(md);
				fileDetails.setErrors(errors);
			}
		} else {
			log.info("Got empty response from malware service for file {}", multiPartFile.getName());
			md = new MessageDescription();
			md.setMessage("Error occured while scanning for malware.");
			errors.add(md);
			fileDetails.setErrors(errors);
		}
		return fileScanDetailsVO;
	}

	@Override
	public void deleteFileFromS3Bucket(String keyName) {
		s3Client.deleteObject(new DeleteObjectRequest(bucketName, keyName));
		log.info("File with keyName {} removed successfully", keyName);
	}

	@Override
	public List<FileDetailsVO> getAvailableFilesFromBucket() {
		List<FileDetailsVO> files = new ArrayList<>();
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix("logo/");
		ObjectListing objects = s3Client.listObjects(listObjectsRequest);
		List<S3ObjectSummary> summaries = objects.getObjectSummaries();
		for (S3ObjectSummary item : summaries) {
			FileDetailsVO file = new FileDetailsVO();
			file.setFileSize(formatedSize(item.getSize()));
			file.setId(item.getKey());
			files.add(file);
		}
		return files;
	}

	@Override
	public byte[] getFile(String keyName) throws Exception {
		try {
			S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, keyName));
			S3ObjectInputStream stream = s3Object.getObjectContent();
			byte[] content = IOUtils.toByteArray(stream);
			s3Object.close();
			log.info("downloaded file with keyName {}  successfully", keyName);
			return content;
		} catch (Exception e) {
			log.error("Failed while downloading file with keyName {}  with exception {} ", keyName, e.getMessage());
			throw e;
		}
	}

	public String formatedSize(long size) {
		long valueinMb = size / 1048576;
		long valueinKb = size / 1024;
		if (size > 1024) {
			if (size < 1048576) {
				return valueinKb + " KB";
			} else {
				return valueinMb + " MB";
			}

		} else
			return "1KB";
	}

	@Override
	public LogoDetailsVO uploadLogoToS3Bucket(LogoVO logo) throws Exception {
		LogoDetailsVO logoDetails = new LogoDetailsVO();
		String keyName = UUID.randomUUID().toString();
		logoDetails.setId(keyName);
		logoDetails.setFileName(logo.getFileName());
		long fileSize = logo.getLogo().length;
		logoDetails.setFileSize(formatedSize(fileSize));
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(fileSize);
		log.info("File {} sent to transferManager", logo.getFileName());
		String path = ConstantsUtility.S3_PATH_TO_UPLOAD_LOGO + keyName;
		try {
			InputStream is = new ByteArrayInputStream(logo.getLogo());
			Upload upload = transferManager.upload(bucketName, path, is, metadata);
			log.info("TransferManager Started uploading file, with fileName {} and keyName {}", logo.getFileName(),
					keyName);
//        upload.waitForCompletion();
//        log.debug("Upload completed for file with fileName {} and keyName {} ", fileName, keyName);
		} catch (Exception e) {
			log.error("Failed while uploading file {} with exception {} ", logo.getFileName(), e.getMessage());
			throw e;
		}
		return logoDetails;
	}

	private boolean isValidAttachment(String fileName) {
		boolean isValid = false;
		String extension = FilenameUtils.getExtension(fileName);
		if (contentTypes.contains(extension.toLowerCase())) {
			isValid = true;
		}
		return isValid;
	}

}
