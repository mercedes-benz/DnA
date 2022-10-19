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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class StorageUtility {

	
	
	private StorageUtility() {
		super();
	}

	/*
	 * To get Union of list return list of user by making unison of 2 userVO list
	 */
	public static List<String> getUnion(List<String> list1, List<String> list2) {
		Set<String> set = new TreeSet<>(new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		});
		// Adding list1 to set
		if(!ObjectUtils.isEmpty(list1)) {
			set.addAll(list1);
		}
		// Adding list2 to set
		if(!ObjectUtils.isEmpty(list2)) {
			set.addAll(list2);
		}
		return new ArrayList<>(set);
	}
	
	/*
	 * To get connection name for dataiku
	 * eg: projectname_bucketName
	 */
	public static String getDataikuConnectionName(String projectKey, String bucketName) {
		return projectKey+"_"+bucketName;
	}
	
	/**
	 * Remove string and empty values from comma separated string
	 * 
	 * @param baseString
	 * @param stringToBeRemoved
	 * @return result {@code String}
	 */
	public static String removePolicy(String baseString, String stringToBeRemoved) {
		if(StringUtils.hasText(baseString)) {
			List<String> policies = new ArrayList<>();
			policies.addAll(Arrays.asList(baseString.split(",")));
			policies.removeAll(Arrays.asList("", null,stringToBeRemoved));
			baseString = String.join(",", policies);
		}
		return baseString;
	}
	
	/**
	 * Append new string and remove empty values from comma separated string
	 * 
	 * @param baseString
	 * @param stringToBeAdded
	 * @return result {@code String}
	 */
	public static String addPolicy(String baseString, String stringToBeAdded) {
		if (StringUtils.hasText(baseString) && !hasText(baseString, stringToBeAdded)) {
			List<String> policies = new ArrayList<>();
			policies.addAll(Arrays.asList(baseString.split(",")));
			policies.removeAll(Arrays.asList("", null));
			policies.add(stringToBeAdded);
			baseString = String.join(",", policies);
		}
		return baseString;
	}
	
	
	/**
	 * To find exact match in base String
	 * 
	 * @param baseString
	 * @param toBeSearched
	 * @return hasText {@code boolean}
	 */
	public static boolean hasText(String baseString, String stringToBeMatched) {
		boolean hasText = false;
		if (Pattern.compile("\\b" + stringToBeMatched + "\\b", Pattern.CASE_INSENSITIVE).matcher(baseString).find()) {
			hasText = true;
		}
		return hasText;
	}
	
}
