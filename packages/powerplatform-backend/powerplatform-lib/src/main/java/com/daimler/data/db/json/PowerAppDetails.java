package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PowerAppDetails implements Serializable{

	private static final long serialVersionUID = 1L;
	private String name;
	private String subscriptionType;
	private String envOwnerName;
	private String envOwnerId;
	private String dyEnvOwnerName;
	private String dyEnvOwnerId;
	private String department;
	private String environment;
	private String prodEnvAvailability;
	private String billingContact;
	private String billingPlant;
	private String billingCostCentre;
	private String customRequirements;
	private String state;
	private String comments;
	private String url;
	private UserDetails requestedBy;
	private Date requestedOn; 
	private Date updatedOn; 
	private List<Developer> developers;
}
