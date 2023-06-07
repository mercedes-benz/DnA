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

package com.daimler.data.registry.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MinioConfig {

	@Value("${minio.models.bucketname}")
	private String minioModelsBucketName;

	@Value("${minio.models.pattern.prefix}")
	private String minioModelsPatternPrefix;

	public MinioClient getMinioClient(String endpointuri, String accessKey, String secretKey) {
		MinioClient minioClient = MinioClient.builder().endpoint(endpointuri).credentials(accessKey, secretKey).build();
		log.info("Successfully got minioclient for minio running at endpoint : {}", endpointuri);
		return minioClient;
	}

	public List<String> getModels(MinioClient client, String userId) throws Exception {
		List<String> models = new ArrayList<>();
		String patternMatch = minioModelsPatternPrefix + userId.toLowerCase();
		log.info("Pattern used to identify user specific object is {} ", patternMatch);
		if (client != null) {
			Iterable<Result<Item>> results = client
					.listObjects(ListObjectsArgs.builder().bucket(minioModelsBucketName).recursive(true).build());
			Iterator<Result<Item>> iterator = results.iterator();
			while (iterator.hasNext()) {
				Result<Item> el = iterator.next();
				String objectName = el.get().objectName();
				if (objectName != null && objectName.length() > minioModelsPatternPrefix.length()
						&& objectName.contains(patternMatch)) {
					models.add(objectName);
				}
			}
		}
		return models;
	}
	public Integer getModelsCount(MinioClient client) throws Exception {
		Integer count =0;
		if (client != null) {
			Iterable<Result<Item>> results = client
					.listObjects(ListObjectsArgs.builder().bucket(minioModelsBucketName).recursive(true).build());
			Iterator<Result<Item>> iterator = results.iterator();
		if(results!= null){
			while (iterator.hasNext()) {
				count=count+1;
			}
		}
		}
		return count;
	}

}
