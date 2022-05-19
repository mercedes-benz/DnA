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

package com.daimler.dna.airflow.service;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;

import com.daimler.dna.airflow.dto.AirflowDagRetryRequestVO;
import com.daimler.dna.airflow.dto.AirflowDagRetryResponseVO;
import com.daimler.dna.airflow.dto.AirflowDagUpdateResponseVO;
import com.daimler.dna.airflow.dto.AirflowDagUpdateVO;
import com.daimler.dna.airflow.dto.AirflowRetryDagVo;
import com.daimler.dna.airflow.exceptions.GenericMessage;

public interface DagMgmtService {

	public ResponseEntity<AirflowDagUpdateResponseVO> updateAirflowDag(AirflowDagUpdateVO dag);

	/**
	 * <p>
	 * To delete DAG identified by DAG name
	 * </p>
	 * 
	 * @param dagName
	 * @param projectId
	 * @return ResponseEntity<GenericMessage>
	 */
	public ResponseEntity<GenericMessage> deleteDag(String dagName, String projectId);

	public AirflowRetryDagVo getPermission(String dagName, String projectId);

	public ResponseEntity<AirflowDagRetryResponseVO> updatePermission(String dagName, String projectId,
			AirflowRetryDagVo airflowRetryDagVo);
}
