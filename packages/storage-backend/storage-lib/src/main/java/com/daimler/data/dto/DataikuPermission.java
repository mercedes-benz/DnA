package com.daimler.data.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission object
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataikuPermission   {
  
	private String owner;

  private List<Permission> permissions = null;
}

