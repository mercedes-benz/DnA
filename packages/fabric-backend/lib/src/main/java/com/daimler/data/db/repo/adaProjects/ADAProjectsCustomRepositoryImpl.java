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

package com.daimler.data.db.repo.adaProjects;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.daimler.data.assembler.ADAProjectsAssembler;
import com.daimler.data.db.entities.ADAProjectsNsql;
import com.daimler.data.db.json.ADAProjectDetails;
import com.daimler.data.db.json.FabricWorkspace;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.dto.adaProjects.ADAProjectDetailsVO;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ADAProjectsCustomRepositoryImpl extends CommonDataRepositoryImpl<ADAProjectsNsql, String>
		implements ADAProjectsCustomRepository {

		@Autowired
		private ADAProjectsAssembler adaProjectsAssembler;

	// @Override
	// public long getTotalCount(String userId) {
	// 	String user = userId.toLowerCase();
	// 	String getCountStmt = "SELECT count(*) FROM fabric_workspace_nsql " + 
    //                   "WHERE (lower(jsonb_extract_path_text(data, 'createdBy', 'id')) = '" + user + "' " + 
    //                   "OR lower(COALESCE(jsonb_extract_path_text(data, 'initiatedBy'), '')) = '" + user + "')";

	// 	Query q = em.createNativeQuery(getCountStmt);
	// 	BigInteger results = (BigInteger) q.getSingleResult();
	// 	return results.longValue();
	// }
	
	// @Override
	// public List<FabricWorkspaceNsql> getAll(String userId, int offset, int limit){
	// 	String user = userId.toLowerCase();
	// 	String getAllStmt = "SELECT cast(id AS text), cast(data AS text) FROM fabric_workspace_nsql " + 
    //                 "WHERE (lower(COALESCE(jsonb_extract_path_text(data, 'createdBy', 'id'), '')) = '" + user + "' " +
    //                 "OR lower(COALESCE(jsonb_extract_path_text(data, 'initiatedBy'), '')) = '" + user + "')";

	// 	if (limit > 0)
	// 		getAllStmt = getAllStmt + " limit " + limit;
	// 	if (offset >= 0)
	// 		getAllStmt = getAllStmt + " offset " + offset;
	// 	Query q = em.createNativeQuery(getAllStmt);
	// 	ObjectMapper mapper = new ObjectMapper();
	// 	List<Object[]> results = q.getResultList();
	// 	List<FabricWorkspaceNsql> convertedResults = results.stream().map(temp -> {
	// 		FabricWorkspaceNsql entity = new FabricWorkspaceNsql();
	// 		try {
	// 			String jsonData = temp[1] != null ? temp[1].toString() : "";
	// 			FabricWorkspace tempForecast = mapper.readValue(jsonData, FabricWorkspace.class);
	// 			entity.setData(tempForecast);
	// 		} catch (Exception e) {
	// 			log.error("Failed while fetching all projects using native query with exception {} ", e.getMessage());
	// 		}
	// 		String id = temp[0] != null ? temp[0].toString() : "";
	// 		entity.setId(id);
	// 		return entity;
	// 	}).collect(Collectors.toList());
	// 	return convertedResults;
	// }

	@Override
	public List<ADAProjectsNsql> searchProjectsByName(String projectName) {
		List<ADAProjectsNsql> convertedResults = new ArrayList<>();
		try {
			StringBuilder queryBuilder = new StringBuilder(
				"SELECT cast(id AS text), cast(data AS text) FROM ada_projects_nsql"
			);

			if (projectName != null && !projectName.trim().isEmpty()) {
				String searchTerm = "%" + projectName.trim().toLowerCase() + "%";
				queryBuilder.append(" WHERE lower(jsonb_extract_path_text(data, 'projectName')) LIKE '")
							.append(searchTerm)
							.append("'");
			}

			Query query = em.createNativeQuery(queryBuilder.toString());

			ObjectMapper mapper = new ObjectMapper();
			List<Object[]> results = query.getResultList();

			convertedResults = results.stream().map(temp -> {
				ADAProjectsNsql entity = new ADAProjectsNsql();
				try {
					String jsonData = temp[1] != null ? temp[1].toString() : "";
					ADAProjectDetails project = mapper.readValue(jsonData, ADAProjectDetails.class);
					entity.setData(project);
				} catch (Exception e) {
					log.error("Failed while parsing project JSON: {}", e.getMessage());
				}
				String id = temp[0] != null ? temp[0].toString() : "";
				entity.setId(id);
				return entity;
			}).collect(Collectors.toList());

		} catch (Exception e) {
			log.error("Error fetching ADA projects by name '{}': {}", projectName, e.getMessage());
		}

		return convertedResults;
	}

}
