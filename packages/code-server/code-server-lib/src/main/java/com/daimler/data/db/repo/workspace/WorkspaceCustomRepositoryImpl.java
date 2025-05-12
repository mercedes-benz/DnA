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
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.json.CodeServerLeanGovernanceFeilds;
import com.daimler.data.db.json.CodespaceSecurityConfig;
import com.daimler.data.db.json.DeploymentAudit;
import com.daimler.data.db.json.UserInfo;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.dto.CodespaceSecurityConfigDto;
import com.daimler.data.dto.workspace.CodeServerWorkspaceValidateVO;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class WorkspaceCustomRepositoryImpl extends CommonDataRepositoryImpl<CodeServerWorkspaceNsql, String>
		implements WorkspaceCustomRepository {

	@Override
	public List<CodeServerWorkspaceNsql> findAll(){
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CodeServerWorkspaceNsql> cq = cb.createQuery(CodeServerWorkspaceNsql.class);
		Root<CodeServerWorkspaceNsql> root = cq.from(entityClass);
		CriteriaQuery<CodeServerWorkspaceNsql> getAll = cq.select(root);
		Predicate p1 = cb.notEqual(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
				"DELETED".toLowerCase());
		Predicate pMain = cb.and(p1);
		cq.where(pMain);		
		cq.orderBy(cb.asc(cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("projectDetails"), cb.literal("projectName"))));
		TypedQuery<CodeServerWorkspaceNsql> getAllQuery = em.createQuery(getAll);
		return getAllQuery.getResultList();		
	} 
			
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
		cq.orderBy(cb.asc(cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("projectDetails"), cb.literal("projectName"))));
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
	public Integer getCountByRecipeName(String userId, String recipeName) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<CodeServerWorkspaceNsql> root = cq.from(CodeServerWorkspaceNsql.class);
		cq.select(cb.count(root));
	
		Predicate p1 = cb.equal(cb.lower(
			cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("workspaceOwner"), cb.literal("id"))),
			userId.toLowerCase());
	
		Predicate p2 = cb.notEqual(cb.lower(
			cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
			"deleted");
	
		Predicate p3 = cb.equal(
			cb.function("jsonb_extract_path_text", String.class, root.get("data"),
				cb.literal("projectDetails"), cb.literal("recipeDetails"), cb.literal("recipeName")),
			recipeName
		);
	
		cq.where(cb.and(p1, p2, p3));
	
		TypedQuery<Long> query = em.createQuery(cq);
		Long count = query.getSingleResult();
		return count.intValue();
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
	public CodeServerWorkspaceNsql findbyProjectName(String projectName) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CodeServerWorkspaceNsql> cq = cb.createQuery(CodeServerWorkspaceNsql.class);
		Root<CodeServerWorkspaceNsql> root = cq.from(entityClass);
		CriteriaQuery<CodeServerWorkspaceNsql> byName = cq.select(root);
		Predicate con1 = cb.equal(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("projectDetails"), cb.literal("projectName"))),
				projectName.toLowerCase());
		Predicate con3 = cb.notEqual(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
				"DELETED".toLowerCase());
		Predicate pMain = cb.and(con1, con3);
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
		//cq.orderBy(cb.desc(cb.function("jsonb_extract_path_text", Date.class, root.get("data"), cb.literal("intiatedOn"))));
		TypedQuery<CodeServerWorkspaceNsql> byNameQuery = em.createQuery(byName);
		List<CodeServerWorkspaceNsql> entities = byNameQuery.getResultList();
		if (entities != null && entities.size() > 0)
			return entities.get(0);
		else
			return null;
	}

	
	@Override
	public List<Object[]> getWorkspaceIdsForProjectMembers(String projectName, String projectOwnerId){
		List<Object[]> records = new ArrayList<>();
		
		String getQuery = "select jsonb_extract_path_text(data,'workspaceId') as wsid,"
				+ "jsonb_extract_path_text(data,'workspaceOwner','id') as userid from workspace_nsql "
				+ "where lower(jsonb_extract_path_text(data,'projectDetails','projectName'))"
				+ "= '" + projectName.toLowerCase() + "' and lower(jsonb_extract_path_text(data,'status')) <> 'deleted'"
				+ "and lower(jsonb_extract_path_text(data,'projectDetails','projectOwner','id'))" + "= '" + projectOwnerId.toLowerCase() + "' ";
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
	@Transactional
	public GenericMessage updateRecipeDetails(CodeServerWorkspaceNsql codeServerWorkspaceNsql){
		GenericMessage updateResponse = new GenericMessage();
		updateResponse.setSuccess("FAILED");
		String updateQuery ="UPDATE public.workspace_nsql SET data = jsonb_set(data, '{projectDetails,recipeDetails,recipeName}', '\""
		+codeServerWorkspaceNsql.getData().getProjectDetails().getRecipeDetails().getRecipeName() +"\"') WHERE jsonb_extract_path_text(data, 'workspaceId') = '"
		+ codeServerWorkspaceNsql.getData().getWorkspaceId()+"'";
		try {
			Query q = em.createNativeQuery(updateQuery);
			q.executeUpdate();
			updateResponse.setSuccess("SUCCESS");
			updateResponse.setErrors(new ArrayList<>());
			updateResponse.setWarnings(new ArrayList<>());
			log.info("migration recipe updated successfully ", codeServerWorkspaceNsql.getData().getWorkspaceId());
		} catch (Exception e) {
			log.error("Exception occured while migrating recipe Name for workspace {} with exception {}",codeServerWorkspaceNsql.getData().getWorkspaceId(),e);
		}
		return updateResponse;
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
						+ " \"isAdmin\": " + updatedcollaborators.getIsAdmin()+ ","
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
			String updateQuery = "update workspace_nsql " +
				"set data = jsonb_set(data,'{projectDetails," + environment + "}', " +
				"'{\"deploymentUrl\": " + addQuotes(deploymentDetails.getDeploymentUrl()) + "," +
				" \"lastDeployedBy\": {\"id\": " + addQuotes(deploymentDetails.getLastDeployedBy().getId()) + "," +
				" \"email\": " + addQuotes(deploymentDetails.getLastDeployedBy().getEmail()) + "," +
				" \"lastName\": " + addQuotes(deploymentDetails.getLastDeployedBy().getLastName()) + "," +
				" \"firstName\": " + addQuotes(deploymentDetails.getLastDeployedBy().getFirstName()) + "," +
				" \"department\": " + addQuotes(deploymentDetails.getLastDeployedBy().getDepartment()) + "," +
				" \"gitUserName\": " + addQuotes(deploymentDetails.getLastDeployedBy().getGitUserName()) + "," +
				" \"mobileNumber\": " + addQuotes(deploymentDetails.getLastDeployedBy().getMobileNumber()) + "}," +
				" \"lastDeployedOn\":" + longdate + "," +
				" \"secureWithIAMRequired\": " + deploymentDetails.getSecureWithIAMRequired() + "," +
				// " \"technicalUserDetailsForIAMLogin\": " + addQuotes(deploymentDetails.getTechnicalUserDetailsForIAMLogin()) + "," +
				" \"lastDeployedBranch\": " + addQuotes(deploymentDetails.getLastDeployedBranch()) + "," +
				" \"gitjobRunID\": " + addQuotes(deploymentDetails.getGitjobRunID()) + "," +
				" \"lastDeploymentStatus\": " + addQuotes(deploymentDetails.getLastDeploymentStatus()) ;

			List<DeploymentAudit> deploymentAuditLogs = deploymentDetails.getDeploymentAuditLogs();
			updateQuery += ", \"deploymentAuditLogs\" : ";
			if (deploymentAuditLogs != null && !deploymentAuditLogs.isEmpty()) {
				// Iterate over each DeploymentAudit object and add it to the JSON array
				updateQuery += "[";
				for (int i = 0; i < deploymentAuditLogs.size(); i++) {
					DeploymentAudit auditLog = deploymentAuditLogs.get(i);
					updateQuery += "{" +
						" \"triggeredBy\": " + addQuotes(auditLog.getTriggeredBy()) + "," +
						" \"triggeredOn\": " + addQuotes(String.valueOf(auditLog.getTriggeredOn().getTime())) + "," +
						" \"deploymentStatus\": " + addQuotes(auditLog.getDeploymentStatus()) + "," +
						" \"deployedOn\": " + (auditLog.getDeployedOn() != null ? addQuotes(String.valueOf(auditLog.getDeployedOn().getTime())) : "null") + "," +
						" \"commitId\": " + (auditLog.getCommitId() != null ? addQuotes(String.valueOf(auditLog.getCommitId())) : "null") + "," +
						" \"branch\": " + addQuotes(auditLog.getBranch()) + "}";
					if(i+1 < deploymentAuditLogs.size()) {
						updateQuery += ",";
					}
				}
				updateQuery += "]";
			}else {
				updateQuery +=  " []";
			}
			updateQuery += "}')\r\n";
			updateQuery += "where data->'projectDetails'->>'projectName' = '" + projectName + "'";

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
			log.error("failed to update deployment details for project {} and environment {} , branch {} ", projectName,environment,deploymentDetails.getLastDeployedBranch());
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
	public Integer getTotalCountOfWorkSpace() {
		String query = "select count(*) from workspace_nsql where jsonb_extract_path_text(data,'status')  in ('CREATED')";
		Query q = em.createNativeQuery(query);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.intValue();
	}

	@Override
	public List<String> getAllWorkspaceIds() {
		List<String> records = new ArrayList<>();

		String getQuery = "select jsonb_extract_path_text(data,'workspaceId') as wsid from workspace_nsql where "
				+ "lower(jsonb_extract_path_text(data,'status')) not in ('create_requested','deleted','collaboration_requested') ;" ;				
		try {
			Query q = em.createNativeQuery(getQuery);
			records = q.getResultList();
			if (records != null && !records.isEmpty()) {
				log.info("Found {} workspaces in project {} which are not in deleted state", records.size());
			}
		} catch (Exception e) {
			log.error("Failed to query workspaces under project {} , which are not in deleted state");
		}
		return records;
	}


	@Override
	public CodeServerWorkspaceValidateVO validateCodespace(String id, String userId) {
		String getQuery = "SELECT COUNT(*) > 0 AS hasPermission " +
				"FROM workspace_nsql " +
				"WHERE jsonb_extract_path_text(data, 'workspaceId') = (:id) " +
				"AND LOWER(jsonb_extract_path_text(data, 'gitUserName')) = LOWER(:userId) " +
				"AND LOWER(jsonb_extract_path_text(data, 'status')) NOT IN ('create_requested', 'deleted', 'collaboration_requested', 'create_failed')";

		try {
			Query q = em.createNativeQuery(getQuery);
			q.setParameter("id", id);
			q.setParameter("userId", userId);

			List<Boolean> resultList = q.getResultList();
			if (!resultList.isEmpty() && resultList.get(0)) {
				CodeServerWorkspaceValidateVO validateVO = new CodeServerWorkspaceValidateVO();
				validateVO.setIsValid(true);
				return validateVO;
			}
		} catch (Exception e) {
			log.error("Failed to query workspaces under project {} , which are not in deleted state");
		}
		// If no record matched or an exception occurred, return CodeServerWorkspaceValidateVO with isValid set to false
		CodeServerWorkspaceValidateVO validateVO = new CodeServerWorkspaceValidateVO();
		validateVO.setIsValid(false);
		return validateVO;
	}

	@Override
	public CodeServerWorkspaceNsql findByWorkspaceId(String wsId) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CodeServerWorkspaceNsql> cq = cb.createQuery(CodeServerWorkspaceNsql.class);
		Root<CodeServerWorkspaceNsql> root = cq.from(entityClass);
		CriteriaQuery<CodeServerWorkspaceNsql> byName = cq.select(root);
		Predicate con1 = cb.equal(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("workspaceId"))),
				wsId.toLowerCase());
		Predicate con2 = cb.notEqual(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
				"DELETED".toLowerCase());
		Predicate pMain = cb.and(con1, con2);
		cq.where(con1);
		TypedQuery<CodeServerWorkspaceNsql> byNameQuery = em.createQuery(byName);
		List<CodeServerWorkspaceNsql> entities = byNameQuery.getResultList();
		if (entities != null && entities.size() > 0)
			return entities.get(0);
		else
			return null;
	
	}

	@Override
	public List<CodespaceSecurityConfigDto> getAllSecurityConfigs(Integer offset, Integer limit,String projectName){
		List<CodespaceSecurityConfigDto> data = new ArrayList<>();

		List<Object[]> results = new ArrayList<>();
		String getQuery;
				  if(projectName == null||"".equalsIgnoreCase(projectName)){
					getQuery = "SELECT DISTINCT ON (jsonb_extract_path_text(data, 'projectDetails', 'projectName'))"+
					"cast(jsonb_extract_path_text(data,'projectDetails','projectName') as text) as PROJECT_NAME, cast(id as text) as COLUMN_ID,  " +
                  "cast(jsonb_extract_path_text(data,'projectDetails','projectOwner') as text) as PROJECT_OWNER, " +
                  "cast(jsonb_extract_path_text(data,'projectDetails','securityConfig') as text) as SECURITY_CONFIG " +
                  "FROM workspace_nsql WHERE lower(jsonb_extract_path_text(data,'projectDetails','securityConfig','status')) in('requested','accepted') AND lower(jsonb_extract_path_text(data,'status')) in('created') ";
				  }
				  else{
					getQuery = "SELECT DISTINCT ON (jsonb_extract_path_text(data, 'projectDetails', 'projectName'))"+
					"cast(jsonb_extract_path_text(data,'projectDetails','projectName') as text) as PROJECT_NAME, cast(id as text) as COLUMN_ID,  " +
                  "cast(jsonb_extract_path_text(data,'projectDetails','projectOwner') as text) as PROJECT_OWNER, " +
                  "cast(jsonb_extract_path_text(data,'projectDetails','securityConfig') as text) as SECURITY_CONFIG " +
                  "FROM workspace_nsql WHERE lower(jsonb_extract_path_text(data,'projectDetails','projectName'))="+" '"+projectName +"'"+" AND lower(jsonb_extract_path_text(data,'status')) in('created') ";
				  }
		if (limit > 0)
			  getQuery = getQuery + " limit " + limit;
	  	if (offset >= 0)
			  getQuery = getQuery + " offset " + offset;
		try {
			Query q = em.createNativeQuery(getQuery);
			results = q.getResultList();

			ObjectMapper mapper = new ObjectMapper();
			for(Object[] rowData : results){
				CodespaceSecurityConfigDto rowDetails = new CodespaceSecurityConfigDto();
				if(rowData !=null){
					rowDetails.setId((String)rowData[1]);
					try{
						rowDetails.setProjectName((String)rowData[0]);
						
						UserInfo userDetails = mapper.readValue(rowData[2].toString(), UserInfo.class);
						rowDetails.setProjectOwner(userDetails);

						CodespaceSecurityConfig recordDetails = mapper.readValue(rowData[3].toString(), CodespaceSecurityConfig.class);
						rowDetails.setSecurityConfig(recordDetails);
					}catch(Exception e){
						log.error("exception occure while mapping data :{} ",e.getMessage());
						rowDetails.setSecurityConfig(null);
					}
					data.add(rowDetails);
				}
			}

			if(data!=null && !data.isEmpty()) {
																
				log.info("Found {} workspaces which are in requested and accepted state", data.size());
			}
		}catch(Exception e) {
			log.error("Failed to query workspaces under project , which are in requested and accepted state");
		}
		return data;
	}

	@Override
	public GenericMessage updateSecurityConfigStatus(String projectName, String status) {
		GenericMessage updateResponse = new GenericMessage();
		updateResponse.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();

		String updateQuery = "update workspace_nsql\r\n"
				+ "set data = jsonb_set(data, '{projectDetails,securityConfig}', \r\n"
				+ " '{\"status\": " + addQuotes(status)
				+ "}' )\n" + "\\:" + "\\:" + "jsonb \n"
				+ "where data->'projectDetails'->>'projectName' = '" + projectName + "'" + " and lower(jsonb_extract_path_text(data,'status')) <> 'deleted'";
		try {
			Query q = em.createNativeQuery(updateQuery);
			q.executeUpdate();
			updateResponse.setSuccess("SUCCESS");
			updateResponse.setErrors(new ArrayList<>());
			updateResponse.setWarnings(new ArrayList<>());
			log.info("security config status updated successfully for project {} ", projectName);
		} catch (Exception e) {
			MessageDescription errMsg = new MessageDescription("Failed while updating the security config status.");
			errors.add(errMsg);
			log.error("Failed while updating the security config status with Exception {} ", e.getMessage());
		}
		return updateResponse;
	}


	@Override
	public List<String> getWorkspaceIdsByProjectName( String projectName) {
		List<String> workspaceIds = new ArrayList<>();

		String getQuery = "select jsonb_extract_path_text(data,'workspaceId') as workspaceId "
				+ "from workspace_nsql "
				+ "where lower(jsonb_extract_path_text(data,'projectDetails','projectName'))"
				+ "in('" + projectName.toLowerCase() + "') and lower(jsonb_extract_path_text(data,'status')) <> 'deleted'";
		
		try {
			Query q = em.createNativeQuery(getQuery);
			workspaceIds = q.getResultList();
			if (workspaceIds != null && !workspaceIds.isEmpty()) {
				log.info("Found {} workspaces with project name {} which are not in deleted state", workspaceIds.size(), projectName);
			}
		} catch (Exception e) {
			log.error("Failed to query workspaces under project name {} , which are not in deleted state", projectName);
		}
		return workspaceIds;
		
	}

	@Override
	public CodeServerWorkspaceNsql findDataById(String id) {

		// String getQuery = "select data "
		// 	+ "from workspace_nsql "
		// 	+ "where id "
		// 	+ "in('"+ id +"') and lower(jsonb_extract_path_text(data,'status')) <> 'deleted'";
		// try {
		// 	Query q = em.createNativeQuery(getQuery);
		// 	List<CodeServerWorkspaceNsql> entities = q.getResultList();
		// if (entities != null && entities.size() > 0){
		// 	log.info("Found data for given id {} which are not in deleted state", id);
		// 	return entities.get(0);
			
		// }else{
		// 	return null;
		// }
		// }catch(Exception e){
		// 	log.info("Caught Exception while getting  data for given id {} which are not in deleted state", id);
		// }
		// return null;
		// String getAllStmt = "select cast(data as text) "
		// 			+ "from workspace_nsql "
		// 			+ "where id "
		// 			+ "in('" + id + "') and lower(jsonb_extract_path_text(data,'status')) <> 'deleted'";
																		
		// Query q = em.createNativeQuery(getAllStmt);
		// ObjectMapper mapper = new ObjectMapper();
		// List<Object[]> results = q.getResultList();
		// List<CodeServerWorkspaceNsql> convertedResults = results.stream().map(temp -> {
		// 	CodeServerWorkspaceNsql entity = new CodeServerWorkspaceNsql();
		// 	try {
		// 		String jsonData = temp[0] != null ? temp[0].toString() : "";
		// 		CodeServerWorkspace codespaceJson = mapper.readValue(jsonData, CodeServerWorkspace.class);
		// 		entity.setData(codespaceJson);
		// 	} catch (Exception e) {
		// 		log.error("Failed while fetching the codespace project details using native query with exception {} ", e.getMessage());
		// 	}			
		// 	return entity;
		// }).collect(Collectors.toList());
		// return convertedResults.get(0);

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CodeServerWorkspaceNsql> cq = cb.createQuery(CodeServerWorkspaceNsql.class);
		Root<CodeServerWorkspaceNsql> root = cq.from(entityClass);
		CriteriaQuery<CodeServerWorkspaceNsql> byName = cq.select(root);
		Predicate con1 = cb.equal(root.get("id"),id);		
		Predicate con2 = cb.notEqual(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("status"))),
				"DELETED".toLowerCase());
		Predicate pMain = cb.and(con1, con2);
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
	public GenericMessage updateGovernanceDetails(String projectName, CodeServerLeanGovernanceFeilds newGovFeilds) {
		GenericMessage updateResponse = new GenericMessage();
		updateResponse.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		// String ArrayTagstoJsonb = "[" + String.join(",", newGovFeilds.getTags()) +
		// "]";
		List<String> tags = newGovFeilds.getTags();
		String ArrayTagstoJsonb = "[" + tags.stream().map(tag -> "\"" + tag + "\"").collect(Collectors.joining(","))
				+ "]";

		String updateQuery = "update workspace_nsql\n"
				+ "set data = jsonb_set(data, '{projectDetails,dataGovernance}',\n"
				+ " '{\"tags\": " + ArrayTagstoJsonb + ","
				+ " \"piiData\": " + newGovFeilds.getPiiData() + ","
				+ " \"archerId\": " + addQuotes(newGovFeilds.getArcherId()) + ","
				+ " \"division\": " + addQuotes(newGovFeilds.getDivision()) + ","
				+ " \"department\": " + addQuotes(newGovFeilds.getDepartment()) + ","
				+ " \"divisionId\": " + addQuotes(newGovFeilds.getDivisionId()) + ","
				+ " \"division\": " + addQuotes(newGovFeilds.getDivision()) + ","
				+ " \"description\": " + addQuotes(newGovFeilds.getDescription()) + ","
				+ " \"procedureID\": " + addQuotes(newGovFeilds.getProcedureID()) + ","
				+ " \"subDivisionId\": " + addQuotes(newGovFeilds.getSubDivisionId()) + ","
				+ " \"subDivision\": " + addQuotes(newGovFeilds.getSubDivision()) + ","
				+ " \"typeOfProject\": " + addQuotes(newGovFeilds.getTypeOfProject()) + ","
				+ " \"classificationType\": " + addQuotes(newGovFeilds.getClassificationType()) + "}')\n"
				+ "where data->'projectDetails'->>'projectName' = '" + projectName + "'";

		try {
			Query q = em.createNativeQuery(updateQuery);
			q.executeUpdate();
			updateResponse.setSuccess("SUCCESS");
			updateResponse.setErrors(new ArrayList<>());
			updateResponse.setWarnings(new ArrayList<>());
			log.info("Governance details updated successfully for project {} ", projectName);
		} catch (Exception e) {
			MessageDescription errMsg = new MessageDescription("Failed while updating the governance details.");
			errors.add(errMsg);
			log.error("Governance details Failed while updating the governance with Exception {} ", e.getMessage());
		}
		return updateResponse;

	}

	@Override
	public List<CodeServerWorkspaceNsql> findAllByUniqueLiteral()
	{
		String query = "SELECT DISTINCT ON (jsonb_extract_path_text(data, 'projectDetails', 'projectName')) *"
		+" FROM workspace_nsql"
		+" WHERE jsonb_extract_path_text(data, 'status') != 'DELETED'";
		Query q = em.createNativeQuery(query, CodeServerWorkspaceNsql.class);
		List<CodeServerWorkspaceNsql> result = q.getResultList();
        return result != null ? result : new ArrayList<>();
	}

	   @Override
		public List<CodeServerWorkspaceNsql> findByRecipeName(String userId, int limit, int offset, String recipeName) {
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
		Predicate p3 = cb.equal(cb.lower(
				cb.function("jsonb_extract_path_text", String.class, root.get("data"),
				cb.literal("projectDetails"), cb.literal("recipeDetails"), cb.literal("recipeName"))
				), recipeName.toLowerCase());
		Predicate pMain = cb.and(p1,p2,p3);
		cq.where(pMain);		
		cq.orderBy(cb.asc(cb.function("jsonb_extract_path_text", String.class, root.get("data"), cb.literal("projectDetails"), cb.literal("projectName"))));
		TypedQuery<CodeServerWorkspaceNsql> getAllQuery = em.createQuery(getAll);
		if (offset >= 0)
			getAllQuery.setFirstResult(offset);
		if (limit > 0)
			getAllQuery.setMaxResults(limit);
		return getAllQuery.getResultList();
	
		}

}

