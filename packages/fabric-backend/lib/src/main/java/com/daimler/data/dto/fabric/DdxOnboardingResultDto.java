package com.daimler.data.dto.fabric;

import com.daimler.data.controller.exceptions.GenericMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DdxOnboardingResultDto {

    private GenericMessage responseMessage;
    private DdxResponseDto ddxResponse;
}
