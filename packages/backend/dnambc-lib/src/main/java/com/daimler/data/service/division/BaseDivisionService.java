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

package com.daimler.data.service.division;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.DivisionAssembler;
import com.daimler.data.assembler.SolutionAssembler;
import com.daimler.data.client.dashboard.DashboardClient;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.DivisionNsql;
import com.daimler.data.db.repo.division.DivisionCustomRepository;
import com.daimler.data.db.repo.division.DivisionRepository;
import com.daimler.data.dto.divisions.DivisionRequestVO;
import com.daimler.data.dto.divisions.DivisionResponseVO;
import com.daimler.data.dto.divisions.DivisionVO;
import com.daimler.data.dto.divisions.SubdivisionVO;
import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.solution.SolutionService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.util.ConstantsUtility;

@Service
public class BaseDivisionService extends BaseCommonService<DivisionVO, DivisionNsql, String>
		implements DivisionService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseDivisionService.class);

	@Autowired
	private DivisionCustomRepository customRepo;

	@Autowired
	private DivisionRepository jpaRepo;
	@Autowired
	private DivisionAssembler assembler;

	@Autowired
	private SolutionService solutionService;

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private DashboardClient dashboardClient;

	@Autowired
	private SolutionAssembler solutionAssembler;

	public BaseDivisionService() {
		super();
	}

	@Override
	public List<SubdivisionVO> getSubDivisionsById(String id) {
		Optional<DivisionNsql> entityOptional = jpaRepo.findById(id);
		DivisionNsql entity = entityOptional != null ? entityOptional.get() : null;
		return assembler.toSubDivisionVoList(entity);
	}

	@Override
	public List<DivisionVO> getDivisionsByIds(List<String> ids) {

		LOGGER.debug("In getDivisionsByIds, Adding SubdivisionVO object with Empty/None value...");
		SubdivisionVO emptySubDivisionVO = new SubdivisionVO();
		emptySubDivisionVO.setId(ConstantsUtility.EMPTY_VALUE);
		emptySubDivisionVO.setName(ConstantsUtility.NONE_VALUE);

		List<DivisionNsql> entityList = ObjectUtils.isEmpty(ids) ? jpaRepo.findAll() : jpaRepo.findAllById(ids);
		List<DivisionVO> divisionsVO = entityList.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());

		if (!ObjectUtils.isEmpty(divisionsVO)) {
			LOGGER.debug("In getDivisionsByIds, Appending Subdivision object with Empty/None value");
			divisionsVO.get(0).getSubdivisions().add(0, emptySubDivisionVO);
		}
		return divisionsVO;
	}

	@Override
	@Transactional
	public ResponseEntity<DivisionResponseVO> createDivision(DivisionRequestVO divisionRequestVO) {
		DivisionVO vo = divisionRequestVO.getData();
		DivisionResponseVO responseVO = new DivisionResponseVO();

		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		if (userInfoService.isAdmin(userId)) {
			DivisionVO existingDivisionVO = super.getByUniqueliteral("name", vo.getName());
			if (existingDivisionVO != null && existingDivisionVO.getName() != null) {
				LOGGER.debug("Division {} already exists", vo.getName());
				responseVO.setData(existingDivisionVO);
				return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
			}
			vo.setId(null);
			DivisionVO divisionVO = super.create(vo);
			if (divisionVO != null && divisionVO.getId() != null) {
				LOGGER.info("New division {} created successfully", vo.getName());
				responseVO.setData(divisionVO);
				List<ChangeLogVO> changeLogs = new ArrayList<>();
				changeLogs = solutionAssembler.jsonObjectCompare(divisionVO, null, currentUser);
				String userName = super.currentUserName(currentUser);
				String eventMessage = "Division " + divisionVO.getName() + " has been added by Admin " + userName;
				userInfoService.notifyAllAdminUsers(ConstantsUtility.SOLUTION_MDM, divisionVO.getId(), eventMessage,
						userId, changeLogs);
				return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
			} else {
				LOGGER.error("Failed to create new division {} with unknown exception", vo.getName());
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			LOGGER.debug("Division cannot be created. User {} not authorized", userId);
			List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
			MessageDescription notAuthorizedMsg = new MessageDescription();
			notAuthorizedMsg.setMessage("Not authorized to create division. User does not have admin privileges.");
			notAuthorizedMsgs.add(notAuthorizedMsg);
			responseVO.setErrors(notAuthorizedMsgs);
			return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteDivision(String id) {
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		if (!userInfoService.isAdmin(userId)) {
			MessageDescription notAuthorizedMsg = new MessageDescription();
			notAuthorizedMsg.setMessage("Not authorized to delete division. User does not have admin privileges.");
			LOGGER.debug("Division with id {} cannot be deleted. User {} not authorized", id, userId);
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(notAuthorizedMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
		}
		DivisionVO existingVO = super.getById(id);
		String divisionName = existingVO != null ? existingVO.getName() : "";
		LOGGER.debug("Calling solutionService to delete cascading refences to division {}", id);
		solutionService.deleteTagForEachSolution(divisionName, null, SolutionService.TAG_CATEGORY.DIVISION);
		//To remove division from users role division lists
		userInfoService.updateDivisionForUserRole(divisionName, null);
		deleteById(id);
		LOGGER.debug("Calling dashboardService to delete cascading refences to division {}", id);
		dashboardClient.deleteDivisionFromEachReport(id);
		String userName = super.currentUserName(currentUser);
		String eventMessage = "Division " + divisionName + " has been deleted by Admin " + userName;
		List<ChangeLogVO> changeLogs = new ArrayList<>();
		changeLogs = solutionAssembler.jsonObjectCompare(null, existingVO, currentUser);
		userInfoService.notifyAllAdminUsers(ConstantsUtility.SOLUTION_MDM, id, eventMessage, userId, changeLogs);
		GenericMessage successMsg = new GenericMessage();
		successMsg.setSuccess("success");
		LOGGER.info("Division {} deleted successfully", divisionName);
		return new ResponseEntity<>(successMsg, HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<DivisionResponseVO> updateDivision(DivisionRequestVO divisionRequestVO) {
		DivisionResponseVO response = new DivisionResponseVO();
		DivisionVO vo = divisionRequestVO.getData();
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : "";
		if (userInfoService.isAdmin(userId)) {
			String id = vo.getId();
			DivisionVO existingVO = super.getById(id);
			DivisionVO mergedDivisionVO = null;
			if (existingVO != null && existingVO.getId() != null) {
				String uniqueDivisionName = vo.getName();
				DivisionVO existingDivisionVO = super.getByUniqueliteral("name", vo.getName());
				if (existingDivisionVO != null && existingDivisionVO.getName() != null
						&& !existingDivisionVO.getId().equals(id)) {
					response.setData(existingDivisionVO);
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("Division already exists.");
					messages.add(message);
					response.setErrors(messages);
					LOGGER.debug("Division {} already exists, returning as CONFLICT", uniqueDivisionName);
					return new ResponseEntity<>(response, HttpStatus.CONFLICT);
				}
				LOGGER.debug("Calling solutionService to update cascading refences to division {}", id);
				solutionService.updateForEachSolution(id, "", SolutionService.TAG_CATEGORY.DIVISION, vo);
				List<ChangeLogVO> changeLogsVO = solutionAssembler.jsonObjectCompare(vo, existingDivisionVO, currentUser);
				if (null != existingDivisionVO.getChangeLogs() && changeLogsVO!=null) {
					changeLogsVO.addAll(existingDivisionVO.getChangeLogs());
				}
				vo.setChangeLogs(changeLogsVO);
				mergedDivisionVO = super.create(vo);
				if (mergedDivisionVO != null && mergedDivisionVO.getId() != null) {
					LOGGER.debug("Calling dashboardService to update cascading refences to division {}", id);
					dashboardClient.updateDivisionFromEachReport(vo);
					//To update division value for users having DivisionAdmin role
					userInfoService.updateDivisionForUserRole(existingVO.getName(), uniqueDivisionName);
					response.setData(mergedDivisionVO);
					response.setErrors(null);
					List<ChangeLogVO> changeLogs = new ArrayList<>();
					changeLogs = solutionAssembler.jsonObjectCompare(mergedDivisionVO, existingVO, currentUser);
					String userName = super.currentUserName(currentUser);
					String eventMessage = "Division " + existingVO.getName() + " has been updated by Admin " + userName;
					userInfoService.notifyAllAdminUsers(ConstantsUtility.SOLUTION_MDM, id, eventMessage, userId,
							changeLogs);
					LOGGER.debug("Division with id {} updated successfully", id);
					return new ResponseEntity<>(response, HttpStatus.OK);
				} else {
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("Failed to update due to internal error");
					messages.add(message);
					response.setData(vo);
					response.setErrors(messages);
					LOGGER.debug("Division with id {} cannot be edited. Failed with unknown internal error", id);
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}

			} else {
				List<MessageDescription> notFoundmessages = new ArrayList<>();
				MessageDescription notFoundmessage = new MessageDescription();
				notFoundmessage.setMessage("No Division found for given id. Update cannot happen");
				notFoundmessages.add(notFoundmessage);
				response.setErrors(notFoundmessages);
				LOGGER.debug("No Division found for given id {} , update cannot happen.", id);
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
		} else {
			List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
			MessageDescription notAuthorizedMsg = new MessageDescription();
			notAuthorizedMsg.setMessage("Not authorized to update division. Only user with admin role can update.");
			notAuthorizedMsgs.add(notAuthorizedMsg);
			response.setErrors(notAuthorizedMsgs);
			LOGGER.debug("Division with id {} cannot be edited. User not authorized", vo.getId());
			return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
		}

	}

}
