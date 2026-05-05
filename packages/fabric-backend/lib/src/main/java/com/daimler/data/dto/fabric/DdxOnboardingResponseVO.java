package com.daimler.data.dto.fabric;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DdxOnboardingResponseVO {

    private DdxResponseDto data;
    private GenericMessage responses;
}
