package com.daimler.data.dto.fabric;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkConnectionResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean allowConnectionUsageInGateway;
    private Boolean allowUsageInUserControlledCode;
    private String id;
    private String displayName;
    private String connectivityType;
    private ConnectionResponseDetailsDto connectionDetails;
    private String privacyLevel;
    private CredentialResponseDetailsDto credentialDetails;
    private ConnectionRecencyDto connectionRecency;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConnectionResponseDetailsDto implements Serializable {

        private static final long serialVersionUID = 1L;

        private String path;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CredentialResponseDetailsDto implements Serializable {

        private static final long serialVersionUID = 1L;

        private String credentialType;
        private String singleSignOnType;
        private String connectionEncryption;
        private Boolean skipTestConnection;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConnectionRecencyDto implements Serializable {

        private static final long serialVersionUID = 1L;

        private String createdDateTime;
        private String lastBoundDateTime;
        private String lastCredentialUsedDateTime;
        private String myLastBoundDateTime;
        private String myLastCredentialUsedDateTime;
    }
}