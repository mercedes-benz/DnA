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
import java.util.Collections;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Predicate;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
	public List<ADAProjectsNsql> findAllByCreator(String creator, int limit, int offset) {
	
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<ADAProjectsNsql> cq = cb.createQuery(ADAProjectsNsql.class);
		Root<ADAProjectsNsql> root = cq.from(ADAProjectsNsql.class);
		
		Expression<String> createdByPath = cb.function(
			"jsonb_extract_path_text",
			String.class,           
			root.get("data"),         
			cb.literal("createdBy")  
		);

		Predicate creatorPredicate = cb.equal(
			cb.lower(createdByPath),
			cb.lower(cb.literal(creator))
		);
		cq.where(creatorPredicate);
		TypedQuery<ADAProjectsNsql> query = em.createQuery(cq);
		
		query.setFirstResult(offset);
		query.setMaxResults(limit);  

		return query.getResultList();
	}

	@Override
	public List<ADAProjectsNsql> searchProjectsByName(String projectName) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<ADAProjectsNsql> cq = cb.createQuery(ADAProjectsNsql.class);
			Root<ADAProjectsNsql> root = cq.from(ADAProjectsNsql.class);
			List<Predicate> predicates = new ArrayList<>();

			if (projectName != null && !projectName.trim().isEmpty()) {
				String loweredTerm = "%" + projectName.trim().toLowerCase() + "%";
				Predicate name = cb.like(
					cb.lower(cb.function("jsonb_extract_path_text", String.class,
							root.get("data"), cb.literal("projectName"))),loweredTerm);
				Predicate projectId = cb.like(
					cb.lower(cb.coalesce(cb.function("jsonb_extract_path_text", String.class,
							root.get("data"), cb.literal("projectID")),
						cb.literal(""))),loweredTerm);
				Predicate stakeholders = cb.like(
					cb.lower(
						cb.function("jsonb_extract_path_text", String.class,
								cb.function("to_jsonb", Object.class, root.get("data")),
								cb.literal("stakeholders"))),loweredTerm);
				Predicate tags = cb.like(
					cb.lower(
						cb.function("jsonb_extract_path_text", String.class,
								cb.function("to_jsonb", Object.class, root.get("data")),
								cb.literal("tags"))),loweredTerm);
				predicates.add(cb.or(name, projectId, stakeholders, tags));
			}
			cq.select(root);
			if (!predicates.isEmpty()) {
				cq.where(cb.and(predicates.toArray(new Predicate[0])));
			}

			cq.orderBy(cb.asc(
					cb.function("jsonb_extract_path_text", String.class,
							root.get("data"), cb.literal("projectName"))));

			TypedQuery<ADAProjectsNsql> query = em.createQuery(cq);
			List<ADAProjectsNsql> results = query.getResultList();

			return results;

		} catch (Exception e) {
			log.error("Error fetching ADA projects by name '{}': {}", projectName, e.getMessage(), e);
			return Collections.emptyList();
		}
	}

}
