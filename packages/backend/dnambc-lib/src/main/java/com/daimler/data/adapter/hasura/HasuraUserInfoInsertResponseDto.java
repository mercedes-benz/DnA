package com.daimler.data.adapter.hasura;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HasuraUserInfoInsertResponseDto extends HasuraGenericErrorResponseDto {
	private HasuraUserInfoInsertResponseContentDto insert_userinfo_nsql;

}
