package com.daimler.data.db.jsonb.solution;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataValueCalculator implements Serializable {

	private static final long serialVersionUID = 1L;
	private CalculatedValueRampUpYears calculatedDataValueRampUpYears;
	private ValueFactorSummary savingsValueFactorSummary;
	private ValueFactorSummary revenueValueFactorSummary;
	  		
}
