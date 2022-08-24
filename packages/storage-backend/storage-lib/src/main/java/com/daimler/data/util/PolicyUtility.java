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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.daimler.data.dto.PolicyDTO;
import com.daimler.data.dto.PolicyStatementDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class PolicyUtility {

	//Minio Policy generator
	public static String policyGenerator(String resource, String version, String action, String sid, String effect)
			throws JsonProcessingException {
		String policy = "";
		List<PolicyStatementDTO> statements = new ArrayList<PolicyStatementDTO>();

		PolicyStatementDTO statement = new PolicyStatementDTO();
		statement.setAction(Arrays.asList(action.split("\\s*,\\s*")));
		statement.setEffect(effect);
		statement.setResource(Arrays.asList(resource.split("\\s*,\\s*")));
		statement.setSid(sid);

		statements.add(statement);
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

		policy = ow.writeValueAsString(new PolicyDTO(version, statements));

		return policy;
	}
	
}
