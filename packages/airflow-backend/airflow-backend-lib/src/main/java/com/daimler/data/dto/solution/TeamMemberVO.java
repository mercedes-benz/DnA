package com.daimler.data.dto.solution;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * TeamMemberVO
 */
@Validated


public class TeamMemberVO   {
  @JsonProperty("shortId")
  private String shortId = null;

  /**
   * Internal or External
   */
  public enum UserTypeEnum {
    INTERNAL("internal"),
    
    EXTERNAL("external");

    private String value;

    UserTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static UserTypeEnum fromValue(String text) {
      for (UserTypeEnum b : UserTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("userType")
  private UserTypeEnum userType = null;

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

  @JsonProperty("company")
  private String company = null;

  @JsonProperty("teamMemberPosition")
  private String teamMemberPosition = null;

  public TeamMemberVO shortId(String shortId) {
    this.shortId = shortId;
    return this;
  }

  /**
   * Short id of the user
   * @return shortId
  **/
  @ApiModelProperty(value = "Short id of the user")


  public String getShortId() {
    return shortId;
  }

  public void setShortId(String shortId) {
    this.shortId = shortId;
  }

  public TeamMemberVO userType(UserTypeEnum userType) {
    this.userType = userType;
    return this;
  }

  /**
   * Internal or External
   * @return userType
  **/
  @ApiModelProperty(value = "Internal or External")


  public UserTypeEnum getUserType() {
    return userType;
  }

  public void setUserType(UserTypeEnum userType) {
    this.userType = userType;
  }

  public TeamMemberVO firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  /**
   * First Name of the user
   * @return firstName
  **/
  @ApiModelProperty(value = "First Name of the user")


  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public TeamMemberVO lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  /**
   * Last Name of the user
   * @return lastName
  **/
  @ApiModelProperty(value = "Last Name of the user")


  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public TeamMemberVO department(String department) {
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

  public TeamMemberVO email(String email) {
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

  public TeamMemberVO mobileNumber(String mobileNumber) {
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

  public TeamMemberVO company(String company) {
    this.company = company;
    return this;
  }

  /**
   * Company name of the external member
   * @return company
  **/
  @ApiModelProperty(value = "Company name of the external member")


  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public TeamMemberVO teamMemberPosition(String teamMemberPosition) {
    this.teamMemberPosition = teamMemberPosition;
    return this;
  }

  /**
   * Position of team member in project
   * @return teamMemberPosition
  **/
  @ApiModelProperty(value = "Position of team member in project")


  public String getTeamMemberPosition() {
    return teamMemberPosition;
  }

  public void setTeamMemberPosition(String teamMemberPosition) {
    this.teamMemberPosition = teamMemberPosition;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TeamMemberVO teamMemberVO = (TeamMemberVO) o;
    return Objects.equals(this.shortId, teamMemberVO.shortId) &&
        Objects.equals(this.userType, teamMemberVO.userType) &&
        Objects.equals(this.firstName, teamMemberVO.firstName) &&
        Objects.equals(this.lastName, teamMemberVO.lastName) &&
        Objects.equals(this.department, teamMemberVO.department) &&
        Objects.equals(this.email, teamMemberVO.email) &&
        Objects.equals(this.mobileNumber, teamMemberVO.mobileNumber) &&
        Objects.equals(this.company, teamMemberVO.company) &&
        Objects.equals(this.teamMemberPosition, teamMemberVO.teamMemberPosition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(shortId, userType, firstName, lastName, department, email, mobileNumber, company, teamMemberPosition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TeamMemberVO {\n");
    
    sb.append("    shortId: ").append(toIndentedString(shortId)).append("\n");
    sb.append("    userType: ").append(toIndentedString(userType)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    department: ").append(toIndentedString(department)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    mobileNumber: ").append(toIndentedString(mobileNumber)).append("\n");
    sb.append("    company: ").append(toIndentedString(company)).append("\n");
    sb.append("    teamMemberPosition: ").append(toIndentedString(teamMemberPosition)).append("\n");
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

