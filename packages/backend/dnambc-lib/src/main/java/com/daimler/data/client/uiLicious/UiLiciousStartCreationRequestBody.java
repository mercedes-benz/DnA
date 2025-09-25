package com.daimler.data.client.uiLicious;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.daimler.data.client.uiLicious.UiLiciousStartCreationBodyDataDTO;

import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UiLiciousStartCreationRequestBody {
    private String filePath;
    private String projectID;
    private String browser;
    private String width;
    private String height;
    private UiLiciousStartCreationBodyDataDTO data;
}
