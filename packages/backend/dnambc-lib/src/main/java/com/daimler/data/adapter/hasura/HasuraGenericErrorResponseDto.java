package com.daimler.data.adapter.hasura;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HasuraGenericErrorResponseDto {
	private String code;
	private String error;
	private String path;

}
