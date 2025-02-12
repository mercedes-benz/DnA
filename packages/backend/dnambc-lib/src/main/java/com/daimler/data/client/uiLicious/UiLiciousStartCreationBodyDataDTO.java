package com.daimler.data.client.uiLicious;

 import lombok.AllArgsConstructor;
 import lombok.Data;
 import lombok.NoArgsConstructor;

 import org.springframework.http.HttpStatus;
 
 @Data
 @NoArgsConstructor
 @AllArgsConstructor
public class UiLiciousStartCreationBodyDataDTO {
    private String userid;
    private String password;
    private String orgName;
    private String projectName;
    private String memberMailId;
}
