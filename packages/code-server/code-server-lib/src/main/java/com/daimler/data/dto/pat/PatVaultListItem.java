package com.daimler.data.dto.pat;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PatVaultListItem {
	private int version;
	private String displaySuffix;
	private String status;
	private String comment;
	private String createdAt;
	private int codeSpaceProjectCount;
	private List<String> permissions;
}
