package com.daimler.data.dto.userinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * UserInfoVO
 */
@Validated


public class UserInfoVO   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("firstName")
  private String firstName = null;

  @JsonProperty("lastName")
  private String lastName = null;

  @JsonProperty("department")
  private String department = null;

  @JsonProperty("email")
  private String email = null;

  @JsonProperty("mobileNumber")
  private String mobileNumber = null;

  @JsonProperty("token")
  private String token = null;

  @JsonProperty("roles")
  @Valid
  private List<UserRoleVO> roles = null;

  public UserInfoVO id(String id) {
    this.id = id;
    return this;
  }

  /**
   * ID of user
   * @return id
  **/
  @ApiModelProperty(value = "ID of user")


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public UserInfoVO firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  /**
   * First Name of the user
   * @return firstName
  **/
  @ApiModelProperty(value = "First Name of the user")

@Size(min=1) 
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public UserInfoVO lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  /**
   * Last Name of the user
   * @return lastName
  **/
  @ApiModelProperty(value = "Last Name of the user")

@Size(min=1) 
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public UserInfoVO department(String department) {
    this.department = department;
    return this;
  }

  /**
   * Department of the user
   * @return department
  **/
  @ApiModelProperty(value = "Department of the user")


  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public UserInfoVO email(String email) {
    this.email = email;
    return this;
  }

  /**
   * E-Mail of the user
   * @return email
  **/
  @ApiModelProperty(value = "E-Mail of the user")


  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public UserInfoVO mobileNumber(String mobileNumber) {
    this.mobileNumber = mobileNumber;
    return this;
  }

  /**
   * Mobile number of the user
   * @return mobileNumber
  **/
  @ApiModelProperty(value = "Mobile number of the user")


  public String getMobileNumber() {
    return mobileNumber;
  }

  public void setMobileNumber(String mobileNumber) {
    this.mobileNumber = mobileNumber;
  }

  

  public UserInfoVO token(String token) {
    this.token = token;
    return this;
  }

  /**
   * Get token
   * @return token
  **/
  @ApiModelProperty(value = "")


  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public UserInfoVO roles(List<UserRoleVO> roles) {
    this.roles = roles;
    return this;
  }

  public UserInfoVO addRolesItem(UserRoleVO rolesItem) {
    if (this.roles == null) {
      this.roles = new ArrayList<UserRoleVO>();
    }
    this.roles.add(rolesItem);
    return this;
  }

  /**
   * Get roles
   * @return roles
  **/
  @ApiModelProperty(value = "")

  @Valid

  public List<UserRoleVO> getRoles() {
    return roles;
  }

  public void setRoles(List<UserRoleVO> roles) {
    this.roles = roles;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserInfoVO userInfoVO = (UserInfoVO) o;
    return Objects.equals(this.id, userInfoVO.id) &&
        Objects.equals(this.firstName, userInfoVO.firstName) &&
        Objects.equals(this.lastName, userInfoVO.lastName) &&
        Objects.equals(this.department, userInfoVO.department) &&
        Objects.equals(this.email, userInfoVO.email) &&
        Objects.equals(this.mobileNumber, userInfoVO.mobileNumber) &&
        Objects.equals(this.token, userInfoVO.token) &&
        Objects.equals(this.roles, userInfoVO.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, lastName, department, email, mobileNumber, token, roles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserInfoVO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    department: ").append(toIndentedString(department)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    mobileNumber: ").append(toIndentedString(mobileNumber)).append("\n");
    sb.append("    token: ").append(toIndentedString(token)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

