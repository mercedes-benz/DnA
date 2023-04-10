package com.daimler.data.dto.userinfo;

import java.util.Objects;
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
 * BookmarkRequestVO
 */
@Validated


public class BookmarkRequestVO   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("deleteBookmark")
  private Boolean deleteBookmark = null;

  @JsonProperty("favoriteUsecases")
  @Valid
  private List<String> favoriteUsecases = null;

  public BookmarkRequestVO id(String id) {
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

  public BookmarkRequestVO deleteBookmark(Boolean deleteBookmark) {
    this.deleteBookmark = deleteBookmark;
    return this;
  }

  /**
   * Flag if true will delete the bookmarks sent in favoriteUsecases array. If nothing is sent then it defaults to add.
   * @return deleteBookmark
  **/
  @ApiModelProperty(value = "Flag if true will delete the bookmarks sent in favoriteUsecases array. If nothing is sent then it defaults to add.")


  public Boolean isDeleteBookmark() {
    return deleteBookmark;
  }

  public void setDeleteBookmark(Boolean deleteBookmark) {
    this.deleteBookmark = deleteBookmark;
  }

  public BookmarkRequestVO favoriteUsecases(List<String> favoriteUsecases) {
    this.favoriteUsecases = favoriteUsecases;
    return this;
  }

  public BookmarkRequestVO addFavoriteUsecasesItem(String favoriteUsecasesItem) {
    if (this.favoriteUsecases == null) {
      this.favoriteUsecases = new ArrayList<String>();
    }
    this.favoriteUsecases.add(favoriteUsecasesItem);
    return this;
  }

  /**
   * Get favoriteUsecases
   * @return favoriteUsecases
  **/
  @ApiModelProperty(value = "")


  public List<String> getFavoriteUsecases() {
    return favoriteUsecases;
  }

  public void setFavoriteUsecases(List<String> favoriteUsecases) {
    this.favoriteUsecases = favoriteUsecases;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BookmarkRequestVO bookmarkRequestVO = (BookmarkRequestVO) o;
    return Objects.equals(this.id, bookmarkRequestVO.id) &&
        Objects.equals(this.deleteBookmark, bookmarkRequestVO.deleteBookmark) &&
        Objects.equals(this.favoriteUsecases, bookmarkRequestVO.favoriteUsecases);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, deleteBookmark, favoriteUsecases);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BookmarkRequestVO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    deleteBookmark: ").append(toIndentedString(deleteBookmark)).append("\n");
    sb.append("    favoriteUsecases: ").append(toIndentedString(favoriteUsecases)).append("\n");
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

