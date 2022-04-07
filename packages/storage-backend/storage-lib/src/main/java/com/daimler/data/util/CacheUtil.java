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
import org.springframework.stereotype.Component;

import com.daimler.data.minio.client.DnaMinioClientImp;

import io.minio.admin.UserInfo;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Searchable;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

@Component
public class CacheUtil {

	private Logger LOGGER = LoggerFactory.getLogger(DnaMinioClientImp.class);
	
	@Autowired
	private CacheManager cacheManager;

	public void createCache(String cacheName) {
		Searchable search = new Searchable();
		search.allowDynamicIndexing(true);
		CacheConfiguration cacheConfiguration = new CacheConfiguration(cacheName, 2000);
		cacheConfiguration.addSearchable(search);
		LOGGER.info("Creating cache:{}",cacheName);
		Ehcache minioUsersCache = new Cache(
				cacheConfiguration.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU).eternal(true));

		cacheManager.addCache(minioUsersCache);
	}

	public void updateCache(String cacheName, Map<String, UserInfo> users) {
		LOGGER.info("Updating cache:{}",cacheName);
		this.getCache(cacheName).put(new Element("users", users));
	}
	
	public Ehcache getCache(String cacheName) {
		return this.cacheManager.getEhcache(cacheName);
	}

	public void removeAll(String cacheName) {
		LOGGER.info("Updating cache:{}",cacheName);
		this.getCache(cacheName).removeAll();
	}
	
	public Map<String, UserInfo> getMinioUsers(String cacheName) {
		Map<String, UserInfo> users = null;
		Query query = this.getCache(cacheName).createQuery().includeKeys().includeValues()
				.end();
		
		Results results = query.execute();
		if(results.hasValues()) {
			users = (Map<String, UserInfo>) results.all().get(0).getValue();
		}
		return users;
	}

	
}
