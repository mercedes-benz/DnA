package com.daimler.data.db.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RunState {

	private String life_cycle_state;
	private String result_state;
	private String state_message;
	private Boolean user_cancelled_or_timedout;
	
}
