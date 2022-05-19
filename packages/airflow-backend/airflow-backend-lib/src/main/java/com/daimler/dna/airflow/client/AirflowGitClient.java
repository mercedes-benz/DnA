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

package com.daimler.dna.airflow.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;

import com.daimler.dna.airflow.dto.AirflowDagUpdateVO;
import com.daimler.dna.airflow.dto.AirflowGITResponse;
import com.daimler.dna.airflow.dto.AirflowProjectUserVO;
import com.daimler.dna.airflow.dto.AirflowProjectVO;
import com.daimler.dna.airflow.exceptions.MessageDescription;

public interface AirflowGitClient {

	/**
	 * <p>
	 * This method exists to be used for create airflow project's DAGs
	 * </p>
	 * 
	 * @param airflowProjectVO
	 * @param currentUser
	 * 
	 * @return {@code List<MessageDescription>} if error exists otherwise
	 *         {@code null}
	 */
	public List<MessageDescription> createAirflowDags(AirflowProjectVO airflowProjectVO,
			AirflowProjectUserVO currentUser);

	/**
	 * <p>
	 * To fetch airflow DAG from airflow GIT repository by given dagId
	 * </p>
	 * 
	 * @param dagId
	 * @return {@code DagContent} if exists otherwise {@code null}
	 */
	public String getDagById(String dagId);

	/**
	 * <p>
	 * This method exists to be used for update airflow project's DAGs if existing
	 * else create a new one
	 * </p>
	 * 
	 * @param airflowProjectVO
	 * @param currentUser
	 * 
	 * @return {@code List<MessageDescription>} if error exists otherwise
	 *         {@code null}
	 */
	public AirflowGITResponse updateAirflowDags(AirflowProjectVO airflowProjectVO, AirflowProjectUserVO currentUser);

	/**
	 * <p>
	 * This method exists to be used for update airflow project's DAG if existing
	 * </p>
	 * 
	 * @param AirflowDagUpdateVO
	 * @param currentUser        {@code AirflowProjectUserVO}
	 * 
	 * @return {@code List<MessageDescription>} if error exists otherwise
	 *         {@code null}
	 */
	public List<MessageDescription> updateAirflowDag(AirflowDagUpdateVO airflowDagUpdateVO,
			AirflowProjectUserVO currentUser);

	/**
	 * <p>
	 * To fetch airflow DAGs content from airflow GIT repository by given dagNames
	 * </p>
	 *
	 * @param dagNames
	 * @return {@code DagContent} if exists otherwise {@code null}
	 */
	public Map<String, String> getDagContentByIds(Set<String> dagNames);

	/**
	 * <p>
	 * To delete a DAG identified by DAG Name
	 * </p>
	 * 
	 * @param dagName
	 * @param currentUser
	 * @return {@code errors} if exists otherwise {@code null}
	 */
	public AirflowGITResponse deleteAirflowDag(String dagName, AirflowProjectUserVO currentUser);

	/**
	 * To rollback GIT commit
	 * 
	 * @param commit
	 * @return void
	 */
	public List<MessageDescription> gitRollback(RevCommit commit);

}
