package com.daimler.data.adapter.hasura;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HasuraUserInfoDto extends HasuraUserInfoRequestDto{

	private String is_logged_in;
}
