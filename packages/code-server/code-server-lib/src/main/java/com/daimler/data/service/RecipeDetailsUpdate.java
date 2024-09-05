package com.daimler.data.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerProjectDetails;
import com.daimler.data.db.json.CodeServerRecipeDetails;
import com.daimler.data.db.json.CodeServerWorkspace;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepositoryImpl;
import com.daimler.data.db.repo.workspace.WorkspaceRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecipeDetailsUpdate {

    @Autowired
    private WorkspaceCustomRepositoryImpl customRepo;

    @Autowired
	private WorkspaceCustomRepository workspaceCustomRepository;

    @Value("${codeServer.run.init}")
    private boolean runInit;

    @Value("${codeServer.git.orguri}")
    private String gitUrl;

    @PostConstruct
    public void init() {
        if (runInit) {
            List<CodeServerWorkspaceNsql> listOfCodespaces = customRepo.findAll();
            if (listOfCodespaces != null) {
                for (CodeServerWorkspaceNsql entity : listOfCodespaces) {
                    CodeServerWorkspace record = entity.getData();
                    if(record!=null){
                        if(record.getProjectDetails()!=null) {
                            CodeServerProjectDetails projectDetails = record.getProjectDetails();
                            if(projectDetails.getRecipeDetails()!=null) {
                                log.info("projectDetails "+record.getWorkspaceId());
                               CodeServerRecipeDetails recipeDetails =  projectDetails.getRecipeDetails();
                               if(recipeDetails != null && recipeDetails.getRecipeId() != null) {
                                List<String> additionalServices =  new ArrayList<>();
                                    switch (recipeDetails.getRecipeId()) {
                                        case "springboot":
                                            recipeDetails.setRecipeName("Microservice using Spring Boot with Gradle");
                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            break;
                                        case "springbootwithmaven":
                                            recipeDetails.setRecipeName("Microservice using Spring Boot with Maven");
                                            additionalServices.add("postgres");
                                            recipeDetails.setAdditionalServices(additionalServices);
                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            break;
                                        case "quarkus":
                                            recipeDetails.setRecipeName("Microservice using QUARKUS");
                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            break;
                                        case "micronaut":
                                            recipeDetails.setRecipeName("Microservice using MICRONAUT");
                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            break;
                                        case "py-fastapi":
                                            recipeDetails.setRecipeName("Microservice using Python FastAPI");
                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            break;
                                        case "dash":
                                            recipeDetails.setRecipeName("Dash Python");                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            break;
                                        case "streamlit":
                                            recipeDetails.setRecipeName("Streamlit ChatApp with GenAI Nexus");
                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            break;
                                        case "expressjs":
                                            recipeDetails.setRecipeName("Microservice using Express - Node.js");
                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            break;
                                        case "nestjs":
                                            recipeDetails.setRecipeName("Microservice using NestJS - Node.js");
                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            break;
                                        case "react":
                                            recipeDetails.setRecipeName("React SPA");
                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            break;
                                        case "angular":
                                            recipeDetails.setRecipeName("Angular SPA");
                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            break;
                                        case "vuejs":
                                            recipeDetails.setRecipeName("Vue3 Webpack SPA");
                                            recipeDetails.setDeployEnabled(Boolean.TRUE);
                                            break;
                                        case "public-dna-frontend":
                                            recipeDetails.setRecipeName("DnA Frontend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-backend":
                                            recipeDetails.setRecipeName("DnA Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-report-backend":
                                            recipeDetails.setRecipeName("DnA Report Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-codespace-backend":
                                            recipeDetails.setRecipeName("DnA Code Space Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-code-space-mfe":
                                            recipeDetails.setRecipeName("DnA Code Space Micro Frontend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-malware-scanner":
                                            recipeDetails.setRecipeName("DnA Malware Scanner");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-storage-mfe":
                                            recipeDetails.setRecipeName("DnA Storage Micro Frontend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-storage-backend":
                                            recipeDetails.setRecipeName("DnA Storage Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-chronos-mfe":
                                            recipeDetails.setRecipeName("DnA Chronos Micro Frontend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-chronos-backend":
                                            recipeDetails.setRecipeName("DnA Chronos Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-data-product-mfe":
                                            recipeDetails.setRecipeName("DnA Data Product Micro Frontend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-data-product-backend":
                                            recipeDetails.setRecipeName("DnA Data Product Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-dataiku-backend":
                                            recipeDetails.setRecipeName("DnA DSS Micro Frontend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-airflow-backend":
                                            recipeDetails.setRecipeName("DnA Airflow Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-modal-registry-backend":
                                            recipeDetails.setRecipeName("DnA Modal Registry Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-trino-backend":
                                            recipeDetails.setRecipeName("DnA Trino Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-nass":
                                            recipeDetails.setRecipeName("DnA Notification Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break; 
                                        case "public-dna-authenticator-backend":
                                            recipeDetails.setRecipeName("DnA Authenticator Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break; 
                                        case "public-dna-matomo-mfe":
                                            recipeDetails.setRecipeName("DnA Matomo Micro Frontend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break; 
                                        case "public-dna-matomo-backend":
                                            recipeDetails.setRecipeName("DnA Matomo Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break; 
                                        case "public-dna-fabric-mfe":
                                            recipeDetails.setRecipeName("DnA Data Lakehouse Micro Frontend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-datalake-mfe":
                                            recipeDetails.setRecipeName("DnA Data Lakehouse Micro Frontend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-fabric-backend":
                                            recipeDetails.setRecipeName("DnA Fabric Backend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-dna-dataentry-mfe":
                                            recipeDetails.setRecipeName("DnA Data Entry as a Service Micro Frontend");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "public-user-defined":
                                            recipeDetails.setRecipeName("Recipe from Public Github(https://github.com/)");
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "private-user-defined":
                                            if(recipeDetails.getRecipeName() == null || recipeDetails.getRecipeName().isBlank()) {
                                                recipeDetails.setRecipeName("Recipe from Private Github(https://"+gitUrl+")");
                                            }
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        case "default":
                                            if(recipeDetails.getRecipeName() == null || recipeDetails.getRecipeName().isBlank()) {
                                                recipeDetails.setRecipeName("Plain or Empty");
                                            }
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                            break;
                                        default:
                                            recipeDetails.setDeployEnabled(Boolean.FALSE);
                                    }
                            } else if(recipeDetails.getRecipeId() == null) {
                                log.info("recipe name onboard failed for workpsace id : "+record.getWorkspaceId()); 
                            }
                            
                        }
                    }
                } 
            }
            this.saveToRepo(listOfCodespaces);
        }
        log.info("Successfully migrated all codespaces");
        } else {
            log.info("No records in database");
        }
    }

    private void saveToRepo(List<CodeServerWorkspaceNsql> listOfCodespaces ) {
        String workspaceId = null;
        for(CodeServerWorkspaceNsql codeServerNsql:listOfCodespaces) {
            workspaceId = codeServerNsql.getData().getWorkspaceId();
            log.info("workspaceId "+workspaceId);
            workspaceCustomRepository.updateRecipeDetails(codeServerNsql);
        }

    }
}


