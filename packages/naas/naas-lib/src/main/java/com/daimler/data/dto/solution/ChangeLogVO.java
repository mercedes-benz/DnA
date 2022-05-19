package com.daimler.data.dto.solution;

import java.util.Objects;
import com.daimler.data.dto.solution.TeamMemberVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Describes a single change log record done on the solution
 */
@ApiModel(description = "Describes a single change log record done on the solution")
@Validated


public class ChangeLogVO   {
  @JsonProperty("changeDate")
  private Date changeDate = null;

  @JsonProperty("modifiedBy")
  private TeamMemberVO modifiedBy = null;

  @JsonProperty("fieldChanged")
  private String fieldChanged = null;

  @JsonProperty("oldValue")
  private String oldValue = null;

  @JsonProperty("newValue")
  private String newValue = null;

  @JsonProperty("changeDescription")
  private String changeDescription = null;

  public ChangeLogVO changeDate(Date changeDate) {
    this.changeDate = changeDate;
    return this;
  }

  /**
   * Date on which this change occurred
   * @return changeDate
  **/
  @ApiModelProperty(value = "Date on which this change occurred")

  @Valid

  public Date getChangeDate() {
    return changeDate;
  }

  public void setChangeDate(Date changeDate) {
    this.changeDate = changeDate;
  }

  public ChangeLogVO modifiedBy(TeamMemberVO modifiedBy) {
    this.modifiedBy = modifiedBy;
    return this;
  }

  /**
   * User who changed the value
   * @return modifiedBy
  **/
  @ApiModelProperty(value = "User who changed the value")

  @Valid

  public TeamMemberVO getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(TeamMemberVO modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public ChangeLogVO fieldChanged(String fieldChanged) {
    this.fieldChanged = fieldChanged;
    return this;
  }

  /**
   * Describe the attribute that changed
   * @return fieldChanged
  **/
  @ApiModelProperty(value = "Describe the attribute that changed")


  public String getFieldChanged() {
    return fieldChanged;
  }

  public void setFieldChanged(String fieldChanged) {
    this.fieldChanged = fieldChanged;
  }

  public ChangeLogVO oldValue(String oldValue) {
    this.oldValue = oldValue;
    return this;
  }

  /**
   * Describes the old value of the changed attribute
   * @return oldValue
  **/
  @ApiModelProperty(value = "Describes the old value of the changed attribute")


  public String getOldValue() {
    return oldValue;
  }

  public void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  public ChangeLogVO newValue(String newValue) {
    this.newValue = newValue;
    return this;
  }

  /**
   * Describes the new value of the changed attribute
   * @return newValue
  **/
  @ApiModelProperty(value = "Describes the new value of the changed attribute")


  public String getNewValue() {
    return newValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public ChangeLogVO changeDescription(String changeDescription) {
    this.changeDescription = changeDescription;
    return this;
  }

  /**
   * Short Description on change/addition/removal of Digital Value
   * @return changeDescription
  **/
  @ApiModelProperty(value = "Short Description on change/addition/removal of Digital Value")


  public String getChangeDescription() {
    return changeDescription;
  }

  public void setChangeDescription(String changeDescription) {
    this.changeDescription = changeDescription;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChangeLogVO changeLogVO = (ChangeLogVO) o;
    return Objects.equals(this.changeDate, changeLogVO.changeDate) &&
        Objects.equals(this.modifiedBy, changeLogVO.modifiedBy) &&
        Objects.equals(this.fieldChanged, changeLogVO.fieldChanged) &&
        Objects.equals(this.oldValue, changeLogVO.oldValue) &&
        Objects.equals(this.newValue, changeLogVO.newValue) &&
        Objects.equals(this.changeDescription, changeLogVO.changeDescription);
  }

  @Override
  public int hashCode() {
    return Objects.hash(changeDate, modifiedBy, fieldChanged, oldValue, newValue, changeDescription);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ChangeLogVO {\n");
    
    sb.append("    changeDate: ").append(toIndentedString(changeDate)).append("\n");
    sb.append("    modifiedBy: ").append(toIndentedString(modifiedBy)).append("\n");
    sb.append("    fieldChanged: ").append(toIndentedString(fieldChanged)).append("\n");
    sb.append("    oldValue: ").append(toIndentedString(oldValue)).append("\n");
    sb.append("    newValue: ").append(toIndentedString(newValue)).append("\n");
    sb.append("    changeDescription: ").append(toIndentedString(changeDescription)).append("\n");
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

