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

package com.daimler.data.db.repo.planningit;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.PlanningITNsql;
import com.daimler.data.db.jsonb.PlanningIT;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class PlanningITCustomRepositoryImpl extends CommonDataRepositoryImpl<PlanningITNsql, String>
		implements PlanningITCustomRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(PlanningITCustomRepositoryImpl.class);

	@Value("${planningit.defaultlimit}")
	private String planningITDefaultLimit;
	
	@Override
	public List<PlanningITNsql> getAllWithFilters(String searchTerm) {
		int limit = Integer.parseInt(planningITDefaultLimit);
		String getAllStmt = " select cast(id as text), cast(data as text) from planningit_nsql ";
		if(searchTerm!= null && !"".equalsIgnoreCase(searchTerm)) {
			searchTerm = "'%"+searchTerm.toLowerCase()+"%'";
			getAllStmt+= "where (" + "lower(jsonb_extract_path_text(data,'appReferenceStr')) similar to " 
						+ searchTerm + " or " + "lower(jsonb_extract_path_text(data,'shortName')) similar to "
						+ searchTerm + " or " + "lower(jsonb_extract_path_text(data,'name')) similar to "
						+ searchTerm + " or " + "lower(id) similar to "
						+ searchTerm + " ) ";
			getAllStmt += " order by lower(jsonb_extract_path_text(data,'name')) asc limit " + limit;
		}
		Query q = em.createNativeQuery(getAllStmt);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();
		List<PlanningITNsql> convertedResults = results.stream().map(temp -> {
			PlanningITNsql entity = new PlanningITNsql();
			try {
				String jsonData = temp[1] != null ? temp[1].toString() : "";
				PlanningIT tempPlanningIT = mapper.readValue(jsonData, PlanningIT.class);
				entity.setData(tempPlanningIT);
			} catch (Exception e) {
				LOGGER.error("Failed while fetching all planningIT systems using native query with exception {} ", e.getMessage());
			}
			String id = temp[0] != null ? temp[0].toString() : "";
			entity.setId(id);
			return entity;
		}).collect(Collectors.toList());
		return convertedResults;
	}
	
	
}
