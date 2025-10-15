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

package com.daimler.data.db.repo.fabric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.json.FabricWorkspace;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.util.ConstantsUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class FabricWorkspaceCustomRepositoryImpl extends CommonDataRepositoryImpl<FabricWorkspaceNsql, String>
		implements FabricWorkspaceCustomRepository {

	@Override
	public long getTotalCount(String userId) {
		String user = userId.toLowerCase();
		String getCountStmt = "SELECT count(*) FROM fabric_workspace_nsql " + 
                      "WHERE (lower(jsonb_extract_path_text(data, 'createdBy', 'id')) = '" + user + "' " + 
                      "OR lower(COALESCE(jsonb_extract_path_text(data, 'initiatedBy'), '')) = '" + user + "')";

		Query q = em.createNativeQuery(getCountStmt);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.longValue();
	}
	
	@Override
	public List<FabricWorkspaceNsql> getAll(String userId, int offset, int limit){
		String user = userId.toLowerCase();
		String getAllStmt = "SELECT cast(id AS text), cast(data AS text) FROM fabric_workspace_nsql " + 
                    "WHERE (lower(COALESCE(jsonb_extract_path_text(data, 'createdBy', 'id'), '')) = '" + user + "' " +
                    "OR lower(COALESCE(jsonb_extract_path_text(data, 'initiatedBy'), '')) = '" + user + "')";

		if (limit > 0)
			getAllStmt = getAllStmt + " limit " + limit;
		if (offset >= 0)
			getAllStmt = getAllStmt + " offset " + offset;
		Query q = em.createNativeQuery(getAllStmt);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();
		List<FabricWorkspaceNsql> convertedResults = results.stream().map(temp -> {
			FabricWorkspaceNsql entity = new FabricWorkspaceNsql();
			try {
				String jsonData = temp[1] != null ? temp[1].toString() : "";
				FabricWorkspace tempForecast = mapper.readValue(jsonData, FabricWorkspace.class);
				entity.setData(tempForecast);
			} catch (Exception e) {
				log.error("Failed while fetching all projects using native query with exception {} ", e.getMessage());
			}
			String id = temp[0] != null ? temp[0].toString() : "";
			entity.setId(id);
			return entity;
		}).collect(Collectors.toList());
		return convertedResults;
	}

	@Override
	public long getTotalCountForAdmin(String search) {
	StringBuilder countQuery = new StringBuilder( "SELECT count(*) FROM fabric_workspace_nsql " + 
	"WHERE lower(jsonb_extract_path_text(data, 'status', 'state')) NOT IN ('deleted')" );

		if (search != null && !search.trim().isEmpty()) {
			String searchTerm = "%" + search.toLowerCase() + "%";
			countQuery.append(" AND lower(jsonb_extract_path_text(data, 'name')) LIKE '")
					.append(searchTerm)
					.append("'");
		}

		Query q = em.createNativeQuery(countQuery.toString());
		BigInteger result = (BigInteger) q.getSingleResult();
		return result.longValue();
	}

	@Override
	public List<FabricWorkspaceNsql> getAllForAdmin(int limit, int offset, String search) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<FabricWorkspaceNsql> cq = cb.createQuery(FabricWorkspaceNsql.class);
			Root<FabricWorkspaceNsql> root = cq.from(FabricWorkspaceNsql.class);

			List<Predicate> predicates = new ArrayList<>();

			Predicate notDeleted = cb.notEqual(
					cb.lower(cb.function("jsonb_extract_path_text", String.class,
							root.get("data"), cb.literal("status"), cb.literal("state"))),
					"deleted");
			predicates.add(notDeleted);

			if (search != null && !search.trim().isEmpty()) {
				Predicate nameLike = cb.like(
						cb.lower(cb.function("jsonb_extract_path_text", String.class,
								root.get("data"), cb.literal("name"))),
						"%" + search.trim().toLowerCase() + "%");
				predicates.add(nameLike);
			}
			cq.select(root);
			cq.where(cb.and(predicates.toArray(new Predicate[0])));
			cq.orderBy(cb.asc(
					cb.function("jsonb_extract_path_text", String.class,
							root.get("data"), cb.literal("name"))));

			TypedQuery<FabricWorkspaceNsql> query = em.createQuery(cq);

			if (limit > 0) {
				query.setMaxResults(limit);
			}
			if (offset >= 0) {
				query.setFirstResult(offset);
			}

			List<FabricWorkspaceNsql> results = query.getResultList();
			log.info("Found {} workspaces (search='{}')", results.size(), search);
			return results;

		} catch (Exception e) {
			log.error("Error fetching Fabric workspaces (search='{}'): {}", search, e.getMessage(), e);
			return Collections.emptyList();
		}
	}

}
