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

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.dto.persistence.BucketCollectionVO;
import com.daimler.data.dto.persistence.BucketObjectResponseWrapperVO;
import com.daimler.data.dto.persistence.BucketResponseWrapperVO;
import com.daimler.data.dto.persistence.BucketVo;
import com.daimler.data.dto.persistence.UserRefreshWrapperVO;

public interface PersistenceService {

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
	public ResponseEntity<BucketCollectionVO> getAllBuckets();

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
}
