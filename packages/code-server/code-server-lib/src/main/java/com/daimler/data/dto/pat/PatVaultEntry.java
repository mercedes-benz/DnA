package com.daimler.data.dto.pat;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class PatVaultEntry {
	@ToString.Exclude
	private String secret;
	private String displaySuffix;
	private String comment;
	private String createdAt;
	private List<String> codeSpaceProjectNames;
	private List<String> permissions;
}
