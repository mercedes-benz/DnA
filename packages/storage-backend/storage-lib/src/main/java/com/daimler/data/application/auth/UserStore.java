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

package com.daimler.data.application.auth;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Component;

import com.daimler.data.dto.storage.CreatedByVO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStore {

	private UserInfo userInfo;

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

	@Data
	@AllArgsConstructor
	@Builder
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class UserInfo {
		private String id;
		private String firstName;
		private String lastName;
		private String email;
		private String mobileNumber;
		private String department;

		private String sub;
		private boolean email_verified;
		private String name;
		private String given_name;
		private String family_name;
		private String personal_data_hint;
		private String updated_at;
		private List<UserRole> userRole;

		public UserInfo() {
			this.department = "";
		}

		public void setSub(String sub) {
			this.sub = this.id = sub;
		}

		public void setGiven_name(String given_name) {
			this.firstName = this.given_name = given_name;
		}

		public void setFamily_name(String family_name) {
			this.lastName = this.family_name = family_name;
		}
		
		public boolean hasAdminAccess() {
			return this.getUserRole().stream().anyMatch(n -> "Admin".equalsIgnoreCase(n.getName()));
		}

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class UserRole implements Serializable {
		private static final long serialVersionUID = 1L;
		private String id;
		private String name;
	}

}
