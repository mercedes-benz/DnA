package com.daimler.data.dto.auth;

import java.util.Objects;
import com.daimler.data.dto.auth.ApiKeyValidationVO;
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
 * ApiKeyValidationResponseVO
 */
@Validated

public class ApiKeyValidationResponseVO   {
    @JsonProperty("validApiKey")
    private Boolean validApiKey = null;

    @JsonProperty("data")
    private ApiKeyValidationVO data = null;

    @JsonProperty("errors")
    @Valid
    private List<com.daimler.data.controller.exceptions.MessageDescription> errors = null;

    public ApiKeyValidationResponseVO validApiKey(Boolean validApiKey) {
        this.validApiKey = validApiKey;
        return this;
    }

    /**
     * if given api key is valid or not
     * @return validApiKey
     **/
    @ApiModelProperty(value = "if given api key is valid or not")


    public Boolean isValidApiKey() {
        return validApiKey;
    }

    public void setValidApiKey(Boolean validApiKey) {
        this.validApiKey = validApiKey;
    }

    public ApiKeyValidationResponseVO data(ApiKeyValidationVO data) {
        this.data = data;
        return this;
    }

    /**
     * Get data
     * @return data
     **/
    @ApiModelProperty(value = "")

    @Valid

    public ApiKeyValidationVO getData() {
        return data;
    }

    public void setData(ApiKeyValidationVO data) {
        this.data = data;
    }

    public ApiKeyValidationResponseVO errors(List<com.daimler.data.controller.exceptions.MessageDescription> errors) {
        this.errors = errors;
        return this;
    }

    public ApiKeyValidationResponseVO addErrorsItem(com.daimler.data.controller.exceptions.MessageDescription errorsItem) {
        if (this.errors == null) {
            this.errors = new ArrayList<com.daimler.data.controller.exceptions.MessageDescription>();
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

    public List<com.daimler.data.controller.exceptions.MessageDescription> getErrors() {
        return errors;
    }

    public void setErrors(List<com.daimler.data.controller.exceptions.MessageDescription> errors) {
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
        ApiKeyValidationResponseVO apiKeyValidationResponseVO = (ApiKeyValidationResponseVO) o;
        return Objects.equals(this.validApiKey, apiKeyValidationResponseVO.validApiKey) &&
                Objects.equals(this.data, apiKeyValidationResponseVO.data) &&
                Objects.equals(this.errors, apiKeyValidationResponseVO.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(validApiKey, data, errors);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ApiKeyValidationResponseVO {\n");

        sb.append("    validApiKey: ").append(toIndentedString(validApiKey)).append("\n");
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

