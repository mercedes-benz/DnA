package com.daimler.data.dto.fabric;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * LeanIXDetailsVO
 */
@Validated
public class LeanIXDetailsVO {
  @JsonProperty("appReferenceStr")
  private String appReferenceStr = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("shortName")
  private String shortName = null;

  @JsonProperty("objectState")
  private String objectState = null;

  @JsonProperty("providerOrgRefstr")
  private String providerOrgRefstr = null;

  @JsonProperty("providerOrgId")
  private String providerOrgId = null;

  @JsonProperty("providerOrgShortname")
  private String providerOrgShortname = null;

  @JsonProperty("providerOrgDeptid")
  private String providerOrgDeptid = null;

  public LeanIXDetailsVO appReferenceStr(String appReferenceStr) {
    this.appReferenceStr = appReferenceStr;
    return this;
  }

  /**
   * Get appReferenceStr
   * @return appReferenceStr
  **/
  @ApiModelProperty(value = "")
  public String getAppReferenceStr() {
    return appReferenceStr;
  }

  public void setAppReferenceStr(String appReferenceStr) {
    this.appReferenceStr = appReferenceStr;
  }

  public LeanIXDetailsVO name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Name of the LeanIX
   * @return name
  **/
  @ApiModelProperty(value = "Name of the LeanIX")
  @Size(min=1)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LeanIXDetailsVO shortName(String shortName) {
    this.shortName = shortName;
    return this;
  }

  /**
   * Short Name of the LeanIX
   * @return shortName
  **/
  @ApiModelProperty(value = "Short Name of the LeanIX")
  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public LeanIXDetailsVO objectState(String objectState) {
    this.objectState = objectState;
    return this;
  }

  /**
   * State of the LeanIX
   * @return objectState
  **/
  @ApiModelProperty(value = "State of the LeanIX")
  public String getObjectState() {
    return objectState;
  }

  public void setObjectState(String objectState) {
    this.objectState = objectState;
  }

  public LeanIXDetailsVO providerOrgRefstr(String providerOrgRefstr) {
    this.providerOrgRefstr = providerOrgRefstr;
    return this;
  }

  /**
   * Details of provider Org of the LeanIX
   * @return providerOrgRefstr
  **/
  @ApiModelProperty(value = "Details of provider Org of the LeanIX")
  public String getProviderOrgRefstr() {
    return providerOrgRefstr;
  }

  public void setProviderOrgRefstr(String providerOrgRefstr) {
    this.providerOrgRefstr = providerOrgRefstr;
  }

  public LeanIXDetailsVO providerOrgId(String providerOrgId) {
    this.providerOrgId = providerOrgId;
    return this;
  }

  /**
   * Get providerOrgId
   * @return providerOrgId
  **/
  @ApiModelProperty(value = "")
  public String getProviderOrgId() {
    return providerOrgId;
  }

  public void setProviderOrgId(String providerOrgId) {
    this.providerOrgId = providerOrgId;
  }

  public LeanIXDetailsVO providerOrgShortname(String providerOrgShortname) {
    this.providerOrgShortname = providerOrgShortname;
    return this;
  }

  /**
   * Get providerOrgShortname
   * @return providerOrgShortname
  **/
  @ApiModelProperty(value = "")
  public String getProviderOrgShortname() {
    return providerOrgShortname;
  }

  public void setProviderOrgShortname(String providerOrgShortname) {
    this.providerOrgShortname = providerOrgShortname;
  }

  public LeanIXDetailsVO providerOrgDeptid(String providerOrgDeptid) {
    this.providerOrgDeptid = providerOrgDeptid;
    return this;
  }

  /**
   * Get providerOrgDeptid
   * @return providerOrgDeptid
  **/
  @ApiModelProperty(value = "")
  public String getProviderOrgDeptid() {
    return providerOrgDeptid;
  }

  public void setProviderOrgDeptid(String providerOrgDeptid) {
    this.providerOrgDeptid = providerOrgDeptid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LeanIXDetailsVO leanIXDetailsVO = (LeanIXDetailsVO) o;
    return Objects.equals(this.appReferenceStr, leanIXDetailsVO.appReferenceStr) &&
        Objects.equals(this.name, leanIXDetailsVO.name) &&
        Objects.equals(this.shortName, leanIXDetailsVO.shortName) &&
        Objects.equals(this.objectState, leanIXDetailsVO.objectState) &&
        Objects.equals(this.providerOrgRefstr, leanIXDetailsVO.providerOrgRefstr) &&
        Objects.equals(this.providerOrgId, leanIXDetailsVO.providerOrgId) &&
        Objects.equals(this.providerOrgShortname, leanIXDetailsVO.providerOrgShortname) &&
        Objects.equals(this.providerOrgDeptid, leanIXDetailsVO.providerOrgDeptid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(appReferenceStr, name, shortName, objectState, providerOrgRefstr, providerOrgId, providerOrgShortname, providerOrgDeptid);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LeanIXDetailsVO {\n");
    
    sb.append("    appReferenceStr: ").append(toIndentedString(appReferenceStr)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    shortName: ").append(toIndentedString(shortName)).append("\n");
    sb.append("    objectState: ").append(toIndentedString(objectState)).append("\n");
    sb.append("    providerOrgRefstr: ").append(toIndentedString(providerOrgRefstr)).append("\n");
    sb.append("    providerOrgId: ").append(toIndentedString(providerOrgId)).append("\n");
    sb.append("    providerOrgShortname: ").append(toIndentedString(providerOrgShortname)).append("\n");
    sb.append("    providerOrgDeptid: ").append(toIndentedString(providerOrgDeptid)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}