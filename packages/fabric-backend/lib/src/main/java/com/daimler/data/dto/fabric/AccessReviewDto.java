package com.daimler.data.dto.fabric;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.daimler.data.dto.fabric.ReviewerConfigDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessReviewDto implements Serializable{

	private static final long serialVersionUID = 1L;

    private boolean enabled;
    private String startDate;
    private List<ReviewerConfigDto> reviewerConfigs;

}
