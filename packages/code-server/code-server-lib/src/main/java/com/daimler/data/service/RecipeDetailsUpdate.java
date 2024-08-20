package com.daimler.data.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerProjectDetails;
import com.daimler.data.db.json.CodeServerRecipeDetails;
import com.daimler.data.db.json.CodeServerWorkspace;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepositoryImpl;
import com.daimler.data.db.repo.workspace.WorkspaceRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RecipeDetailsUpdate {

    @Autowired
    private WorkspaceCustomRepositoryImpl customRepo;

    @Autowired
    WorkspaceRepository repo;

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
                               CodeServerRecipeDetails recipeDetails =  projectDetails.getRecipeDetails();
                               if(recipeDetails.getRecipeId() != null) {
                                List<String> additionalServices =  new ArrayList<>();
                                if(recipeDetails.getRecipeName().isBlank()) {
                                    switch (recipeDetails.getRecipeId()) {
                                        case "springboot":
                                            recipeDetails.setRecipeName("Microservice using Spring Boot with Gradle (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                        case "springbootwithmaven":
                                            recipeDetails.setRecipeName("Microservice using Spring Boot with Maven (Debian 11 OS, 2GB RAM, 1CPU)");
                                            additionalServices.add("postgres");
                                            recipeDetails.setAdditionalServices(additionalServices);
                                            break;
                                        case "quarkus":
                                            recipeDetails.setRecipeName("Microservice using QUARKUS (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                        case "micronaut":
                                            recipeDetails.setRecipeName("Microservice using MICRONAUT (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                        case "py-fastapi":
                                            recipeDetails.setRecipeName("Microservice using Python FastAPI (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                        case "dash":
                                            recipeDetails.setRecipeName("Dash Python (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                        case "streamlit":
                                            recipeDetails.setRecipeName("Streamlit ChatApp with GenAI Nexus (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                        case "expressjs":
                                            recipeDetails.setRecipeName("Microservice using Express - Node.js (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                        case "nestjs":
                                            recipeDetails.setRecipeName("Microservice using NestJS - Node.js (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                        case "react":
                                            recipeDetails.setRecipeName("React SPA (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                        case "angular":
                                            recipeDetails.setRecipeName("Angular SPA (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                        case "vuejs":
                                            recipeDetails.setRecipeName("Vue3 Webpack SPA (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                        case "public-dna-frontend":
                                            recipeDetails.setRecipeName("DnA Frontend (Debian 11 OS, 6GB RAM, 2CPU)");
                                            break;
                                        case "public-dna-backend":
                                            recipeDetails.setRecipeName("DnA Backend (Debian 11 OS, 4GB RAM, 1CPU)");
                                            break;
                                        case "public-dna-report-backend":
                                            recipeDetails.setRecipeName("DnA Report Backend (Debian 11 OS, 4GB RAM, 1CPU)");
                                            break;
                                        case "public-dna-codespace-backend":
                                            recipeDetails.setRecipeName("DnA Code Space Backend (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                        case "public-dna-code-space-mfe":
                                            recipeDetails.setRecipeName("DnA Code Space Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)");
                                            break;
                                        case "public-dna-malware-scanner":
                                            recipeDetails.setRecipeName("DnA Malware Scanner (Debian 11 OS, 4GB RAM, 1CPU)");
                                            break;
                                        case "public-dna-storage-mfe":
                                            recipeDetails.setRecipeName("DnA Storage Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)");
                                            break;
                                        case "public-dna-storage-backend":
                                            recipeDetails.setRecipeName("DnA Storage Backend (Debian 11 OS, 4GB RAM, 1CPU)");
                                            break;
                                        case "public-dna-chronos-mfe":
                                            recipeDetails.setRecipeName("DnA Chronos Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)");
                                            break;
                                        case "public-dna-chronos-backend":
                                            recipeDetails.setRecipeName("DnA Chronos Backend (Debian 11 OS, 4GB RAM, 1CPU)");
                                            break;
                                        case "public-dna-data-product-mfe":
                                            recipeDetails.setRecipeName("DnA Data Product Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)");
                                            break;
                                        case "public-dna-data-product-backend":
                                            recipeDetails.setRecipeName("DnA Data Product Backend (Debian 11 OS, 4GB RAM, 1CPU)");
                                            break;
                                        case "public-dna-dataiku-backend":
                                            recipeDetails.setRecipeName("DnA DSS Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)");
                                            break;
                                        case "public-dna-airflow-backend":
                                            recipeDetails.setRecipeName("DnA Airflow Backend (Debian 11 OS, 4GB RAM, 1CPU)");
                                            break;
                                        case "public-dna-modal-registry-backend":
                                            recipeDetails.setRecipeName("DnA Modal Registry Backend (Debian 11 OS, 4GB RAM, 1CPU)");
                                            break;
                                        case "public-dna-trino-backend":
                                            recipeDetails.setRecipeName("DnA Trino Backend (Debian 11 OS, 4GB RAM, 1CPU)");
                                            break;
                                        case "public-dna-nass":
                                            recipeDetails.setRecipeName("DnA Notification Backend (Debian 11 OS, 4GB RAM, 1CPU)");
                                            break; 
                                        case "public-dna-authenticator-backend":
                                            recipeDetails.setRecipeName("DnA Authenticator Backend (Debian 11 OS, 4GB RAM, 1CPU)");
                                            break; 
                                        case "public-dna-matomo-mfe":
                                            recipeDetails.setRecipeName("DnA Matomo Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)");
                                            break; 
                                        case "public-dna-matomo-backend":
                                            recipeDetails.setRecipeName("DnA Matomo Backend (Debian 11 OS, 4GB RAM, 1CPU)");
                                            break; 
                                        case "public-dna-fabric-mfe":
                                            recipeDetails.setRecipeName("DnA Data Lakehouse Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)");
                                            break;
                                        case "public-dna-datalake-mfe":
                                            recipeDetails.setRecipeName("DnA Data Lakehouse Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)");
                                            break;
                                        case "public-dna-fabric-backend":
                                            recipeDetails.setRecipeName("DnA Fabric Backend (Debian 11 OS, 6GB RAM, 2CPU)");
                                            break;
                                        case "public-dna-dataentry-mfe":
                                            recipeDetails.setRecipeName("DnA Data Entry as a Service Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)");
                                            break;
                                        case "public-user-defined":
                                            recipeDetails.setRecipeName("Recipe from Public Github(https://github.com/) (Debian 11 OS, 6GB RAM, 2CPU)");
                                            break;
                                        case "private-user-defined":
                                            recipeDetails.setRecipeName("Recipe from Private Github(https://"+gitUrl+") (Debian 11 OS, 4GB RAM, 2CPU)");
                                            break;
                                        default:
                                            recipeDetails.setRecipeName("Plain or Empty (Debian 11 OS, 2GB RAM, 1CPU)");
                                            break;
                                    }
                                }
                            } else if(recipeDetails.getRecipeId() == null) {
                                log.info("recipe name onboard failed for workpsace id : "+record.getWorkspaceId()); 
                            }
                        }
                    }
                } 
            }
                    repo.saveAll(listOfCodespaces);
                }
                log.info("Successfully migrated all codespaces");
            } else {
                log.info("No records in database");
            }
        }

     }
    


