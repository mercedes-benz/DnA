package com.mb.dna.data.application.adapter.dna;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo implements Serializable{
	
	private String id;
	private String firstName;
	private String lastName;
	private String email;
	private String mobileNumber;
	private String department;

	private String sub;
	private boolean email_verified;
	private String name;
	private String given_name;
	private String family_name;
	private String personal_data_hint;
	private String updated_at;
	private List<UserRole> userRole;

	public boolean hasAdminAccess() {
		return this.getUserRole().stream().anyMatch(
				n -> "DataComplianceAdmin".equalsIgnoreCase(n.getName()) || "Admin".equalsIgnoreCase(n.getName()));
	}

}