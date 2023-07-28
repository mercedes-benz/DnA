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

package com.daimler.data.service.notebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.NotebookAssembler;
import com.daimler.data.controller.NotebookController;
import com.daimler.data.db.entities.NotebookNsql;
import com.daimler.data.db.jsonb.Notebook;
import com.daimler.data.db.repo.notebook.NotebookCustomRepository;
import com.daimler.data.db.repo.notebook.NotebookRepository;
import com.daimler.data.dto.notebook.NotebookVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.solution.SolutionVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.solution.SolutionService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;

@Service
public class BaseNotebookService extends BaseCommonService<NotebookVO, NotebookNsql, String>
		implements NotebookService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseNotebookService.class);

	@Autowired
	private UserStore userStore;

	@Autowired
	private KafkaProducerService kafkaProducer;

	@Autowired
	private NotebookCustomRepository customRepo;
	@Autowired
	private NotebookRepository jpaRepo;
	@Autowired
	private NotebookAssembler notebookAssembler;
	
	@Autowired
	private SolutionService solutionService;

	@Autowired
	private UserInfoService userInfoService;
	
	public BaseNotebookService() {
		super();
	}

	/**
	 * updateSolutionIdofDnaNotebook
	 * <p>
	 * update solution id of DnA Notebook
	 * 
	 * @param dnaNotebookId
	 * @param solutionId
	 */
	@Override
	@Transactional
	public void updateSolutionIdofDnaNotebook(String updateType, String dnaNotebookId, String solutionId) {
		boolean sendNotificationForNotebookLink = false;
		boolean sendNotificationForNotebookUnLink = false;
//		Optional<NotebookNsql> notebookOptional= jpaRepo.findById(dnaNotebookId);
//		NotebookNsql notebookNsql = (notebookOptional != null && !notebookOptional.isEmpty()) ? notebookOptional.get()
//				: null;
//		if(notebookNsql!=null && notebookNsql.getData()!=null) {
//			notebookNsql.getData().setSolutionId(solutionId);
//			LOGGER.debug("Saving Notebook...");
//			jpaRepo.save(notebookNsql);
//		}
		String solutionName = solutionId;
		Notebook notebook = null;
		if (solutionId != null) {
			SolutionVO solutionVO = solutionService.getById(solutionId);
			if(solutionVO!=null) {
				solutionName = solutionVO.getProductName();
			}
			NotebookNsql existingNotebook = customRepo.findbyUniqueLiteral("solutionId", solutionId);
			if (existingNotebook != null) {
				Notebook existingNotebookDetails = existingNotebook.getData();
				if (dnaNotebookId != null && !"".equals(dnaNotebookId)) {
					String preNotebookId = existingNotebook.getId();
					notebook = existingNotebook.getData();
					if (!preNotebookId.equalsIgnoreCase(dnaNotebookId)) {
						updateNotebook(null, existingNotebook);
						LOGGER.info("Solution {} unlinked from old notebook {} ", solutionId,
								existingNotebook.getId());
						Optional<NotebookNsql> notebookOptional = jpaRepo.findById(dnaNotebookId);
						NotebookNsql notebookNsql = (notebookOptional != null && !notebookOptional.isEmpty())
								? notebookOptional.get()
								: null;
						if (notebookNsql != null) {
							updateNotebook(solutionId, notebookNsql);
							sendNotificationForNotebookLink = true;
							LOGGER.info("Solution {} linked to notebook {} ", solutionId, notebookNsql.getId());
						}
					}
					else {
						updateNotebook(null, existingNotebook);
						sendNotificationForNotebookUnLink = true;
						LOGGER.info("Solution {} unlinked from notebook {} ", solutionId, existingNotebook.getId());
					}
				} 
			} else {
				if (dnaNotebookId != null) {
					Optional<NotebookNsql> notebookOptional = jpaRepo.findById(dnaNotebookId);
					NotebookNsql notebookNsql = (notebookOptional != null && !notebookOptional.isEmpty())
							? notebookOptional.get()
							: null;
					if (notebookNsql != null) {
						updateNotebook(solutionId, notebookNsql);
						sendNotificationForNotebookLink = true;
						LOGGER.info("Solution {} linked to notebook {} ", solutionId, notebookNsql.getId());
					}
				}
			}
		} else {
			if (dnaNotebookId != null) {
				Optional<NotebookNsql> notebookOptional = jpaRepo.findById(dnaNotebookId);
				NotebookNsql notebookNsql = (notebookOptional != null && !notebookOptional.isEmpty())
						? notebookOptional.get()
						: null;
				if (notebookNsql != null) {
					notebook = notebookNsql.getData();
					updateNotebook(null, notebookNsql);
					sendNotificationForNotebookUnLink = true;
					LOGGER.debug("Solution {} unlinked from notebook {} , on delete of solution", solutionId,
							notebookNsql.getId());
				}
			}
		}

		String eventType = "";
		String message = "";
		Boolean mailRequired = true;
		List<String> subscribedUsers = new ArrayList<>();
		List<String> subscribedUsersEmail = new ArrayList<>();
		NotebookNsql notebookNsql = new NotebookNsql();
		if(dnaNotebookId != null) {
			Optional<NotebookNsql> notebookOptional = jpaRepo.findById(dnaNotebookId);
			notebookNsql = (notebookOptional != null && !notebookOptional.isEmpty())
					? notebookOptional.get()
					: null;
		}
		String notebookName = (notebookNsql != null && notebookNsql.getData() != null) ? notebookNsql.getData().getName() : "";		
		if(notebookNsql.getData() != null) {
			LOGGER.info(notebookNsql.getData().toString());
		}
		if ("provisioned".equalsIgnoreCase(updateType) && sendNotificationForNotebookLink) {
			eventType = "Notebook Provisioned";
			message = "Solution " + solutionName + " provisioned from notebook " + notebookName;
		}
		if (sendNotificationForNotebookUnLink) {
			eventType = "Notebook Unlinked";
			message = "Notebook " + notebookName + " unlinked from Solution " + solutionName + " before solution delete";
		}
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "dna_system";
		String emailId = currentUser != null ? currentUser.getEmail() : "";
		subscribedUsers.add(userId);
		subscribedUsersEmail.add(emailId);
		try {
			/*
			 * if(subscribedUsers!=null && !subscribedUsers.isEmpty() &&
			 * subscribedUsers.contains(userId)) {
			 * LOGGER.info("Removed current userid from subscribedUsers");
			 * subscribedUsers.remove(userId); }
			 */
			if (sendNotificationForNotebookLink || sendNotificationForNotebookUnLink)
					kafkaProducer.send(eventType, solutionId, "", userId, message, mailRequired, subscribedUsers,subscribedUsersEmail,null);
		} catch (Exception e) {
			LOGGER.error("Failed while publishing notebookevent of eventType {} solutionId {} with exceptionmsg {} ",
					eventType, solutionId, e.getMessage());
		}
	}

	@Override
	public Integer getTotalNumberOfNotebooks() {
		return customRepo.getTotalNumberOfNotebooks();
	}

	private void updateNotebook(String solutionId, NotebookNsql notebookRecord) {
		Notebook existingNotebookDetails = notebookRecord.getData();
		existingNotebookDetails.setSolutionId(solutionId);
		notebookRecord.setData(existingNotebookDetails);
		customRepo.update(notebookRecord);
	}
	
	@Override
	public NotebookVO getById(String id) {
		LOGGER.info("Fetching notebook info from db.");
		NotebookVO notebookVO = super.getById(id);
		if(Objects.nonNull(notebookVO)) {
			LOGGER.info("Fetching user details for user:{}",notebookVO.getUserId());
			UserInfoVO userInfoVO = userInfoService.getById(notebookVO.getUserId().toUpperCase());
			LOGGER.info("Setting createdByVO.");
			notebookVO.setCreatedBy(notebookAssembler.toCreatedByVO(userInfoVO));
		}
		return notebookVO;
	}

}
