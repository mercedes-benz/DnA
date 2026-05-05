package com.daimler.data.dto.fabric;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalUsageInformationDataDto {

    private List<String> legalBasisForProcessingPurposes;
    private String restrictionsOnUsageAndToU;
    private String standardTimeLimitForErasure;
    private String startOfTimeLimit;
}