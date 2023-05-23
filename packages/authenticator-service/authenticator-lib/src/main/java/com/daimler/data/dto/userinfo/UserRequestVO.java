package com.daimler.data.dto.userinfo;

import java.util.Objects;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * UserRequestVO
 */
@Validated


public class UserRequestVO   {
  @JsonProperty("data")
  private UserInfoVO data = null;

  public UserRequestVO data(UserInfoVO data) {
    this.data = data;
    return this;
  }

  /**
   * Request data containing user details to be created/updated
   * @return data
  **/
  @ApiModelProperty(value = "Request data containing user details to be created/updated")

  @Valid

  public UserInfoVO getData() {
    return data;
  }

  public void setData(UserInfoVO data) {
    this.data = data;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserRequestVO userRequestVO = (UserRequestVO) o;
    return Objects.equals(this.data, userRequestVO.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserRequestVO {\n");
    
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
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

