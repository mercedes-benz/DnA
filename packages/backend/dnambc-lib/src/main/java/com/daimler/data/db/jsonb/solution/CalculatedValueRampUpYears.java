package com.daimler.data.db.jsonb.solution;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculatedValueRampUpYears implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<DataValueRampUpYear> savings;
	private List<DataValueRampUpYear> revenue;
}
