package com.mb.dna.kube.client.main;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NamespaceMetadataDto {

	public List<String> pods;
	public String node;
}
