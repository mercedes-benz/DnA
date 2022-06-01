package com.daimler.data.dto.usernotificationpref;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * NotificationPreferenceVO
 */
@Validated


public class NotificationPreferenceVO   {
  @JsonProperty("enableAppNotifications")
  private Boolean enableAppNotifications = null;

  @JsonProperty("enableEmailNotifications")
  private Boolean enableEmailNotifications = null;

  public NotificationPreferenceVO enableAppNotifications(Boolean enableAppNotifications) {
    this.enableAppNotifications = enableAppNotifications;
    return this;
  }

  /**
   * Enable app notifications or not. By default all users would have this enabled.
   * @return enableAppNotifications
  **/
  @ApiModelProperty(value = "Enable app notifications or not. By default all users would have this enabled.")


  public Boolean isEnableAppNotifications() {
    return enableAppNotifications;
  }

  public void setEnableAppNotifications(Boolean enableAppNotifications) {
    this.enableAppNotifications = enableAppNotifications;
  }

  public NotificationPreferenceVO enableEmailNotifications(Boolean enableEmailNotifications) {
    this.enableEmailNotifications = enableEmailNotifications;
    return this;
  }

  /**
   * Enable email notifications or not. By default all users would have this disabled.
   * @return enableEmailNotifications
  **/
  @ApiModelProperty(value = "Enable email notifications or not. By default all users would have this disabled.")


  public Boolean isEnableEmailNotifications() {
    return enableEmailNotifications;
  }

  public void setEnableEmailNotifications(Boolean enableEmailNotifications) {
    this.enableEmailNotifications = enableEmailNotifications;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotificationPreferenceVO notificationPreferenceVO = (NotificationPreferenceVO) o;
    return Objects.equals(this.enableAppNotifications, notificationPreferenceVO.enableAppNotifications) &&
        Objects.equals(this.enableEmailNotifications, notificationPreferenceVO.enableEmailNotifications);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enableAppNotifications, enableEmailNotifications);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NotificationPreferenceVO {\n");
    
    sb.append("    enableAppNotifications: ").append(toIndentedString(enableAppNotifications)).append("\n");
    sb.append("    enableEmailNotifications: ").append(toIndentedString(enableEmailNotifications)).append("\n");
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

