package com.daimler.data.auth.client;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoVO implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String firstName;
	private String lastName;
	private String department;
	private String email;
	private String mobileNumber;
	private List<UserFavoriteUseCaseVO> favoriteUsecases;
	private String token;
	private List<UserRoleVO> roles;
	private List<String> divisionAdmins;

}
