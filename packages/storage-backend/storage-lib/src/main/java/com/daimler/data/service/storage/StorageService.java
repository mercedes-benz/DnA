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

package com.daimler.data.service.storage;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.storage.BucketCollectionVO;
import com.daimler.data.dto.storage.BucketObjectResponseWrapperVO;
import com.daimler.data.dto.storage.BucketResponseWrapperVO;
import com.daimler.data.dto.storage.BucketVo;
import com.daimler.data.dto.storage.ConnectionResponseWrapperVO;
import com.daimler.data.dto.storage.ConnectionVO;
import com.daimler.data.dto.storage.UserRefreshWrapperVO;

public interface StorageService {

	/**
	 * To make a new bucket
	 * 
	 * @param bucketVo
	 * @return ResponseEntity<BucketResponseWrapperVO>
	 */
	public ResponseEntity<BucketResponseWrapperVO> createBucket(BucketVo bucketVo);

	/**
	 * To list all the buckets for user
	 * 
	 * @return ResponseEntity<BucketCollectionVO>
	 */
	public ResponseEntity<BucketCollectionVO> getAllBuckets(int limit, String sortBy, String sortOrder, int offset); 

	/**
	 * To list all the objects inside buckets
	 * 
	 * @param bucketName
	 * @param prefix
	 * @return ResponseEntity<BucketObjectResponseWrapperVO>
	 */
	public ResponseEntity<BucketObjectResponseWrapperVO> getBucketObjects(String bucketName, String prefix);

	/**
	 * To get object contents for given path
	 * 
	 * @param bucketName
	 * @param prefix
	 * @return ResponseEntity<ByteArrayResource>
	 */
	public ResponseEntity<ByteArrayResource> getObjectContent(String bucketName, String prefix);

	/**
	 * To upload object in given path
	 * 
	 * @param uploadfile
	 * @param bucketName
	 * @param prefix
	 * @return ResponseEntity<BucketResponseWrapperVO>
	 */
	public ResponseEntity<BucketResponseWrapperVO> objectUpload(MultipartFile uploadfile, String bucketName,
			String prefix);

	/**
	 * To refresh user secret
	 * 
	 * @param userId
	 * @return ResponseEntity<UserRefreshWrapperVO>
	 */
	public ResponseEntity<UserRefreshWrapperVO> userRefresh(String userId);
	
	/**
	 * To get connection details of given path for user
	 * 
	 * @param bucketName
	 * @param userId
	 * @param prefix
	 * @return ResponseEntity<ConnectionResponseWrapperVO>
	 */
	public ResponseEntity<ConnectionResponseWrapperVO> getConnection(String bucketName, String userId, String prefix);

	/**
	 * To refresh ehcache
	 * 
	 * @return void
	 */
	public ResponseEntity<GenericMessage> cacheRefresh();
	
	/**
	 * To delete bucket's object of given path.
	 * 
	 * @param bucketName
	 * @param prefix
	 * @return ResponseEntity<GenericMessage>
	 */
	public ResponseEntity<GenericMessage> deleteBucketObjects(String bucketName, String prefix);

	/**
	 * To delete an empty bucket
	 * 
	 * @param bucketName
	 * @return ResponseEntity<GenericMessage>
	 */
	public ResponseEntity<GenericMessage> deleteBucket(String bucketName, Boolean live);

	/**
	 * To delete an non empty bucket
	 *
	 * @param bucketName
	 * @return ResponseEntity<GenericMessage>
	 */
	@Transactional
	ResponseEntity<GenericMessage> deleteBucketCascade(String bucketName, Boolean live);

	/**
	 * True if the bucket exists.
	 *
	 * @param bucketName
	 * @return ResponseEntity<GenericMessage>
	 */
	@Transactional
	Boolean isBucketPresent(String bucketName);

	/**
	 * To update bucket along with collaborator
	 * 
	 * @param bucketVo
	 * @return ResponseEntity<BucketResponseWrapperVO>
	 */
	public ResponseEntity<BucketResponseWrapperVO> updateBucket(BucketVo bucketVo);
	
	/**
	 * To get bucket details for given bucket name
	 * 
	 * @param bucketName
	 * @return ResponseEntity<BucketVo>
	 */
	public ResponseEntity<BucketVo> getByBucketName(String bucketName);
	
	/**
	 * To migrate storage buckets
	 * 
	 * @param void
	 * @return ResponseEntity<GenericMessage>
	 */
	public ResponseEntity<GenericMessage> bucketMigrate();
	
	/**
	 * To create bucket connection in dataiku for given project role groups  
	 * 
	 * @param connectionVO {@code ConnectionVO}
	 * @return response {@code ResponseEntity<GenericMessage>}
	 */
	public ResponseEntity<GenericMessage> createDataikuConnection(ConnectionVO connectionVO, Boolean live);
	
}
