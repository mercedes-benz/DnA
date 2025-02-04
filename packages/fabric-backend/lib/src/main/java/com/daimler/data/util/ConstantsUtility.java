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

	public static final String PENDING_STATE = "PENDING";
	public static final String CREATED_STATE = "CREATED";
	public static final String ASSIGNED_STATE = "ASSIGNED";
	public static final String FAILED_STATE = "FAILED";
	
	public static final String COMPLETED_STATE = "COMPLETED";
	public static final String INPROGRESS_STATE = "IN_PROGRESS";
	
	public static final String ENTITLEMENT_TYPE = "ENTITLEMENT";
	
	public static final String PERMISSION_CONTRIBUTOR = "Contributor";
	public static final String PERMISSION_ADMIN = "Admin";
	public static final String PERMISSION_MEMBER = "Member";
	public static final String PERMISSION_VIEWER = "Viewer";
	
	public static final String DATACLASSIFICATION_CONFIDENTIAL = "CONFIDENTIAL";
	
	public static final String GROUPSEARCH_URL_PREFIX = "https://graph.microsoft.com/v1.0/groups?$filter=startsWith(displayName,'";
	public static final String GROUPSEARCH_URL_SUFFIX = "')";
	
	public static final String GROUPPRINCIPAL_GROUP_TYPE = "Group";
	public static final String GROUPPRINCIPAL_USER_TYPE = "User";
	public static final String GROUPPRINCIPAL_APP_TYPE = "App";

	public static final String JOB_STATUS_RUNNING = "running";
	public static final String JOB_STATUS_STOPPED = "stopped";
	public static final String JOB_STATUS_COMPLETED = "completed";
	public static final String JOB_STATUS_WAITING = "waiting";
	
}
