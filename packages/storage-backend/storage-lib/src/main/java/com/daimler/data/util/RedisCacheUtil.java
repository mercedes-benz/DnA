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

package com.daimler.data.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;

import com.daimler.data.minio.client.DnaMinioClientImp;

import io.minio.admin.UserInfo;

@Component
public class RedisCacheUtil {

	private Logger LOGGER = LoggerFactory.getLogger(DnaMinioClientImp.class);
	
	@Autowired
	private RedisCacheManager cacheManager;
	
	public Cache getCache(String cacheName) {
		return this.cacheManager.getCache(cacheName);
	}
	
	public void updateCache(String cacheName, Map<String, UserInfo> users) {
		LOGGER.info("Updating cache:{}",cacheName);
		this.getCache(cacheName).put("users", users);
	}

	public void removeAll(String cacheName) {
		LOGGER.info("deleting all cache entries for name:{}",cacheName);
		this.getCache(cacheName).clear();
	}
	
	public Map<String, UserInfo> getMinioUsers(String cacheName) {
		Map<String, UserInfo> users = null;
		ValueWrapper value = this.getCache(cacheName).get("users");
		users = (Map<String, UserInfo>) value.get();
		return users;
	}

	
}
