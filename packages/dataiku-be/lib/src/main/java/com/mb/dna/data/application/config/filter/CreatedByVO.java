package com.mb.dna.data.application.config.filter;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;


public class CreatedByVO   {
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

  public CreatedByVO id(String id) {
    this.id = id;
    return this;
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public CreatedByVO firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }


  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public CreatedByVO lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public CreatedByVO department(String department) {
    this.department = department;
    return this;
  }


  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public CreatedByVO email(String email) {
    this.email = email;
    return this;
  }


  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public CreatedByVO mobileNumber(String mobileNumber) {
    this.mobileNumber = mobileNumber;
    return this;
  }


  public String getMobileNumber() {
    return mobileNumber;
  }

  public void setMobileNumber(String mobileNumber) {
    this.mobileNumber = mobileNumber;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreatedByVO createdByVO = (CreatedByVO) o;
    return Objects.equals(this.id, createdByVO.id) &&
        Objects.equals(this.firstName, createdByVO.firstName) &&
        Objects.equals(this.lastName, createdByVO.lastName) &&
        Objects.equals(this.department, createdByVO.department) &&
        Objects.equals(this.email, createdByVO.email) &&
        Objects.equals(this.mobileNumber, createdByVO.mobileNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, lastName, department, email, mobileNumber);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreatedByVO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    department: ").append(toIndentedString(department)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    mobileNumber: ").append(toIndentedString(mobileNumber)).append("\n");
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

