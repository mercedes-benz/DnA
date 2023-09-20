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

package com.daimler.dna.airflow.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RevertCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.daimler.dna.airflow.dto.AirflowDagUpdateVO;
import com.daimler.dna.airflow.dto.AirflowDagVo;
import com.daimler.dna.airflow.dto.AirflowGITResponse;
import com.daimler.dna.airflow.dto.AirflowProjectUserVO;
import com.daimler.dna.airflow.dto.AirflowProjectVO;
import com.daimler.dna.airflow.exceptions.MessageDescription;

@Component
public class AirflowGitClientImpl implements AirflowGitClient {

	private static Logger LOGGER = LoggerFactory.getLogger(AirflowGitClientImpl.class);

	@Value("${git.uri}")
	private String gitUri;

	@Value("${git.mountPath}")
	private String gitMountPath;

	@Value("${git.token}")
	private String gitToken;

	@Value("${git.branch}")
	private String gitBranch;

	@Value("${git.dagPath}")
	private String dagPath;

	@Value("${git.dagFileExtension}")
	private String dagFileExtension;

	// private static final String localBranch = "test_";
	private static final String ORIGIN = "origin";

	public AirflowGitClientImpl() {
		super();
	}

	/**
	 * TO push Dag to airflow git repository
	 * 
	 * @param data
	 */
	@Override
	public List<MessageDescription> createAirflowDags(AirflowProjectVO airflowProjectVO,
			AirflowProjectUserVO currentUser) {
		LOGGER.trace("Entering pushDag");
		List<MessageDescription> errors = null;
		try {
			UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("token",
					gitToken);
			Path localDagPath = Paths.get(gitMountPath + File.separator + dagPath);
			Git git = gitClone(credentialsProvider);
			if (git != null) {
//				CheckoutCommand checkoutCommand = git.checkout();
//				CreateBranchCommand createBranchCommand = git.branchCreate();
				PushCommand pushCommand = git.push();
				AddCommand addCommand = git.add();
				String gitCommitMsg = "Adding DAGs:";
				/* temporary code start to create a new branch and push the changes */
//				int min = 1;
//				int max = 100;
//				int version = (int) (Math.random() * (max - min + 1) + min);
//
//				/* Creating Branch */
//				createBranchCommand = git.branchCreate();
//				createBranchCommand.setName(localBranch + version).call();
//
//				// checkout
//				checkoutCommand.setName(localBranch + version);
//				checkoutCommand.call();
				/* temporary code end */

				if (!ObjectUtils.isEmpty(airflowProjectVO.getDags())) {
					String dagFileName = null;
					for (AirflowDagVo dag : airflowProjectVO.getDags()) {
						dagFileName = dag.getDagName() + "." + dagFileExtension;
						gitCommitMsg = gitCommitMsg + " " + dagFileName;
						LOGGER.debug("DAG path {} is {}", localDagPath.toString() + File.separator + dagFileName, new File(localDagPath.toString() + File.separator + dagFileName).exists());
//						if (new File(localDagPath.toString() + File.separator + dagFileName).exists()) {
//							LOGGER.debug("DAG already existing:{}", dagFileName);
//							errors = setErrors("DAG already existing: " + dagFileName);
//							break;
//						} else {
							LOGGER.debug("Writing to:{}", dagFileName);
							Files.writeString(localDagPath.resolve(dagFileName), dag.getDagContent());
//						}
					}
				}
				if (Objects.isNull(errors)) {

					// git add
					LOGGER.debug("Processing git add.");
					addCommand.addFilepattern(localDagPath.getFileName().toString()).call();

					// git commit
					gitCommitMsg = gitCommitMsg + " for Project: " + airflowProjectVO.getProjectName();
					LOGGER.debug("Processing git commit.");
					git.commit().setMessage(gitCommitMsg).setAuthor(currentUser.getUsername(), currentUser.getEmail())
							.call();

					// git push
					LOGGER.debug("Processing git push.");
					pushCommand.setRemote(ORIGIN);
					// pushCommand.setRefSpecs(new RefSpec(localBranch + version + ":" + localBranch
					// + version));
					pushCommand.setRefSpecs(new RefSpec(gitBranch + ":" + gitBranch));
					pushCommand.setCredentialsProvider(credentialsProvider);
					pushCommand.call();
				}
			}
		} catch (IllegalStateException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		} catch (GitAPIException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		} catch (IOException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		}

		return errors;
	}

	/**
	 * set errors
	 * 
	 * @param error
	 * @return List<MessageDescription>
	 */
	private List<MessageDescription> setErrors(String error) {
		List<MessageDescription> errors = new ArrayList<MessageDescription>();
		errors.add(new MessageDescription(error));
		return errors;
	}

	/**
	 * To clone git repository if already exists pull repository
	 * 
	 * @param credentialsProvider
	 * @return Git
	 * @throws GitAPIException
	 * @throws IOException
	 */
	private Git gitClone(UsernamePasswordCredentialsProvider credentialsProvider) throws GitAPIException, IOException {
		LOGGER.trace("Entering gitClone");
		PullResult result = null;
		Git git = null;
		try {
			if (new File(gitMountPath).exists()) {
				LOGGER.debug("Repository already exist pulling from git..");
				git = Git.open(new File(gitMountPath));
				CheckoutCommand checkoutCommand = git.checkout();
				checkoutCommand = git.checkout();
				checkoutCommand.setName(gitBranch);
				checkoutCommand.call();
				result = git.pull().setCredentialsProvider(credentialsProvider).setRemote(ORIGIN)
						.setRemoteBranchName(gitBranch).call();
			} else {
				LOGGER.debug("Cloning from repository..");
				git = Git.cloneRepository().setURI(gitUri).setDirectory(new File(gitMountPath))
						.setCredentialsProvider(credentialsProvider).setBranch(gitBranch).call();
			}
		} catch (GitAPIException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			throw e;
		} catch (IOException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			throw e;
		}
		LOGGER.trace("Returning from gitClone");
		return git;
	}

	/**
	 * To fetch Dag by given dagId
	 * 
	 * @param dagId
	 * @return dagCode
	 */
	@Override
	public String getDagById(String dagId) {
		LOGGER.trace("Entering getDagById");
		String dagFileContent = null;
		UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("token",
				gitToken);
		try {
			Git git = gitClone(credentialsProvider);
			String dagFileName = gitMountPath + File.separator + dagPath + File.separator + dagId + "."
					+ dagFileExtension;
			if (git != null && new File(dagFileName).isFile()) {
				LOGGER.debug("Fetching file content");
				dagFileContent = Files.readString(Paths.get(dagFileName));
			}
		} catch (IOException e) {
			LOGGER.error("error occured: {}", e.getMessage());
		} catch (GitAPIException e) {
			LOGGER.error("error occured: {}", e.getMessage());
		}
		LOGGER.trace("Returning from getDagById");
		return dagFileContent;
	}

	/**
	 * <p>
	 * This method exists to be used for update airflow project's DAGs if existing
	 * else create a new one
	 * </p>
	 * 
	 * @param airflowProjectVO
	 * @param currentUser
	 * 
	 * @return {@code List<MessageDescription>} if error exists otherwise
	 *         {@code null}
	 */
	@Override
	public AirflowGITResponse updateAirflowDags(AirflowProjectVO airflowProjectVO, AirflowProjectUserVO currentUser) {
		AirflowGITResponse res = new AirflowGITResponse();
		List<MessageDescription> errors = null;
		try {
			UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("token",
					gitToken);
			Path localDagPath = Paths.get(gitMountPath + File.separator + dagPath);
			Git git = gitClone(credentialsProvider);
			if (git != null) {
				PushCommand pushCommand = git.push();
				AddCommand addCommand = git.add();
				String gitCommitMsg = "Updating DAGs:";
				if (!ObjectUtils.isEmpty(airflowProjectVO.getDags())) {
					String dagFileName = null;
					for (AirflowDagVo dag : airflowProjectVO.getDags()) {
						dagFileName = dag.getDagName() + "." + dagFileExtension;
						gitCommitMsg = gitCommitMsg + " " + dagFileName;
						LOGGER.debug("Writing to:{}", dagFileName);
						Files.writeString(localDagPath.resolve(dagFileName), dag.getDagContent());
					}
				}
				if (Objects.isNull(errors)) {
					// git add
					LOGGER.debug("Processing GIT add..");
					addCommand.addFilepattern(localDagPath.getFileName().toString()).call();

					// git commit
					LOGGER.debug("Processing GIT commit..");
					gitCommitMsg = gitCommitMsg + " for Project: " + airflowProjectVO.getProjectName();
					git.commit().setMessage(gitCommitMsg).setAuthor(currentUser.getUsername(), currentUser.getEmail())
							.call();

					// Fetching RevCommit
					RevCommit latestCommit = git.log().setMaxCount(1).call().iterator().next();
					res.setGitCommitId(latestCommit);

					// git push
					LOGGER.debug("Processing GIT push..");
					pushCommand.setRemote(ORIGIN);
					pushCommand.setRefSpecs(new RefSpec(gitBranch + ":" + gitBranch));
					pushCommand.setCredentialsProvider(credentialsProvider);
					pushCommand.call();
				}
			}
		} catch (IllegalStateException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		} catch (GitAPIException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		} catch (IOException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		}
		res.setErrors(errors);
		return res;
	}

	@Override
	public List<MessageDescription> updateAirflowDag(AirflowDagUpdateVO dag, AirflowProjectUserVO currentUser) {
		List<MessageDescription> errors = null;
		try {
			UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("token",
					gitToken);
			Path localDagPath = Paths.get(gitMountPath + File.separator + dagPath);
			Git git = gitClone(credentialsProvider);
			if (git != null) {
				PushCommand pushCommand = git.push();
				AddCommand addCommand = git.add();
				String gitCommitMsg = "Updating DAG:";
				String dagFileName = dag.getDagName() + "." + dagFileExtension;
				if (new File(localDagPath.toString() + File.separator + dagFileName).isFile()) {
					LOGGER.debug("Writing to:{}", dag.getDagName());
					gitCommitMsg = gitCommitMsg + " " + dagFileName;
					Files.writeString(localDagPath.resolve(dagFileName), dag.getDagContent());
				} else {
					LOGGER.debug("DAG not found:{}", dag.getDagName());
					errors = setErrors("DAG not found: " + dag.getDagName());
				}
				if (Objects.isNull(errors)) {
					// git add
					LOGGER.debug("Processing GIT add..");
					addCommand.addFilepattern(localDagPath.getFileName().toString()).call();

					// git commit
					LOGGER.debug("Processing GIT commit..");
					git.commit().setMessage(gitCommitMsg).setAuthor(currentUser.getUsername(), currentUser.getEmail())
							.call();

					// git push
					LOGGER.debug("Processing GIT push..");
					pushCommand.setRemote(ORIGIN);
					pushCommand.setRefSpecs(new RefSpec(gitBranch + ":" + gitBranch));
					pushCommand.setCredentialsProvider(credentialsProvider);
					pushCommand.call();
				}
			}
		} catch (IllegalStateException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		} catch (GitAPIException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		} catch (IOException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		}
		return errors;
	}

	@Override
	public Map<String, String> getDagContentByIds(Set<String> dagNames) {

		LOGGER.trace("Entering getDagById");
		Map<String, String> dagsMap = null;
		UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("token",
				gitToken);
		try {
			Git git = gitClone(credentialsProvider);
			if (git != null) {
				dagsMap = new HashMap<String, String>();
				for (String dagName : dagNames) {
					String dagFileName = gitMountPath + File.separator + dagPath + File.separator + dagName + "."
							+ dagFileExtension;
					if (new File(dagFileName).isFile()) {
						LOGGER.debug("Fetching file content for:{}", dagName);
						String dagFileContent = Files.readString(Paths.get(dagFileName));
						dagsMap.put(dagName, dagFileContent);
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("error occured: {}", e.getMessage());
		} catch (GitAPIException e) {
			LOGGER.error("error occured: {}", e.getMessage());
		}
		LOGGER.trace("Returning from getDagById");
		return dagsMap;
	}

	/**
	 * <p>
	 * To delete a DAG identified by DAG Name
	 * </p>
	 * 
	 * @param dagName
	 * @param currentUser
	 * @return {@code errors} if exists otherwise {@code null}
	 */
	@Override
	public AirflowGITResponse deleteAirflowDag(String dagName, AirflowProjectUserVO currentUser) {
		LOGGER.trace("Entering deleteAirflowDag.");
		AirflowGITResponse res = new AirflowGITResponse();
		List<MessageDescription> errors = null;
		try {
			UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("token",
					gitToken);
			Path localDagPath = Paths.get(gitMountPath + File.separator + dagPath);
			Git git = gitClone(credentialsProvider);
			if (git != null) {
				PushCommand pushCommand = git.push();
				AddCommand addCommand = git.add();
				RmCommand rmCommand = git.rm();
				CommitCommand commitCommand = git.commit();
				StatusCommand statusCommand = git.status();
				String gitCommitMsg = "Deleting DAG:";
				String dagFileName = dagName + "." + dagFileExtension;
				if (new File(localDagPath.toString() + File.separator + dagFileName).isFile()) {
					LOGGER.debug("Deleting DAG:{}", dagName);
					gitCommitMsg = gitCommitMsg + " " + dagFileName;
					Files.deleteIfExists(localDagPath.resolve(dagFileName));
				} else {
					LOGGER.debug("DAG not found:{}", dagName);
					errors = setErrors("DAG not found: " + dagName);
				}
				if (ObjectUtils.isEmpty(errors)) {
					// git add
					LOGGER.debug("Processing GIT add..");
					statusCommand.call().getMissing().forEach(rmCommand::addFilepattern);
					rmCommand.call();
					// addCommand.addFilepattern(".").call();

					// git commit
					LOGGER.debug("Processing GIT commit..");
					commitCommand.setMessage(gitCommitMsg).setAuthor(currentUser.getUsername(), currentUser.getEmail())
							.call();

					RevCommit latestCommit = git.log().setMaxCount(1).call().iterator().next();
					// String latestCommitHash = latestCommit.getName();
					res.setGitCommitId(latestCommit);

					// git push
					LOGGER.debug("Processing GIT push..");
					pushCommand.setRemote(ORIGIN);
					pushCommand.setRefSpecs(new RefSpec(gitBranch + ":" + gitBranch));
					pushCommand.setCredentialsProvider(credentialsProvider);
					pushCommand.call();
				}
			}
		} catch (IllegalStateException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		} catch (GitAPIException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		} catch (IOException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		}
		LOGGER.trace("Returning from deleteAirflowDag.");
		res.setErrors(errors);
		return res;
	}

	/**
	 * To rollback GIT commit
	 * 
	 * @param commit
	 * @return List<MessageDescription>
	 */
	@Override
	public List<MessageDescription> gitRollback(RevCommit commit) {
		LOGGER.trace("Entering gitRollback.");
		List<MessageDescription> errors = null;
		try {
			UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("token",
					gitToken);
			Git git = gitClone(credentialsProvider);
			if (git != null) {
				PushCommand pushCommand = git.push();
				RevertCommand revertCommand = git.revert();
				// git revert
				LOGGER.debug("Processing GIT Revert..");
				RevCommit latestCommit = git.log().setMaxCount(1).call().iterator().next();
				revertCommand.include(latestCommit).call();

				// git push
				LOGGER.debug("Processing GIT push..");
				pushCommand.setRemote(ORIGIN);
				pushCommand.setRefSpecs(new RefSpec(gitBranch + ":" + gitBranch));
				pushCommand.setCredentialsProvider(credentialsProvider);
				pushCommand.call();
			}
		} catch (IllegalStateException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		} catch (GitAPIException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		} catch (IOException e) {
			LOGGER.error("error occured: {}", e.getMessage());
			errors = setErrors(e.getMessage());
		}
		return errors;
	}

}
