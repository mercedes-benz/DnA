package com.daimler.data.adapter.hasura;



import org.springframework.http.HttpStatusss;

import com.daimler.data.dto.userinfo.UserInfoVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HasuraUserInfoInsertGenericResponse extends HasuraGenericErrorResponseDto {
	
	private HttpStatus status;
	private String errorMessage;
	private UserInfoVO userInfoVO;
}
