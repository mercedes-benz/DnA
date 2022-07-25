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
 * UserFavoriteUseCaseVO
 */
@Validated


public class UserFavoriteUseCaseVO   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("usecaseId")
  private String usecaseId = null;

  public UserFavoriteUseCaseVO id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Unique ID of this favorite
   * @return id
  **/
  @ApiModelProperty(value = "Unique ID of this favorite")


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public UserFavoriteUseCaseVO usecaseId(String usecaseId) {
    this.usecaseId = usecaseId;
    return this;
  }

  /**
   * Solution ID that the user would mark as favorite
   * @return usecaseId
  **/
  @ApiModelProperty(value = "Solution ID that the user would mark as favorite")


  public String getUsecaseId() {
    return usecaseId;
  }

  public void setUsecaseId(String usecaseId) {
    this.usecaseId = usecaseId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserFavoriteUseCaseVO userFavoriteUseCaseVO = (UserFavoriteUseCaseVO) o;
    return Objects.equals(this.id, userFavoriteUseCaseVO.id) &&
        Objects.equals(this.usecaseId, userFavoriteUseCaseVO.usecaseId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, usecaseId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserFavoriteUseCaseVO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    usecaseId: ").append(toIndentedString(usecaseId)).append("\n");
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

