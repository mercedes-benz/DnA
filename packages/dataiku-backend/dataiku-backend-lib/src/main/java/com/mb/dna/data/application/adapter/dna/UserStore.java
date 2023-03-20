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

package com.mb.dna.data.application.adapter.dna;

import io.micronaut.runtime.context.scope.ThreadLocal;

@ThreadLocal
public class UserStore {

	private UserInfo userInfo = null;

	public void clear() {
		this.userInfo = null;
	}

	public CreatedByVO getVO() {
		CreatedByVO vo = new CreatedByVO();
		vo.setId(this.userInfo.getId());
		vo.setFirstName(this.userInfo.getFirstName());
		vo.setLastName(this.userInfo.getLastName());
		vo.setDepartment(this.userInfo.getDepartment());
		vo.setEmail(this.userInfo.getEmail());
		vo.setMobileNumber(this.userInfo.getMobileNumber());
		return vo;
	}

	public UserInfo getUserInfo() {
		return this.userInfo;
	}

	public void setUserInfo(UserInfo userInfo1) {
		this.userInfo = userInfo1;
	}
	
	
}
