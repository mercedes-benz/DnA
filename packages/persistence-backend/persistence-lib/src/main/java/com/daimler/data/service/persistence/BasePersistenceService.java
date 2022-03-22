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

package com.daimler.data.service.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.MinioGenericResponse;
import com.daimler.data.dto.persistence.BucketObjectCollection;
import com.daimler.data.dto.persistence.BucketResponseWrapperVO;
import com.daimler.data.dto.persistence.BucketVo;
import com.daimler.data.dto.persistence.GetBucketResponseWrapperVO;
import com.daimler.data.dto.persistence.MinioBucketResponse;
import com.daimler.data.dto.persistence.UserRefreshWrapperVO;
import com.daimler.data.dto.persistence.UserVO;
import com.daimler.data.minio.client.DnaMinioClient;
import com.daimler.data.util.ConstantsUtility;

import io.minio.messages.Bucket;

@Service
@SuppressWarnings(value = "unused")
public class BasePersistenceService implements PersistenceService {

	private static Logger LOGGER = LoggerFactory.getLogger(BasePersistenceService.class);

	@Autowired
	private UserStore userStore;

	@Autowired
	private DnaMinioClient dnaMinioClient;

	public BasePersistenceService() {
		super();
	}

	@Override
	public ResponseEntity<BucketResponseWrapperVO> createBucket(BucketVo bucketVo) {
		BucketResponseWrapperVO responseVO = new BucketResponseWrapperVO();
		HttpStatus httpStatus;
		List<UserVO> bucketAccessinfo = new ArrayList<UserVO>();
		String currentUser = userStore.getUserInfo().getId();
		MinioGenericResponse createBucketResponse = dnaMinioClient.createBucket(bucketVo.getBucketName());
		if (createBucketResponse != null && createBucketResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			// Onboarding current user
			MinioGenericResponse onboardOwnerResponse = dnaMinioClient.onboardUserMinio(currentUser,
					createBucketResponse.getPolicies());
			if (onboardOwnerResponse != null && onboardOwnerResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
				onboardOwnerResponse.getUser().setPermissions(Arrays.asList("READ", "WRITE"));
				bucketAccessinfo.add(onboardOwnerResponse.getUser());
			}

			// Onboard collaborators
			if (!ObjectUtils.isEmpty(bucketVo.getCollaborators())) {
				for (UserVO userVO : bucketVo.getCollaborators()) {
					if (!ObjectUtils.isEmpty(userVO.getPermissions())) {
						List<String> policies = userVO.getPermissions().stream()
								.map(n -> bucketVo.getBucketName() + "_" + n.toUpperCase())
								.collect(Collectors.toList());
						MinioGenericResponse onboardUserResponse = dnaMinioClient.onboardUserMinio(currentUser,
								policies);
						if (onboardUserResponse != null && onboardUserResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
							onboardUserResponse.getUser().setPermissions(userVO.getPermissions());
							bucketAccessinfo.add(onboardUserResponse.getUser());
						}
					}
				}
			}

			responseVO.setBucketAccessinfo(bucketAccessinfo);
			httpStatus = HttpStatus.OK;
		} else {
			httpStatus = HttpStatus.BAD_REQUEST;
			// MessageDescription messageDescription = new MessageDescription();
			// messageDescription.setMessage(createBucketResponse.getError());
			List<MessageDescription> messages = new ArrayList<>();
			messages.add(new MessageDescription(createBucketResponse.getError().getErrorMsg()));
			responseVO.setErrors(messages);
		}
		responseVO.setData(bucketVo);
		responseVO.setStatus(createBucketResponse.getStatus());
		return new ResponseEntity<>(responseVO, httpStatus);
	}

	@Override
	public ResponseEntity<GetBucketResponseWrapperVO> getAllBuckets() {
		String currentUser = userStore.getUserInfo().getId();
		HttpStatus httpStatus;

		GetBucketResponseWrapperVO bucketResponseWrapperVO = new GetBucketResponseWrapperVO();
		List<MinioBucketResponse> minioBucketsResponse;
		MinioBucketResponse minioBucketResponse;
		MinioGenericResponse minioResponse = dnaMinioClient.getAllBuckets(currentUser);
		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			httpStatus = HttpStatus.OK;
			minioBucketsResponse = new ArrayList<MinioBucketResponse>();
			for (Bucket bucket : minioResponse.getBuckets()) {
				minioBucketResponse = new MinioBucketResponse();
				minioBucketResponse.setBucketName(bucket.name());
				minioBucketResponse.setCreationDate(bucket.creationDate().toString());

				minioBucketsResponse.add(minioBucketResponse);
			}

			bucketResponseWrapperVO.setData(minioBucketsResponse);
		} else {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			bucketResponseWrapperVO
					.setErrors(Arrays.asList(new MessageDescription(minioResponse.getError().getErrorMsg())));
		}

		bucketResponseWrapperVO.setStatus(minioResponse.getStatus());
		return new ResponseEntity<>(bucketResponseWrapperVO, httpStatus);
	}

	@Override
	public ResponseEntity<BucketObjectCollection> getBucketObjects(String bucketName, @Valid String prefix) {
		String currentUser = userStore.getUserInfo().getId();
		HttpStatus httpStatus;
		BucketObjectCollection bucketObjectCollection = new BucketObjectCollection();

		MinioGenericResponse minioObjectResponse = dnaMinioClient.getBucketObjects(currentUser, bucketName, prefix);
		if (minioObjectResponse != null && minioObjectResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			bucketObjectCollection.setData(minioObjectResponse.getObjects());
			if (ObjectUtils.isEmpty(minioObjectResponse.getObjects())) {
				httpStatus = HttpStatus.NO_CONTENT;
			} else {
				httpStatus = HttpStatus.OK;
			}

		} else {
			bucketObjectCollection
					.setErrors(Arrays.asList(new MessageDescription(minioObjectResponse.getError().getErrorMsg())));
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(bucketObjectCollection, httpStatus);
	}

	@Override
	public ResponseEntity<ByteArrayResource> getObjectContent(String bucketName, String prefix) {
		String currentUser = userStore.getUserInfo().getId();
		HttpStatus httpStatus;
		// ObjectMetadataWrapperVO objectMetadataWrapperVO = new
		// ObjectMetadataWrapperVO();

		MinioGenericResponse minioResponse = dnaMinioClient.getObjectContents(currentUser, bucketName, prefix);

		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			if (Objects.nonNull(minioResponse.getObjectMetadata().getObjectContent())) {
				ByteArrayResource resource = minioResponse.getObjectMetadata().getObjectContent();
				return ResponseEntity.ok().contentLength(resource.contentLength()).contentType(contentType(prefix))
						.header("Content-disposition", "attachment; filename=\"" + fileName(prefix) + "\"")
						.body(resource);

				// objectMetadataWrapperVO.setData(minioResponse.getData());
				// httpStatus = HttpStatus.OK;
			} else {
				httpStatus = HttpStatus.NO_CONTENT;
			}

		} else {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			// objectMetadataWrapperVO.setErrors(Arrays.asList(new
			// MessageDescription(minioResponse.getMessage())));
		}

		return new ResponseEntity<>(null, httpStatus);
	}

	private String fileName(String prefix) {
		String[] arr = prefix.split("/");
		String fileName = arr[arr.length - 1];
		return fileName;
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

	@Override
	public ResponseEntity<BucketResponseWrapperVO> objectUpload(MultipartFile uploadfile, String bucketName,
			String prefix) {

		String currentUser = userStore.getUserInfo().getId();
		HttpStatus httpStatus;
		BucketResponseWrapperVO bucketResponseWrapperVO = new BucketResponseWrapperVO();
		MinioGenericResponse minioResponse = dnaMinioClient.objectUpload(currentUser, uploadfile, bucketName, prefix);
		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			httpStatus = HttpStatus.OK;
		} else {
			bucketResponseWrapperVO
					.setErrors(Arrays.asList(new MessageDescription(minioResponse.getError().getErrorMsg())));
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		bucketResponseWrapperVO.setStatus(minioResponse.getStatus());
		return new ResponseEntity<>(bucketResponseWrapperVO, httpStatus);
	}

	@Override
	public ResponseEntity<UserRefreshWrapperVO> userRefresh(String userId) {
		HttpStatus httpStatus;
		UserRefreshWrapperVO userRefreshWrapperVO = new UserRefreshWrapperVO();
		MinioGenericResponse minioResponse = dnaMinioClient.userRefresh(userId.toUpperCase());
		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			httpStatus = HttpStatus.OK;
			userRefreshWrapperVO.setData(minioResponse.getUser());
		} else {
			userRefreshWrapperVO
					.setErrors(Arrays.asList(new MessageDescription(minioResponse.getError().getErrorMsg())));
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		userRefreshWrapperVO.setStatus(minioResponse.getStatus());
		return new ResponseEntity<>(userRefreshWrapperVO, httpStatus);
	}

}
