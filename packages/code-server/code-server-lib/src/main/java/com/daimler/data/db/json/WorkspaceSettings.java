package com.daimler.data.db.json;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceSettings  implements Serializable{

	private static final long serialVersionUID = 91401808120254047L;
	
	private String ramSize;
	private String cpuCapacity;
	private String operatingSystem;
	
}
