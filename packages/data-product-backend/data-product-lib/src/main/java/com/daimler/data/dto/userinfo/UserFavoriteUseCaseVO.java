/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

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

