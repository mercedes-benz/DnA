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

package com.daimler.data.db.jsonb.solution;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolutionTeamMember implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String userType;
	private String shortId;
	private String firstName;
	private String lastName;
	private String department;
	private String email;
	private String mobileNumber;
	private String company;
	private String teamMemberPosition;

//	public SolutionTeamMember() {
//		super();
//	}
//
//	public SolutionTeamMember(String id, String userType, String firstName, String lastName, String department,
//			String email, String mobileNumber, String company, String teamMemberPosition) {
//		super();
//		this.id = id;
//		this.userType = userType;
//		this.firstName = firstName;
//		this.lastName = lastName;
//		this.department = department;
//		this.email = email;
//		this.mobileNumber = mobileNumber;
//		this.company = company;
//		this.teamMemberPosition = teamMemberPosition;
//	}
//
//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}
//
//	public String getUserType() {
//		return userType;
//	}
//
//	public void setUserType(String userType) {
//		this.userType = userType;
//	}
//
//	public String getFirstName() {
//		return firstName;
//	}
//
//	public void setFirstName(String firstName) {
//		this.firstName = firstName;
//	}
//
//	public String getLastName() {
//		return lastName;
//	}
//
//	public void setLastName(String lastName) {
//		this.lastName = lastName;
//	}
//
//	public String getDepartment() {
//		return department;
//	}
//
//	public void setDepartment(String department) {
//		this.department = department;
//	}
//
//	public String getEmail() {
//		return email;
//	}
//
//	public void setEmail(String email) {
//		this.email = email;
//	}
//
//	public String getMobileNumber() {
//		return mobileNumber;
//	}
//
//	public void setMobileNumber(String mobileNumber) {
//		this.mobileNumber = mobileNumber;
//	}
//
//	public String getCompany() {
//		return company;
//	}
//
//	public void setCompany(String company) {
//		this.company = company;
//	}
//
//	public String getTeamMemberPosition() {
//		return teamMemberPosition;
//	}
//
//	public void setTeamMemberPosition(String teamMemberPosition) {
//		this.teamMemberPosition = teamMemberPosition;
//	}

}
