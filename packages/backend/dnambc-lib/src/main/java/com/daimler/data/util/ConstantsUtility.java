/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.util;

import java.util.HashMap;
import java.util.Map;

public class ConstantsUtility {

	public static final String CHANGE_LOGS = "changeLogs";
	public static final String VALUE_CALCULATOR = "valueCalculator";
	public static final String ID = "id";
	public static final String SOLUTION_DIGITAL_VALUE_VO = "{\"digitalValue\": 0,\"digitalValueComment\": \"\",\"digitalEffort\": 0,\"digitalEffortComment\": \"\",\"maturityLevel\": null,\"projectController\": null,\"costDrivers\": null,\"valueDrivers\": null,\"attachments\": null,\"assessment\": null,\"permissions\": null,\"changeLogs\": null}";
	public static final String SOLUTIONDIGITALVALUEVO_EMPTY = "{\"digitalValue\":0,\"digitalValueComment\":\"\",\"digitalEffort\":0,\"digitalEffortComment\":\"\"}";
	public static final String SOLUTIONDIGITALVALUEVO_EMPTY_1 = "{\"projectControllers\":[],\"attachments\":[],\"assessment\":{\"commentOnStrategicRelevance\":\"\",\"commentOnBenefitRealizationRisk\":\"\"},\"permissions\":[],\"changeLogs\":[],\"valueCalculator\":{\"calculatedValueRampUpYears\":[],\"costFactorSummary\":{},\"valueFactorSummary\":{},\"breakEvenPoint\":0}}";
	public static final String EMPTY_VALUE = "EMPTY";
	public static final String NONE_VALUE = "None";
	public static final String OPEN = "OPEN";
	public static final String DELETED = "DELETED";
	public static final String SOLUTION_MDM=  "Solution MDM";

	// S3 Path to upload logo
	public static final String S3_PATH_TO_UPLOAD_LOGO = "logos/";

	public static final Map<String, String> staticMap = new HashMap<>();
	static {
		staticMap.put("costDrivers", "Cost Driver");
		staticMap.put("valueDrivers", "Value Driver");
		staticMap.put("rampUp", "Ramp-Up");
		staticMap.put("attachments", "Attached Files");
		staticMap.put("assessment", "Strategy & Risk Assessment");
		staticMap.put("permissions", "Share/Permission");
		staticMap.put("projectController", "Project Controller");

		staticMap.put("digitalValue", "Digital Value");
		staticMap.put("digitalValueComment", "Comment");
		staticMap.put("digitalEffort", "Digital Effort");
		staticMap.put("maturityLevel", "Maturity Level");

		staticMap.put("description", "Description");
		staticMap.put("category", "Category");
		staticMap.put("value", "Value");
		staticMap.put("source", "Source");
		staticMap.put("year", "Year");
		staticMap.put("percent", "Percent");

	}

}
