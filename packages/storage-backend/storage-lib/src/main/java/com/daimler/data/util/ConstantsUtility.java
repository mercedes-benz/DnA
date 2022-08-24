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

public class ConstantsUtility {

	public static final String SUCCESS = "SUCCESS";
	public static final String FAILURE = "FAILURE";
	
	public static final String READWRITE = "RW";
	public static final String READ = "READ";
	public static final String DELETE = "DEL";
	public static final String URI = "uri";
	public static final String HOSTNAME = "hostName";
	public static final String BUCKET_NAME = "bucketName";
	public static final String DATAIKU_READ_ONLY = "READ-ONLY";
	
	//for rest template
	public static final String ACCEPT = "Accept";
	public static final String CONTENT_TYPE = "Content-Type";
	
	//Variables for cache
	public static final String MINIO_USERS_CACHE = "minioUsersCache";
	
	//Variables To make minio policy
	public static final String POLICY_LIST_BUCKET = "s3:ListBucket";
	public static final String POLICY_PUT_OBJECT = "s3:PutObject";
	public static final String POLICY_GET_OBJECT = "s3:GetObject";
	public static final String POLICY_DELETE_OBJECT = "s3:DeleteObject";
	public static final String POLICY_BUCKET_LOCATION = "s3:GetBucketLocation";
	public static final String POLICY_RESOURCE = "arn:aws:s3:::";
}
