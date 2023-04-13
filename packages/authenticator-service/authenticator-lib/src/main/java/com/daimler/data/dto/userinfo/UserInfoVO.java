package com.daimler.data.dto.userinfo;

import java.util.Objects;
import com.daimler.data.dto.userinfo.UserFavoriteUseCaseVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

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

  @JsonProperty("favoriteUsecases")
  @Valid
  private List<UserFavoriteUseCaseVO> favoriteUsecases = null;

  @JsonProperty("token")
  private String token = null;

  @JsonProperty("roles")
  @Valid
  private List<UserRoleVO> roles = null;

  @JsonProperty("divisionAdmins")
  @Valid
  private List<String> divisionAdmins = null;

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

  public UserInfoVO favoriteUsecases(List<UserFavoriteUseCaseVO> favoriteUsecases) {
    this.favoriteUsecases = favoriteUsecases;
    return this;
  }

  public UserInfoVO addFavoriteUsecasesItem(UserFavoriteUseCaseVO favoriteUsecasesItem) {
    if (this.favoriteUsecases == null) {
      this.favoriteUsecases = new ArrayList<UserFavoriteUseCaseVO>();
    }
    this.favoriteUsecases.add(favoriteUsecasesItem);
    return this;
  }

  /**
   * Get favoriteUsecases
   * @return favoriteUsecases
  **/
  @ApiModelProperty(value = "")

  @Valid

  public List<UserFavoriteUseCaseVO> getFavoriteUsecases() {
    return favoriteUsecases;
  }

  public void setFavoriteUsecases(List<UserFavoriteUseCaseVO> favoriteUsecases) {
    this.favoriteUsecases = favoriteUsecases;
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

  public UserInfoVO divisionAdmins(List<String> divisionAdmins) {
    this.divisionAdmins = divisionAdmins;
    return this;
  }

  public UserInfoVO addDivisionAdminsItem(String divisionAdminsItem) {
    if (this.divisionAdmins == null) {
      this.divisionAdmins = new ArrayList<String>();
    }
    this.divisionAdmins.add(divisionAdminsItem);
    return this;
  }

  /**
   * List of divisions for which user has access as part of DivisionAdmin role.
   * @return divisionAdmins
  **/
  @ApiModelProperty(value = "List of divisions for which user has access as part of DivisionAdmin role.")


  public List<String> getDivisionAdmins() {
    return divisionAdmins;
  }

  public void setDivisionAdmins(List<String> divisionAdmins) {
    this.divisionAdmins = divisionAdmins;
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
        Objects.equals(this.favoriteUsecases, userInfoVO.favoriteUsecases) &&
        Objects.equals(this.token, userInfoVO.token) &&
        Objects.equals(this.roles, userInfoVO.roles) &&
        Objects.equals(this.divisionAdmins, userInfoVO.divisionAdmins);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, lastName, department, email, mobileNumber, favoriteUsecases, token, roles, divisionAdmins);
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
    sb.append("    favoriteUsecases: ").append(toIndentedString(favoriteUsecases)).append("\n");
    sb.append("    token: ").append(toIndentedString(token)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
    sb.append("    divisionAdmins: ").append(toIndentedString(divisionAdmins)).append("\n");
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

