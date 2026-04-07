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

package com.daimler.data.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@RestController
@Api(value = "Login API", tags = { "authentication" })
@RequestMapping("/api")
public class LoginController {

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
		private List<UserRole> digiRole;
		private List<String> divisionAdmins;

		private String sub;
		private boolean email_verified;
		private String name;
		private String given_name;
		private String family_name;
		private String personal_data_hint;
		private String updated_at;

		public UserInfo() {
			// Always add USER role
			this.digiRole = new ArrayList<>();
			// digiRole.add(new UserRole("1", "USER"));
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

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class UserRole implements Serializable {
		private String id;
		private String name;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class IntrospectionResponse implements Serializable {
		private String sub;
		private String aud;
		private String scope;
		private String active;
		private String token_type;
		private String exp;
		private String client_id;
		private String auth_time;
	}

	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AuthoriserResponse implements Serializable {
		private String id;
		private String givenname;
		private String surname;
		private String mailAddress;
		private String mobileNumber;
		private String departmentNumber;

		public AuthoriserResponse(JsonNode userInfo) {
			
            ObjectMapper objectMapper = new ObjectMapper();
            AuthoriserResponse mappedResponse = objectMapper.convertValue(userInfo, AuthoriserResponse.class);

            this.id = mappedResponse.getId();
            this.givenname = mappedResponse.getGivenname();
            this.surname = mappedResponse.getSurname();
            this.mailAddress = mappedResponse.getMailAddress();
            this.mobileNumber = mappedResponse.getMobileNumber();
            this.departmentNumber = mappedResponse.getDepartmentNumber();


		}
	}
}
