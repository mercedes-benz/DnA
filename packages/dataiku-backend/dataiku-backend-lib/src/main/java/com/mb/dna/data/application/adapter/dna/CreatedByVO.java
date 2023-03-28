package com.mb.dna.data.application.adapter.dna;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class CreatedByVO implements Serializable {
	
  private String id;
  private String firstName;
  private String lastName;
  private String department;
  private String email;
  private String mobileNumber;

}

