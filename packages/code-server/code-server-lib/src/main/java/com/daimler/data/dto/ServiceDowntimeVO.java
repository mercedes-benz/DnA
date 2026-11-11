package com.daimler.data.dto;

import lombok.Data;

@Data
public class ServiceDowntimeVO {

    private String id;
    private String serviceName;
    private String message;
    private String detectedAt;
}
