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

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Bucket;
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
	
//	public void copyObjects(String source,String destination, String newFileName) {
//		
//	}
	
	public List<Bucket> getBuckets(MinioClient client) {
		try {
			return client.listBuckets();
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IOException e) {
			log.error("Failed to get buckets list, exception occured is : {}", e.getMessage());
			return null;
		}
	}


}
