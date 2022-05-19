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

package com.daimler.data.db.repo.itsmm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.daimler.data.assembler.ItsmmGameUserAssembler;
import com.daimler.data.db.entities.ItsmmGameUserDetailNsql;
import com.daimler.data.db.jsonb.ItsmmGameUserDetail;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.dto.itsmmgame.ItsmmGameUserDetailsResultVO;
import com.daimler.data.dto.itsmmgame.ItsmmGameUserRecordsVO;
import com.daimler.data.dto.itsmmgame.ItsmmGameUserResultsVO;
import com.daimler.data.dto.itsmmgame.ItsmmGameUserResultsWraperVO;

@Repository
public class ItsmmGameUserCustomRepositoryImpl extends CommonDataRepositoryImpl<ItsmmGameUserDetailNsql, String>
		implements ItsmmGameUserCustomRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(ItsmmGameUserCustomRepositoryImpl.class);

	@Autowired
	private ItsmmGameUserAssembler assembler;

	@Override
	public ItsmmGameUserRecordsVO getRecords() {
		ItsmmGameUserRecordsVO wrapperVO = new ItsmmGameUserRecordsVO();
		List<ItsmmGameUserDetailsResultVO> data = new ArrayList();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<ItsmmGameUserDetailNsql> cq = cb.createQuery(ItsmmGameUserDetailNsql.class);
			Root<ItsmmGameUserDetailNsql> root = cq.from(ItsmmGameUserDetailNsql.class);
			CriteriaQuery<ItsmmGameUserDetailNsql> getAll = cq.select(root).distinct(true);
			TypedQuery<ItsmmGameUserDetailNsql> getAllQuery = em.createQuery(getAll);
			List<ItsmmGameUserDetailNsql> rows = getAllQuery.getResultList();
			List<ItsmmGameUserDetail> records = new ArrayList<>();
			List<ItsmmGameUserDetail> sortedRecords = new ArrayList<>();
			if (rows != null && !rows.isEmpty()) {
				records = rows.stream().map(n -> n.getData()).collect(Collectors.toList());
				if (records != null && !records.isEmpty()) {
					for (ItsmmGameUserDetail player : records) {
						if (player != null) {
							Date startTime = player.getUserEventStartTime();
							Date endTime = player.getUserEventEndTime();
							if (startTime != null && endTime != null) {
								long diffInMillies = Math.abs(endTime.getTime() - startTime.getTime());
								player.setTimeTaken(diffInMillies);
							} else
								player.setTimeTaken(Long.MAX_VALUE);
						}
					}
					sortedRecords = records.stream().sorted(Comparator.comparingLong(ItsmmGameUserDetail::getTimeTaken))
							.collect(Collectors.toList());
				}
			}
			data = sortedRecords.stream().map(n -> assembler.toResultsVO(n)).collect(Collectors.toList());
			wrapperVO.setData(data);
			return wrapperVO;
		} catch (Exception e) {
			LOGGER.error("Failed to fetch records {} ", e.getMessage());
			return null;
		}
	}

	@Override
	public ItsmmGameUserResultsWraperVO getResults(String correctAnswer) {
		ItsmmGameUserResultsWraperVO resultWrapperVO = new ItsmmGameUserResultsWraperVO();
		ItsmmGameUserResultsVO resultVO = new ItsmmGameUserResultsVO();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<ItsmmGameUserDetailNsql> cq = cb.createQuery(ItsmmGameUserDetailNsql.class);
		Root<ItsmmGameUserDetailNsql> root = cq.from(ItsmmGameUserDetailNsql.class);
		CriteriaQuery<ItsmmGameUserDetailNsql> getAll = cq.select(root).distinct(true);
		TypedQuery<ItsmmGameUserDetailNsql> getAllQuery = em.createQuery(getAll);
		List<ItsmmGameUserDetailNsql> rows = getAllQuery.getResultList();
		List<ItsmmGameUserDetail> records = new ArrayList<>();
		int participated = 0;
		int submittedCorrectWithoutHint = 0;
		int submittedCorrectWithHint = 0;
		int notAnswered = 0;
		int viewedSolution = 0;
		List<ItsmmGameUserDetail> winnersWithoutHint = new ArrayList<>();
		List<ItsmmGameUserDetail> winnersWithHint = new ArrayList<>();
		if (rows != null && !rows.isEmpty()) {
			records = rows.stream().map(n -> n.getData()).collect(Collectors.toList());
			if (records != null && !records.isEmpty()) {
				for (ItsmmGameUserDetail player : records) {
					if (player != null) {
						if (player.getUserEventStatus() != null && !"NOTSTARTED".equals(player.getUserEventStatus())) {
							participated++;
						}
						if (player.getAnswer() == null && !"NOTSTARTED".equals(player.getUserEventStatus()))
							notAnswered++;
						String userAnswer = player.getAnswer();
						Date startTime = player.getUserEventStartTime();
						Date endTime = player.getUserEventEndTime();
						if (player.getSolutionSeen() != null && player.getSolutionSeen()) {
							viewedSolution++;
						}
						if (userAnswer != null && correctAnswer.equalsIgnoreCase(userAnswer)) {
							long diffInMillies = Long.MAX_VALUE;
							if (startTime != null && endTime != null) {
								diffInMillies = Math.abs(endTime.getTime() - startTime.getTime());
								player.setTimeTaken(diffInMillies);
							}
							if (player.getSolutionSeen() != null && player.getSolutionSeen()) {
								submittedCorrectWithHint++;
								winnersWithHint.add(player);
							} else {
								submittedCorrectWithoutHint++;
								winnersWithoutHint.add(player);
							}
						}
					}
				}
			}
		}
		resultVO.setParticipated(Long.valueOf(participated));
		LOGGER.info("participated {} ", participated);
		resultVO.setSubmittedCorrectWithoutHint(Long.valueOf(submittedCorrectWithoutHint));
		LOGGER.info("submittedCorrectWithoutHint {}", submittedCorrectWithoutHint);
		resultVO.setSubmittedCorrectWithHint(Long.valueOf(submittedCorrectWithHint));
		LOGGER.info("submittedCorrectWithHint {}", submittedCorrectWithHint);
		resultVO.setNotAnswered(Long.valueOf(notAnswered));
		LOGGER.info("notAnswered {}", notAnswered);
		resultVO.setViewedSolution(Long.valueOf(viewedSolution));
		LOGGER.info("viewedSolution {}", viewedSolution);
		LOGGER.info("Fetching Players who answered correct without hint");
		List<ItsmmGameUserDetail> sortedwinnersWithoutHint = winnersWithoutHint.stream()
				.sorted(Comparator.comparingLong(ItsmmGameUserDetail::getTimeTaken)).collect(Collectors.toList());
		List<ItsmmGameUserDetailsResultVO> sortedwinnersWithoutHintVO = sortedwinnersWithoutHint.stream()
				.map(n -> assembler.toResultsVO(n)).collect(Collectors.toList());
		resultVO.setSortedwinnersWithoutHint(sortedwinnersWithoutHintVO);
		LOGGER.info("Fetching Players who answered correct with hint");
		List<ItsmmGameUserDetail> sortedwinnersWithHint = winnersWithHint.stream()
				.sorted(Comparator.comparingLong(ItsmmGameUserDetail::getTimeTaken)).collect(Collectors.toList());
		List<ItsmmGameUserDetailsResultVO> sortedwinnersWithHintVO = sortedwinnersWithHint.stream()
				.map(n -> assembler.toResultsVO(n)).collect(Collectors.toList());
		resultVO.setSortedwinnersWithHint(sortedwinnersWithHintVO);
		ItsmmGameUserDetailsResultVO overAllWinner = (sortedwinnersWithoutHintVO != null
				&& !sortedwinnersWithoutHintVO.isEmpty()) ? sortedwinnersWithoutHintVO.get(0) : null;
		if (overAllWinner == null)
			overAllWinner = (sortedwinnersWithHintVO != null && !sortedwinnersWithHintVO.isEmpty())
					? sortedwinnersWithHintVO.get(0)
					: null;
		resultVO.setOverAllEventWinner(overAllWinner);
		resultWrapperVO.setData(resultVO);
		return resultWrapperVO;
	}

}
