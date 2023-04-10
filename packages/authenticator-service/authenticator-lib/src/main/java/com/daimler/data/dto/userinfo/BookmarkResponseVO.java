package com.daimler.data.dto.userinfo;

import java.util.Objects;
import com.daimler.data.controller.exceptions.MessageDescription;
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
 * BookmarkResponseVO
 */
@Validated


public class BookmarkResponseVO   {
  @JsonProperty("data")
  private UserInfoVO data = null;

  @JsonProperty("errors")
  @Valid
  private List<MessageDescription> errors = null;

  public BookmarkResponseVO data(UserInfoVO data) {
    this.data = data;
    return this;
  }

  /**
   * Get data
   * @return data
  **/
  @ApiModelProperty(value = "")

  @Valid

  public UserInfoVO getData() {
    return data;
  }

  public void setData(UserInfoVO data) {
    this.data = data;
  }

  public BookmarkResponseVO errors(List<MessageDescription> errors) {
    this.errors = errors;
    return this;
  }

  public BookmarkResponseVO addErrorsItem(MessageDescription errorsItem) {
    if (this.errors == null) {
      this.errors = new ArrayList<MessageDescription>();
    }
    this.errors.add(errorsItem);
    return this;
  }

  /**
   * Get errors
   * @return errors
  **/
  @ApiModelProperty(value = "")

  @Valid

  public List<MessageDescription> getErrors() {
    return errors;
  }

  public void setErrors(List<MessageDescription> errors) {
    this.errors = errors;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BookmarkResponseVO bookmarkResponseVO = (BookmarkResponseVO) o;
    return Objects.equals(this.data, bookmarkResponseVO.data) &&
        Objects.equals(this.errors, bookmarkResponseVO.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, errors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BookmarkResponseVO {\n");
    
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
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

