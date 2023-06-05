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

package com.daimler.data.db.repo.workspace;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.daimler.data.db.json.UserInfo;
import org.springframework.stereotype.Repository;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class WorkspaceCustomRepositoryImpl extends CommonDataRepositoryImpl<CodeServerWorkspaceNsql, String>
		implements WorkspaceCustomRepository {

	
	@Override
	public List<CodeServerWorkspaceNsql> findAll(String userId, int limit, int offset) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CodeServerWorkspaceNsql> cq = cb.createQuery(CodeServerWorkspaceNsql.class);
		Root<CodeServerWorkspaceNsql> root = cq.from(entityClass);
		CriteriaQuery<CodeServerWorkspaceNsql> getAll = cq.select(root);
		Predicate p1 = cb.equal(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("workspaceOwner"), cb.literal("id"))),
				userId.toLowerCase());
		Predicate p2 = cb.notEqual(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
				"DELETED".toLowerCase());
		Predicate pMain = cb.and(p1,p2);
		cq.where(pMain);
		TypedQuery<CodeServerWorkspaceNsql> getAllQuery = em.createQuery(getAll);
		if (offset >= 0)
			getAllQuery.setFirstResult(offset);
		if (limit > 0)
			getAllQuery.setMaxResults(limit);
		return getAllQuery.getResultList();
	}

	@Override
	public Integer getCount(String userId) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<CodeServerWorkspaceNsql> root = cq.from(CodeServerWorkspaceNsql.class);
		CriteriaQuery<Long> getAll = cq.select(cb.count(root));
		Predicate p1 = cb.equal(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("workspaceOwner"), cb.literal("id"))),
				userId.toLowerCase());
		Predicate p2 = cb.notEqual(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
				"DELETED".toLowerCase());
		Predicate pMain = cb.and(p1,p2);
		cq.where(pMain);
		TypedQuery<Long> getAllQuery = em.createQuery(getAll);
		Long count = getAllQuery.getSingleResult();
		return Integer.valueOf(count.intValue());
	}

	@Override
	public CodeServerWorkspaceNsql findbyProjectName(String userId, String projectName) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CodeServerWorkspaceNsql> cq = cb.createQuery(CodeServerWorkspaceNsql.class);
		Root<CodeServerWorkspaceNsql> root = cq.from(entityClass);
		CriteriaQuery<CodeServerWorkspaceNsql> byName = cq.select(root);
		Predicate con1 = cb.equal(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("projectDetails"), cb.literal("projectName"))),
				projectName.toLowerCase());
		Predicate con2 = cb.equal(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("workspaceOwner"), cb.literal("id"))),
				userId.toLowerCase());
		Predicate con3 = cb.notEqual(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
				"DELETED".toLowerCase());
		Predicate pMain = cb.and(con1, con2, con3);
		cq.where(pMain);
		TypedQuery<CodeServerWorkspaceNsql> byNameQuery = em.createQuery(byName);
		List<CodeServerWorkspaceNsql> entities = byNameQuery.getResultList();
		if (entities != null && entities.size() > 0)
			return entities.get(0);
		else
			return null;
	}
	
	@Override
	public CodeServerWorkspaceNsql findbyUniqueLiteral(String userId, String uniqueLiteral, String value) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CodeServerWorkspaceNsql> cq = cb.createQuery(CodeServerWorkspaceNsql.class);
		Root<CodeServerWorkspaceNsql> root = cq.from(entityClass);
		CriteriaQuery<CodeServerWorkspaceNsql> byName = cq.select(root);
		Predicate con1 = cb.equal(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal(uniqueLiteral))),
				value.toLowerCase());
		Predicate con2 = cb.equal(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("workspaceOwner"), cb.literal("id"))),
				userId.toLowerCase());
		Predicate con3 = cb.notEqual(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
				"DELETED".toLowerCase());
		Predicate pMain = cb.and(con1, con2, con3);
		cq.where(pMain);
		TypedQuery<CodeServerWorkspaceNsql> byNameQuery = em.createQuery(byName);
		List<CodeServerWorkspaceNsql> entities = byNameQuery.getResultList();
		if (entities != null && entities.size() > 0)
			return entities.get(0);
		else
			return null;
	}

	@Override
	public CodeServerWorkspaceNsql findById(String userId, String id) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CodeServerWorkspaceNsql> cq = cb.createQuery(CodeServerWorkspaceNsql.class);
		Root<CodeServerWorkspaceNsql> root = cq.from(entityClass);
		CriteriaQuery<CodeServerWorkspaceNsql> byName = cq.select(root);
		Predicate con1 = cb.equal(root.get("id"),id);
		Predicate con2 = cb.equal(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("workspaceOwner"), cb.literal("id"))),
				userId.toLowerCase());
		Predicate con3 = cb.notEqual(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
				"DELETED".toLowerCase());
		Predicate pMain = cb.and(con1, con2, con3);
		cq.where(pMain);
		cq.orderBy(cb.desc(cb.function("jsonb_extract_path_text", Date.class, root.get("data"), cb.literal("intiatedOn"))));
		TypedQuery<CodeServerWorkspaceNsql> byNameQuery = em.createQuery(byName);
		List<CodeServerWorkspaceNsql> entities = byNameQuery.getResultList();
		if (entities != null && entities.size() > 0)
			return entities.get(0);
		else
			return null;
	}

	
	@Override
	public List<Object[]> getWorkspaceIdsForProjectMembers(String projectName){
		List<Object[]> records = new ArrayList<>();
		
		String getQuery = "select jsonb_extract_path_text(data,'workspaceId') as wsid,"
				+ "jsonb_extract_path_text(data,'workspaceOwner','id') as userid from workspace_nsql "
				+ "where lower(jsonb_extract_path_text(data,'projectDetails','projectName'))"
				+ "= '" + projectName.toLowerCase() + "' and lower(jsonb_extract_path_text(data,'status')) <> 'deleted'";
		try {
			Query q = em.createNativeQuery(getQuery);
			records = q.getResultList();
			if(records!=null && !records.isEmpty()) {
				log.info("Found {} workspaces in project {} which are not in deleted state", records.size(), projectName);
			}
		}catch(Exception e) {
			log.error("Failed to query workspaces under project {} , which are not in deleted state", projectName);
		}
		return records;
	}

	@Override
	public String getWorkspaceTechnicalId(String userId, String projectName) {
		String id = "";
		String getQuery = "select id "
				+ " from workspace_nsql "
				+ " where lower(jsonb_extract_path_text(data,'projectDetails','projectName'))" + "= '" + projectName.toLowerCase() + "' and lower(jsonb_extract_path_text(data,'workspaceOwner','id')) = '" + userId.toLowerCase() + "'"
				+ " and lower(jsonb_extract_path_text(data,'status')) <> 'deleted'";
		try {
			Query q = em.createNativeQuery(getQuery);
			id = (String) q.getSingleResult();
			if (id != null && !id.isEmpty()) {
				log.info("Found {} workspaces in project {} which are not in deleted state", id, projectName);
			}
		} catch (Exception e) {
			log.error("Failed to query workspaces under project {} , which are not in deleted state", projectName);
		}
		return id;
	}

	@Override
	public GenericMessage updateProjectOwnerDetails(String projectName, UserInfo updatedProjectOwnerDetails) {
		GenericMessage updateResponse = new GenericMessage();
		updateResponse.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();

		String updateQuery = "update workspace_nsql\r\n"
				+ "set data = jsonb_set(data, '{projectDetails,projectOwner}', \r\n"
				+ " '{\"id\": " + addQuotes(updatedProjectOwnerDetails.getId()) + ","
				+ " \"email\": " + addQuotes(updatedProjectOwnerDetails.getEmail()) + ","
				+ " \"lastName\": " + addQuotes(updatedProjectOwnerDetails.getLastName()) + ","
				+ " \"firstName\": " + addQuotes(updatedProjectOwnerDetails.getFirstName()) + ","
				+ " \"department\": " + addQuotes(updatedProjectOwnerDetails.getDepartment()) + ","
				+ " \"gitUserName\": " + addQuotes(updatedProjectOwnerDetails.getGitUserName()) + ","
				+ " \"mobileNumber\": " + addQuotes(updatedProjectOwnerDetails.getMobileNumber()) + "}' )\n" + "\\:" + "\\:" + "jsonb \n"
				+ "where data->'projectDetails'->>'projectName' = '" + projectName + "'" + " and lower(jsonb_extract_path_text(data,'status')) <> 'deleted'";
		try {
			Query q = em.createNativeQuery(updateQuery);
			q.executeUpdate();
			updateResponse.setSuccess("SUCCESS");
			updateResponse.setErrors(new ArrayList<>());
			updateResponse.setWarnings(new ArrayList<>());
			log.info("collaborator details updated successfully for project {} ", projectName);
		} catch (Exception e) {
			e.printStackTrace();
			MessageDescription errMsg = new MessageDescription("Failed while updating the collaborator details.");
			errors.add(errMsg);
			log.error("projectCollaborators details Failed while updating the collaborator with Exception {} ", e.getMessage());
		}
		return updateResponse;
	}

		@Override
	public GenericMessage updateCollaboratorDetails(String projectName, UserInfo updatedcollaborators, boolean removeUser) {
		GenericMessage updateResponse = new GenericMessage();
		updateResponse.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		if (updatedcollaborators != null) {
			String updateQuery = "";
			if (removeUser) {
				updateQuery = "update workspace_nsql\r\n"
						+ "set data = jsonb_set(data,'{projectDetails, projectCollaborators}', \r\n"
						+ "case when (select count(x) from jsonb_array_elements(data->'projectDetails'->'projectCollaborators')"
						+ " x where x->>'id' != " + "'" + updatedcollaborators.getId() + "'" + ") = 0 "
						+ "then '[]' "
						+ "else (select jsonb_agg(x) from jsonb_array_elements(data->'projectDetails'->'projectCollaborators')"
						+ " x where x->>'id' != " + "'" + updatedcollaborators.getId() + "'" + ") "
						+ "end )"
						+ "where data->'projectDetails'->>'projectName' = '" + projectName + "'" + " and lower(jsonb_extract_path_text(data,'status')) <> 'deleted'";
			} else {
				updateQuery = "update workspace_nsql\r\n"
						+ "set data = jsonb_set(data,'{projectDetails, projectCollaborators}', \r\n"
						+ "COALESCE(jsonb_extract_path_text(data, 'projectDetails','projectCollaborators'), '[]')" + "\\:" + "\\:" + "jsonb || \n"
						+ " '{\"id\": " + addQuotes(updatedcollaborators.getId()) + ","
						+ " \"email\": " + addQuotes(updatedcollaborators.getEmail()) + ","
						+ " \"lastName\": " + addQuotes(updatedcollaborators.getLastName()) + ","
						+ " \"firstName\": " + addQuotes(updatedcollaborators.getFirstName()) + ","
						+ " \"department\": " + addQuotes(updatedcollaborators.getDepartment()) + ","
						+ " \"gitUserName\": " + addQuotes(updatedcollaborators.getGitUserName()) + ","
						+ " \"mobileNumber\": " + addQuotes(updatedcollaborators.getMobileNumber()) + "}' )\n"
						+ "where data->'projectDetails'->>'projectName' = '" + projectName + "'" + " and lower(jsonb_extract_path_text(data,'status')) <> 'deleted'";
			}
			try {
				Query q = em.createNativeQuery(updateQuery);
				q.executeUpdate();
				updateResponse.setSuccess("SUCCESS");
				updateResponse.setErrors(new ArrayList<>());
				updateResponse.setWarnings(new ArrayList<>());
				log.info("collaborator details updated successfully for project {} ", projectName);
			} catch (Exception e) {
				MessageDescription errMsg = new MessageDescription("Failed while updating the collaborator details.");
				errors.add(errMsg);
				log.error("projectCollaborators details Failed while updating the collaborator with Exception {} ", e.getMessage());
			}
		}
		return updateResponse;
	}

	
	@Override
	public GenericMessage updateDeploymentDetails(String projectName, String environment, CodeServerDeploymentDetails deploymentDetails) {
		GenericMessage updateResponse = new GenericMessage();
		updateResponse.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		Date deployedOn = deploymentDetails.getLastDeployedOn();
		String longdate = null;
		if(deployedOn!=null)
			longdate = String.valueOf(deployedOn.getTime()) ;
		String updateQuery = "update workspace_nsql\r\n"
				+ "set data = jsonb_set(data,'{projectDetails,"+ environment  +"}', \r\n"
						+ "'{\"deploymentUrl\": "+ addQuotes(deploymentDetails.getDeploymentUrl()) +","
						+ " \"lastDeployedBy\": {\"id\": "+ addQuotes(deploymentDetails.getLastDeployedBy().getId())  + ","
								+ " \"email\": " + addQuotes(deploymentDetails.getLastDeployedBy().getEmail())  + ","
								+ " \"lastName\": "+ addQuotes(deploymentDetails.getLastDeployedBy().getLastName()) + ","
								+ " \"firstName\": "+ addQuotes(deploymentDetails.getLastDeployedBy().getFirstName())  + ","
								+ " \"department\": "+ addQuotes(deploymentDetails.getLastDeployedBy().getDepartment())  + ","
								+ " \"gitUserName\": "+ addQuotes(deploymentDetails.getLastDeployedBy().getGitUserName())  + ","
								+ " \"mobileNumber\": "+ addQuotes(deploymentDetails.getLastDeployedBy().getMobileNumber())  + "},"
						+ " \"lastDeployedOn\":" +   longdate + ","
						+ " \"lastDeployedBranch\": "+ addQuotes(deploymentDetails.getLastDeployedBranch()) +","
						+ " \"lastDeploymentStatus\": "+ addQuotes(deploymentDetails.getLastDeploymentStatus()) +"}')\r\n"
				+ "where data->'projectDetails'->>'projectName' = '"+projectName+"'";
		try {
			Query q = em.createNativeQuery(updateQuery);
			q.executeUpdate();
			updateResponse.setSuccess("SUCCESS");
			updateResponse.setErrors(new ArrayList<>());
			updateResponse.setWarnings(new ArrayList<>());
			log.info("deployment details updated successfully for project {} ", projectName);
		}catch(Exception e) {
			MessageDescription errMsg = new MessageDescription("Failed while updating deployment details.");
			errors.add(errMsg);
			log.error("deployment details updated successfully for project {} and environment {} , branch {} ", projectName,environment,deploymentDetails.getLastDeployedBranch());
		}
		return updateResponse;
	}
	
	private String addQuotes(String value) {
		if(value!=null && !"null".equalsIgnoreCase(value))
			return "\"" + value + "\"";
		else
			return null;
	}

	@Override
	public void updateDeletedStatusForProject(String projectName) {
		String updateQuery = "UPDATE workspace_nsql SET data = jsonb_set(data, '{status}', '\"DELETED\"') "
				+ " where lower(jsonb_extract_path_text(data,'projectDetails','projectName')) "
				+ "= '" + projectName.toLowerCase() + "' and lower(jsonb_extract_path_text(data,'status')) <> 'deleted'";
		try {
			Query q = em.createNativeQuery(updateQuery);
			q.executeUpdate();
			log.info("updated all workspaces under project {} to DELETED state", projectName);
		}catch(Exception e) {
			log.error("Failed to update workspaces under project {} to DELETED state with exception {}", projectName, e.getMessage());
		}
	}

	@Override
	public Integer getTotalCountOfWorkSpace(String status) {
		String query = "select count(*) from workspace_nsql where ((jsonb_extract_path_text(data,'status')) != (" +"'"+ status +"'"+ "))";
		Query q = em.createNativeQuery(query);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.intValue();
	}

}
