package com.daimler.data.dto.userinfo;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * UserRoleVO
 */
@Validated


public class UserRoleVO   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("name")
  private String name = null;

  public UserRoleVO id(String id) {
    this.id = id;
    return this;
  }

  /**
   * ID of User Role
   * @return id
  **/
  @ApiModelProperty(value = "ID of User Role")


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public UserRoleVO name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Name of the User Role
   * @return name
  **/
  @ApiModelProperty(value = "Name of the User Role")

@Pattern(regexp="[a-zA-Z\\s]+") @Size(min=1) 
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserRoleVO userRoleVO = (UserRoleVO) o;
    return Objects.equals(this.id, userRoleVO.id) &&
        Objects.equals(this.name, userRoleVO.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserRoleVO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
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

