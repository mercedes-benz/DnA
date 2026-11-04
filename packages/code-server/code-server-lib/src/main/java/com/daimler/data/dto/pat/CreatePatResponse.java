package com.daimler.data.dto.pat;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
public class CreatePatResponse {
	private int version;
	@ToString.Exclude
	private String token;
	private String displaySuffix;
	private List<String> permissions;
	private String comment;
	private String createdAt;
	private int codeSpaceProjectCount;
}
