package com.daimler.data.dto;

import java.io.Serializable;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class JupyterHubCreateUserDTO implements Serializable{

    private boolean admin;
	private List<String> usernames;

}