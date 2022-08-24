package com.daimler.data.dto.userinfo;

import java.util.Objects;
import com.daimler.data.dto.userinfo.UserInfoVO;
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
 * Collection of Users.
 */
@ApiModel(description = "Collection of Users.")
@Validated


public class UsersCollection   {
  @JsonProperty("totalCount")
  private Integer totalCount = null;

  @JsonProperty("records")
  @Valid
  private List<UserInfoVO> records = null;

  public UsersCollection totalCount(Integer totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  /**
   * Total Record Count
   * @return totalCount
  **/
  @ApiModelProperty(value = "Total Record Count")


  public Integer getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }

  public UsersCollection records(List<UserInfoVO> records) {
    this.records = records;
    return this;
  }

  public UsersCollection addRecordsItem(UserInfoVO recordsItem) {
    if (this.records == null) {
      this.records = new ArrayList<UserInfoVO>();
    }
    this.records.add(recordsItem);
    return this;
  }

  /**
   * Get records
   * @return records
  **/
  @ApiModelProperty(value = "")

  @Valid

  public List<UserInfoVO> getRecords() {
    return records;
  }

  public void setRecords(List<UserInfoVO> records) {
    this.records = records;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UsersCollection usersCollection = (UsersCollection) o;
    return Objects.equals(this.totalCount, usersCollection.totalCount) &&
        Objects.equals(this.records, usersCollection.records);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalCount, records);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UsersCollection {\n");
    
    sb.append("    totalCount: ").append(toIndentedString(totalCount)).append("\n");
    sb.append("    records: ").append(toIndentedString(records)).append("\n");
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

