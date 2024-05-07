package com.mb.dna.datalakehouse.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.daimler.data.assembler.TrinoDataLakeAssembler;
import com.mb.dna.datalakehouse.db.entities.TrinoAccessNsql;
import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;
import com.mb.dna.datalakehouse.db.jsonb.DataLakeTableCollabDetails;
import com.mb.dna.datalakehouse.db.jsonb.DatalakeTable;
import com.mb.dna.datalakehouse.db.jsonb.TrinoAccess;
import com.mb.dna.datalakehouse.db.jsonb.TrinoDataLakeProject;
import com.mb.dna.datalakehouse.db.jsonb.TrinoSchemaRules;
import com.mb.dna.datalakehouse.db.jsonb.TrinoTableRules;
import com.mb.dna.datalakehouse.db.repo.TrinoAccessCustomRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoAccessRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoDataLakeCustomRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoDataLakeRepo;

import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(value="datalake.table2projectCollab.migration", havingValue = "true", matchIfMissing = false)
@Component
@Slf4j
public class TableToProjectCollabMigrationService {

	@Autowired
	private TrinoDataLakeCustomRepo customRepo;
	
	@Autowired
	private TrinoDataLakeRepo jpaRepo;
	
	@Autowired
	private TrinoAccessCustomRepo accessCustomRepo;
	
	@Autowired
	private TrinoAccessRepo accessJpaRepo;
	
	@Autowired
	private TrinoDataLakeAssembler assembler;
	
	private static List<String> readPrivileges = Arrays.asList(new String[] {"SELECT"});
	private static List<String> writePrivileges = Arrays.asList(new String[] {"SELECT","INSERT","DELETE","UPDATE"});
	private static List<String> ownerShipPrivileges = Arrays.asList(new String[] {"SELECT","INSERT","DELETE","UPDATE","OWNERSHIP"});
	
	@PostConstruct
	public void init() {
		
		List<TrinoDataLakeNsql> updatedEntities = new ArrayList<>();
		TrinoAccessNsql accessNsql = null;
		TrinoAccess accessRules = null;
		TrinoAccess updatedAccessRules = null;
		
		//fetching records from db
		List<TrinoDataLakeNsql> entities = jpaRepo.findAll();
		log.info("Fetched all datalake records from db successfully for migration");
		
		List<TrinoAccessNsql> accessRecords = accessJpaRepo.findAll();
		if(accessRecords!=null && !accessRecords.isEmpty() && accessRecords.get(0)!=null && accessRecords.get(0).getData()!=null) {
			accessNsql = accessRecords.get(0);
			accessRules = accessRecords.get(0).getData();
			updatedAccessRules = accessRecords.get(0).getData();
		}
		log.info("Fetched access rules record from db successfully for migration");
		
		//for each record, transform data and add to list
		if(entities!=null && !entities.isEmpty()) {
			for(TrinoDataLakeNsql entity : entities) {
				TrinoDataLakeProject data = entity.getData();
				log.info("Migrating record {} {} ", entity.getId(), entity.getData().getProjectName());
				List<DataLakeTableCollabDetails> projectCollabs = new ArrayList<>();
				HashMap<String, DataLakeTableCollabDetails> projectCollabMap = new HashMap<String, DataLakeTableCollabDetails>();
				List<DatalakeTable> tables = data.getTables();
				String catalog = data.getCatalogName();
				String schema = data.getSchemaName();
				
				if(tables!=null && !tables.isEmpty()) {
					for(DatalakeTable table : tables) {
						List<DataLakeTableCollabDetails> tableCollabs = table.getCollabs();
						if(tableCollabs !=null && !tableCollabs.isEmpty()) {
							for(DataLakeTableCollabDetails collab : tableCollabs) {
								String userId = collab.getCollaborator().getId();
								if(projectCollabMap.get(userId)==null || collab.getHasWritePermission()) {
									projectCollabMap.put(userId, collab);
								}
							}
						}
						
					}
					projectCollabs = (List<DataLakeTableCollabDetails>) projectCollabMap.values();
				}
				
				data.setCollabs(projectCollabs);
				entity.setData(data);
				updatedEntities.add(entity);
				
				// remove existing schema rule
				updatedAccessRules.getSchemas().removeIf(x-> x.getCatalog()!=null && x.getCatalog().equalsIgnoreCase(catalog) 
						&& x.getSchema()!=null && x.getSchema().equalsIgnoreCase(schema));
				log.info("Removed schema rule of {} for record {} {} during migration", schema,  entity.getId(), entity.getData().getProjectName());
				//remove existing table rules
				updatedAccessRules.getTables().removeIf(x-> x.getCatalog()!=null && x.getCatalog().equalsIgnoreCase(catalog)  
						&& x.getSchema()!=null && x.getSchema().equalsIgnoreCase(schema));
				log.info("Removed all existing table rules including tech user table rule if exists, of record {} {} during migration", entity.getId(), entity.getData().getProjectName());
				
				List<String> schemaCollaborators = new ArrayList<>();
				List<String> readCollabs = new ArrayList<>();
				List<String> writeCollabs = new ArrayList<>();
				
				//adding owner and tech user to schema collabs
				schemaCollaborators.add(data.getCreatedBy().getId());
				if(data.getTechUserClientId()!=null)
					schemaCollaborators.add(data.getTechUserClientId());
				
				if(projectCollabs!=null && !projectCollabs.isEmpty()) {
					for(DataLakeTableCollabDetails collab: projectCollabs) {
						String collabId = collab.getCollaborator().getId();
						if(collab.getHasWritePermission()) {
							writeCollabs.add(collabId);
						}else {
							readCollabs.add(collabId);
						}
						schemaCollaborators.add(collabId);
					}
				}
				//adding new schema rule
				TrinoSchemaRules schemaRules = new TrinoSchemaRules();
				schemaRules.setCatalog(catalog);
				schemaRules.setOwner(false);
				schemaRules.setSchema(schema);
				schemaRules.setUser(String.join("|", schemaCollaborators));
				updatedAccessRules.getSchemas().add(schemaRules);
				log.info("Added new schema rule for record {} {} during migration", entity.getId(), entity.getData().getProjectName());
				
				//adding new table rule for read collabs
				TrinoTableRules readAccessRules = new TrinoTableRules();
				readAccessRules.setCatalog(catalog);
				readAccessRules.setPrivileges(readPrivileges);
				readAccessRules.setSchema(schema);
				readAccessRules.setTable(".*");
				readAccessRules.setUser(String.join("|", readCollabs));
				updatedAccessRules.getTables().add(readAccessRules);
				log.info("Added new read collabs table rule for record {} {} during migration", entity.getId(), entity.getData().getProjectName());
				
				List<String> ownershipUsers = new ArrayList<>();
				ownershipUsers.add(data.getCreatedBy().getId());
				if(data.getTechUserClientId()!=null)
					ownershipUsers.add(data.getTechUserClientId());
				ownershipUsers.addAll(writeCollabs);
				//adding ownership rights at tables level for owner and techuser and readwrite collabs
				TrinoTableRules techUserTableRule = new TrinoTableRules();
				techUserTableRule.setCatalog(catalog);
				techUserTableRule.setSchema(schema);
				techUserTableRule.setUser(String.join("|", ownershipUsers));
				techUserTableRule.setTable(null);
				techUserTableRule.setPrivileges(ownerShipPrivileges);
				updatedAccessRules.getTables().add(techUserTableRule);
				log.info("Added updated owner n techuser table rule for record {} {} during migration", entity.getId(), entity.getData().getProjectName());
				
			}
			
			accessNsql.setData(updatedAccessRules);
			accessJpaRepo.save(accessNsql);
			
			jpaRepo.saveAllAndFlush(updatedEntities);
			
		}
	}
	
	
}
