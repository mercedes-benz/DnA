package com.daimler.data.dto.auth;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ApiKeyValidationVO
 */
@Validated

public class ApiKeyValidationVO   {
    @JsonProperty("appId")
    private String appId = null;

    @JsonProperty("apiKey")
    private String apiKey = null;

    public ApiKeyValidationVO appId(String appId) {
        this.appId = appId;
        return this;
    }

    /**
     * Id of the Application
     * @return appId
     **/
    @ApiModelProperty(required = true, value = "Id of the Application")
    @NotNull


    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public ApiKeyValidationVO apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * generated apiKey
     * @return apiKey
     **/
    @ApiModelProperty(required = true, value = "generated apiKey")
    @NotNull


    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiKeyValidationVO apiKeyValidationVO = (ApiKeyValidationVO) o;
        return Objects.equals(this.appId, apiKeyValidationVO.appId) &&
                Objects.equals(this.apiKey, apiKeyValidationVO.apiKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, apiKey);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ApiKeyValidationVO {\n");

        sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
        sb.append("    apiKey: ").append(toIndentedString(apiKey)).append("\n");
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



