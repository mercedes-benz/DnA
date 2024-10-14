package com.daimler.data.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.annotation.PostConstruct;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.daimler.data.application.client.GitClient;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerProjectDetails;
import com.daimler.data.db.json.CodeServerRecipeDetails;
import com.daimler.data.db.json.CodeServerWorkspace;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRecipeRepo;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepositoryImpl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RepoSoftwareFileUpdate {

    @Autowired
    private WorkspaceCustomRepositoryImpl customRepo;

    @Value("${codeServer.run.init}")
    private boolean runInit;

    @Value("${codeServer.git.orguri}")
    private String gitOrgUrl;

    @Value("${codeServer.git.orgname}")
    private String orgName;

    @Value("${codeserver.recipe.software.filename}")
	private String gitFileName;
  
    @Autowired
    private GitClient gitClient;

    @Autowired
    private WorkspaceCustomRecipeRepo workspaceCustomRecipeRepo;

    @PostConstruct
    public void init() {
        if (runInit) {
            List<CodeServerWorkspaceNsql> listOfCodespaces = customRepo.findAll();
            if (listOfCodespaces != null) {
                for (CodeServerWorkspaceNsql entity : listOfCodespaces) {
                    CodeServerWorkspace record = entity.getData();
                    if (record != null) {
                        String gitUrl = null;
                        if (record.getProjectDetails() != null) {
                            CodeServerProjectDetails projectDetails = record.getProjectDetails();
                            if (projectDetails.getRecipeDetails() != null) {
                                CodeServerRecipeDetails recipeDetails = projectDetails.getRecipeDetails();
                                if (recipeDetails != null && recipeDetails.getRecipeId() != null) {
                                    List<String> softwares = new ArrayList<>();
                                    log.info("Migration started for the Workspace: " + record.getWorkspaceId());
                                    switch (recipeDetails.getRecipeId()) {
                                        case "springboot":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "springbootwithmaven":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("MAVEN-3.9.6");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "quarkus":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "micronaut":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "py-fastapi":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("PYTHON-3.11.9");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "dash":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("PYTHON-3.11.9");
                                            softwares.add("POETRY-1.8.3");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "streamlit":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("PYTHON-3.11.9");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "expressjs":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "nestjs":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "react":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "angular":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            softwares.add("ANGULAR-15.2.7");

                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "vuejs":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-frontend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-report-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-codespace-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-code-space-mfe":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-malware-scanner":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-storage-mfe":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-storage-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-chronos-mfe":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-chronos-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-data-product-mfe":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-data-product-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-dataiku-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-airflow-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-modal-registry-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-trino-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-nass":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-authenticator-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-matomo-mfe":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-matomo-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-fabric-mfe":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-datalake-mfe":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-fabric-backend":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("JAVA-11.0.23");
                                            softwares.add("GRADLE-7.6.1");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                        case "public-dna-dataentry-mfe":
                                            gitUrl = "https://" + gitOrgUrl + orgName + "/"
                                                    + projectDetails.getProjectName()+"/";
                                            softwares.add("NODE-20.14.0");
                                            softwares.add("YARN-1.22.22");
                                            this.addSoftwareFileToGit(gitUrl, softwares);
                                            break;
                                    }
                                } else if (recipeDetails.getRecipeId() == null) {
                                    log.info("repo onborad failed for workpsace id : " + record.getWorkspaceId());
                                }

                            }
                        }
                    }
                }
            }
            log.info("Successfully migrated all codespaces repo");
        } else {
            log.info("No records in database");
        }
    }

    private void addSoftwareFileToGit(String gitHubUrl, List<String> softwares) {
        HttpStatus status = null;
        try {
            String repoName = null;
            String softwareFileName = null;
            String gitUrl = null;
            String repoOwner = null;
            String SHA = null;
            String encodedFileContent = null;
            StringBuffer fileContent = new StringBuffer();
            if (gitHubUrl.contains(".git")) {
                gitHubUrl = gitHubUrl.replaceAll("\\.git$", "/");
            }
            String[] codespaceSplitValues = gitHubUrl.split("/");
            int length = codespaceSplitValues.length;
            repoName = codespaceSplitValues[length - 1];
            repoOwner = codespaceSplitValues[length - 2];
            gitUrl = gitHubUrl.replace("/" + repoOwner, "");
            gitUrl = gitUrl.replace("/" + repoName, "");
            log.info("repo creation for url "+gitHubUrl);
            JSONObject jsonResponse = gitClient.readFileFromGit(repoName, repoOwner, gitUrl, gitFileName);
            if (jsonResponse != null && jsonResponse.has("name") && jsonResponse.has("content")) {
                softwareFileName = jsonResponse.getString("name");
                SHA = jsonResponse.has("sha") ? jsonResponse.getString("sha") : null;
                log.info("sh file retriveing was successfull from Git skiping since .sh file is present");
            } else {
                for (String software : softwares) {
                    String additionalProperties = workspaceCustomRecipeRepo.findBySoftwareName(software);
                    fileContent.append(additionalProperties);
                }
                if (fileContent.toString().contains("dotnet")) {
                    fileContent.append(
                            "\ncode-server --install-extension ms-dotnettools.vscode-dotnet-runtime\ncode-server --install-extension aliasadidev.nugetpackagemanagergui");
                }
                fileContent.append(
                        "\ncode-server --install-extension mtxr.sqltools-driver-pg\ncode-server --install-extension mtxr.sqltools\ncode-server --install-extension cweijan.vscode-database-client2\ncode-server --install-extension cweijan.vscode-redis-client\n");
                encodedFileContent = Base64.getEncoder().encodeToString(fileContent.toString().getBytes());
                if (encodedFileContent != null) {
                    status = gitClient.createOrValidateSoftwareInGit(repoName, repoOwner,SHA, gitUrl,
                            encodedFileContent);
                    if (status.is2xxSuccessful()) {
                        log.info("Software creation was successfull in Git for migration for repo "+repoName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("error occured while writing file into git for migration", e);
        }
    }
}
