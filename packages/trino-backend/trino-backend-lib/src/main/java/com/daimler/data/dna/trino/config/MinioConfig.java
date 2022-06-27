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

package com.daimler.data.dna.trino.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MinioConfig {

	@Value("${minio.baseUri}")
	private String minioBaseUri;
	
	@Value("${minio.accessKey}")
	private String minioAccessKey;
	
	@Value("${minio.secretKey}")
	private String minioSecretKey;
	
	public MinioClient getMinioClient(String endpointuri, String accessKey, String secretKey) {
		try {
			MinioClient minioClient = MinioClient.builder().endpoint(endpointuri).credentials(accessKey, secretKey)
					.build();
			log.info("Successfully got minioclient for minio running at endpoint : {}", endpointuri);
			return minioClient;
		} catch (Exception e) {
			log.error("Failed to get minioclient for given config endpoint {}, exception occured is : {}", endpointuri,
					e.getMessage());
			return null;
		}
	}

	public void moveObject(String sourceBucket, String objectPath, String destinationBucket, String newFilePath) throws Exception
	{
		MinioClient minioClient = this.getMinioClient(minioBaseUri, minioAccessKey, minioSecretKey);
		 if(!minioClient.bucketExists(BucketExistsArgs.builder().bucket(destinationBucket).build())) {
			    minioClient.makeBucket(MakeBucketArgs.builder().bucket(destinationBucket).build());
			  }
		CopySource source = CopySource.builder().bucket(sourceBucket).object(objectPath).build();
		CopyObjectArgs copyArgs = CopyObjectArgs.builder().source(source).bucket(destinationBucket).object(newFilePath).build();
		minioClient.copyObject(copyArgs);
		log.info("Successfully copied object to {} from {}", destinationBucket+"/"+newFilePath , sourceBucket+"/"+objectPath);
		minioClient.removeObject(RemoveObjectArgs.builder().bucket(sourceBucket).object(objectPath).build());
		log.info("Successfully removed object from {}, after copy", sourceBucket+"/"+objectPath);

	}


}
