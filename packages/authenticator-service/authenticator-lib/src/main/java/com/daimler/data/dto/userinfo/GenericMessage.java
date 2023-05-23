package com.daimler.data.dto.userinfo;

import java.util.Objects;
import com.daimler.data.controller.exceptions.MessageDescription;
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
 * GenericMessage
 */
@Validated


public class GenericMessage   {
  @JsonProperty("success")
  private String success = null;

  @JsonProperty("errors")
  @Valid
  private List<MessageDescription> errors = null;

  @JsonProperty("warnings")
  @Valid
  private List<MessageDescription> warnings = null;

  public GenericMessage success(String success) {
    this.success = success;
    return this;
  }

  /**
   * Get success
   * @return success
  **/
  @ApiModelProperty(value = "")


  public String getSuccess() {
    return success;
  }

  public void setSuccess(String success) {
    this.success = success;
  }

  public GenericMessage errors(List<MessageDescription> errors) {
    this.errors = errors;
    return this;
  }

  public GenericMessage addErrorsItem(MessageDescription errorsItem) {
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

  public GenericMessage warnings(List<MessageDescription> warnings) {
    this.warnings = warnings;
    return this;
  }

  public GenericMessage addWarningsItem(MessageDescription warningsItem) {
    if (this.warnings == null) {
      this.warnings = new ArrayList<MessageDescription>();
    }
    this.warnings.add(warningsItem);
    return this;
  }

  /**
   * Get warnings
   * @return warnings
  **/
  @ApiModelProperty(value = "")

  @Valid

  public List<MessageDescription> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<MessageDescription> warnings) {
    this.warnings = warnings;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GenericMessage genericMessage = (GenericMessage) o;
    return Objects.equals(this.success, genericMessage.success) &&
        Objects.equals(this.errors, genericMessage.errors) &&
        Objects.equals(this.warnings, genericMessage.warnings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, errors, warnings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GenericMessage {\n");
    
    sb.append("    success: ").append(toIndentedString(success)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
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

