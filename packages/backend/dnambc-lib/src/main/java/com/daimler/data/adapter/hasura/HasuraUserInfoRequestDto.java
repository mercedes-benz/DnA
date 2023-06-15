package com.daimler.data.adapter.hasura;

import com.daimler.data.db.jsonb.UserInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HasuraUserInfoRequestDto {
	
	private String id;
	private UserInfo data;


}
