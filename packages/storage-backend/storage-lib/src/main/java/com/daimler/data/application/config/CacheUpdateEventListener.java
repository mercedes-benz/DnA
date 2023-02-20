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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.data.minio.client.DnaMinioClient;
import com.daimler.data.util.RedisCacheUtil;

import io.minio.admin.UserInfo;
import io.minio.admin.UserInfo.Status;


@Component
public class CacheUpdateEventListener {

	private static Logger LOGGER = LoggerFactory.getLogger(CacheUpdateEventListener.class);

	@Autowired
	DnaMinioClient dnaMinioClient;

	@Autowired
	private RedisCacheUtil cacheUtil;

	@PostConstruct
	public void init() {

		LOGGER.info("started updating cache from Minio");
		populateDataOnCache();
		LOGGER.info("Successfully updated data on cache from Minio");
	}

	private void populateDataOnCache() {
		String cacheName = "minioUsersCache";
		
		LOGGER.info("Creating cache for Minio users.");
		cacheUtil.getCache(cacheName);
		
		LOGGER.info("listing Users from minio.");
		Map<String, UserInfo> users = dnaMinioClient.listUsers();
		
		//Adding data to cache
		if(users != null) {
			cacheUtil.updateCache(cacheName, users);
		}
	}
}
