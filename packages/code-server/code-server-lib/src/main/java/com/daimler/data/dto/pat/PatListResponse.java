package com.daimler.data.dto.pat;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PatListResponse {
	private List<PatVaultListItem> data;
	private int count;
}
