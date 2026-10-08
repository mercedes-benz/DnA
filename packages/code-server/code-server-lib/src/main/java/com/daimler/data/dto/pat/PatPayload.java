package com.daimler.data.dto.pat;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class PatPayload {
	private String userId;
	private int tokenVersion;
	@ToString.Exclude
	private String secret;
	private List<String> codeSpaceProjectNames;
	private List<String> permissions;
	private String comment;
	private String createdAt;
	private String expiresAt;
}
