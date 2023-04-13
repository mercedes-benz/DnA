package com.daimler.data.adapter.hasura;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HasuraUserInfoInsertResponseContentDto {

	private int affected_rows;
	private List<HasuraUserInfoDto> returning;
	
}
