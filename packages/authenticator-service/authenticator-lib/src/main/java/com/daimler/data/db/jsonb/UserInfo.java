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

package com.daimler.data.db.jsonb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo implements Serializable {

	private static final long serialVersionUID = 4606219323373826458L;

	String firstName;
	String lastName;
	String email;
	String mobileNumber;
	String department;

	List<UserInfoRole> roles;
	List<UserFavoriteUseCase> favoriteUsecases;
	private List<String> divisionAdmins;

//    public UserInfo() {
//        super();
//    }
//
//    public UserInfo(String firstName, String lastName, String email, String mobileNumber, String department,
//                    List<UserInfoRole> roles, List<UserFavoriteUseCase> favoriteUsecases) {
//        super();
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.email = email;
//        this.mobileNumber = mobileNumber;
//        this.department = department;
//        this.roles = roles;
//        this.favoriteUsecases = favoriteUsecases;
//    }

//    public String getFirstName() {
//        return firstName;
//    }
//
//    public void setFirstName(String firstName) {
//        this.firstName = firstName;
//    }
//
//    public String getLastName() {
//        return lastName;
//    }
//
//    public void setLastName(String lastName) {
//        this.lastName = lastName;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getMobileNumber() {
//        return mobileNumber;
//    }
//
//    public void setMobileNumber(String mobileNumber) {
//        this.mobileNumber = mobileNumber;
//    }
//
//    public String getDepartment() {
//        return department;
//    }
//
//    public void setDepartment(String department) {
//        this.department = department;
//    }
//
//    public List<UserInfoRole> getRoles() {
//        return roles;
//    }
//
//    public void setRoles(List<UserInfoRole> roles) {
//        this.roles = roles;
//    }
//
//    public List<UserFavoriteUseCase> getFavoriteUsecases() {
//        return favoriteUsecases;
//    }
//
//    public void setFavoriteUsecases(List<UserFavoriteUseCase> favoriteUsecases) {
//        this.favoriteUsecases = favoriteUsecases;
//    }

}
