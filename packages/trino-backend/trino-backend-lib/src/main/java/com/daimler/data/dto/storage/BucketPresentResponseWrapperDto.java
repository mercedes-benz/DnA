package com.daimler.data.dto.storage;

import com.daimler.data.controller.exceptions.MessageDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BucketPresentResponseWrapperDto {
    private static final long serialVersionUID = 1L;
    private BucketPresentRequestDto data;
    private String isBucketPresent;
    private List<MessageDescription> success;
    private List<MessageDescription> errors;
    private List<MessageDescription> warnings;
}
