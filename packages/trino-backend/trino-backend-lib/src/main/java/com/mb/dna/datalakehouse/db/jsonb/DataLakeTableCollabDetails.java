package com.mb.dna.datalakehouse.db.jsonb;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataLakeTableCollabDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	private UserInfo collaborator;
	private Boolean hasWritePermission;
}
