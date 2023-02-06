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

package com.daimler.data.assembler;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.SolutionNsql;
import com.daimler.data.db.jsonb.SubDivision;
import com.daimler.data.db.jsonb.solution.AssessmentDetails;
import com.daimler.data.db.jsonb.solution.CalculatedDigitalValue;
import com.daimler.data.db.jsonb.solution.ChangeLogs;
import com.daimler.data.db.jsonb.solution.CostDriver;
import com.daimler.data.db.jsonb.solution.CostFactorSummary;
import com.daimler.data.db.jsonb.solution.CreatedBy;
import com.daimler.data.db.jsonb.solution.CurrentPhase;
import com.daimler.data.db.jsonb.solution.Factor;
import com.daimler.data.db.jsonb.solution.FileDetails;
import com.daimler.data.db.jsonb.solution.LogoDetails;
import com.daimler.data.db.jsonb.solution.MarketingRoleSummary;
import com.daimler.data.db.jsonb.solution.RampUpYear;
import com.daimler.data.db.jsonb.solution.SkillSummary;
import com.daimler.data.db.jsonb.solution.Solution;
import com.daimler.data.db.jsonb.solution.SolutionAlgorithm;
import com.daimler.data.db.jsonb.solution.SolutionComplianceLink;
import com.daimler.data.db.jsonb.solution.SolutionCustomerJourneyPhase;
import com.daimler.data.db.jsonb.solution.SolutionDataCompliance;
import com.daimler.data.db.jsonb.solution.SolutionDataVolume;
import com.daimler.data.db.jsonb.solution.SolutionDatasource;
import com.daimler.data.db.jsonb.solution.SolutionDigitalValue;
import com.daimler.data.db.jsonb.solution.SolutionDivision;
import com.daimler.data.db.jsonb.solution.SolutionLanguage;
import com.daimler.data.db.jsonb.solution.SolutionLocation;
import com.daimler.data.db.jsonb.solution.SolutionMarketingCommunicationChannel;
import com.daimler.data.db.jsonb.solution.SolutionMilestone;
import com.daimler.data.db.jsonb.solution.SolutionPersonalization;
import com.daimler.data.db.jsonb.solution.SolutionPhase;
import com.daimler.data.db.jsonb.solution.SolutionPlatform;
import com.daimler.data.db.jsonb.solution.SolutionProjectStatus;
import com.daimler.data.db.jsonb.solution.SolutionResult;
import com.daimler.data.db.jsonb.solution.SolutionRollOut;
import com.daimler.data.db.jsonb.solution.SolutionRollOutDetail;
import com.daimler.data.db.jsonb.solution.SolutionTeamMember;
import com.daimler.data.db.jsonb.solution.SolutionVisualization;
import com.daimler.data.db.jsonb.solution.ValueCalculator;
import com.daimler.data.db.jsonb.solution.ValueDriver;
import com.daimler.data.db.jsonb.solution.ValueFactorSummary;
import com.daimler.data.dto.algorithm.AlgorithmVO;
import com.daimler.data.dto.attachment.FileDetailsVO;
import com.daimler.data.dto.customerJourneyPhase.CustomerJourneyPhaseVO;
import com.daimler.data.dto.datavolume.DataVolumeVO;
import com.daimler.data.dto.language.LanguageVO;
import com.daimler.data.dto.marketingCommunicationChannel.MarketingCommunicationChannelVO;
import com.daimler.data.dto.platform.PlatformVO;
import com.daimler.data.dto.result.ResultVO;
import com.daimler.data.dto.solution.AssessmentDetailsVO;
import com.daimler.data.dto.solution.CalculatedDigitalValueVO;
import com.daimler.data.dto.solution.CalculatedValueRampUpYearVO;
import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.data.dto.solution.CostFactorSummaryVO;
import com.daimler.data.dto.solution.CostFactorVO;
import com.daimler.data.dto.solution.CostRampUpYearVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.solution.DataSourceSummaryVO;
import com.daimler.data.dto.solution.LinkVO;
import com.daimler.data.dto.solution.LogoDetailsVO;
import com.daimler.data.dto.solution.MarketingRoleSummaryVO;
import com.daimler.data.dto.solution.MilestoneVO;
import com.daimler.data.dto.solution.PersonalizationVO;
import com.daimler.data.dto.solution.SkillSummaryVO;
import com.daimler.data.dto.solution.SolutionAnalyticsVO;
import com.daimler.data.dto.solution.SolutionCollection;
import com.daimler.data.dto.solution.SolutionCurrentPhase;
import com.daimler.data.dto.solution.SolutionDataComplianceVO;
import com.daimler.data.dto.solution.SolutionDataSourceVO;
import com.daimler.data.dto.solution.SolutionDigitalValueVO;
import com.daimler.data.dto.solution.SolutionDivisionVO;
import com.daimler.data.dto.solution.SolutionLocationVO;
import com.daimler.data.dto.solution.SolutionMarketingVO;
import com.daimler.data.dto.solution.SolutionMilestonePhaseVO;
import com.daimler.data.dto.solution.SolutionPhaseVO;
import com.daimler.data.dto.solution.SolutionPortfolioVO;
import com.daimler.data.dto.solution.SolutionProjectStatusVO;
import com.daimler.data.dto.solution.SolutionRolloutDetailsVO;
import com.daimler.data.dto.solution.SolutionRolloutPhaseVO;
import com.daimler.data.dto.solution.SolutionSharingVO;
import com.daimler.data.dto.solution.SolutionVO;
import com.daimler.data.dto.solution.TeamMemberVO;
import com.daimler.data.dto.solution.TeamMemberVO.UserTypeEnum;
import com.daimler.data.dto.solution.ValueCalculatorVO;
import com.daimler.data.dto.solution.ValueFactorSummaryVO;
import com.daimler.data.dto.solution.ValueFactorVO;
import com.daimler.data.dto.solution.ValueRampUpYearVO;
import com.daimler.data.dto.visualization.VisualizationVO;
import com.daimler.data.util.ConstantsUtility;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class SolutionAssembler implements GenericAssembler<SolutionVO, SolutionNsql> {

	private Logger LOGGER = LoggerFactory.getLogger(SolutionAssembler.class);

	@Override
	public SolutionVO toVo(SolutionNsql entity) {
		SolutionVO vo = new SolutionVO();
		if (entity != null && entity.getData() != null) {
			Solution solution = entity.getData();
			BeanUtils.copyProperties(solution, vo);
			if (solution.getCreatedBy() != null) {
				vo.setCreatedBy(new CreatedByVO().id(solution.getCreatedBy().getId())
						.firstName(solution.getCreatedBy().getFirstName())
						.lastName(solution.getCreatedBy().getLastName())
						.department(solution.getCreatedBy().getDepartment()).email(solution.getCreatedBy().getEmail())
						.department(solution.getCreatedBy().getDepartment()));
			}

			// Logo details
			if (solution.getLogoDetails() != null) {
				LogoDetailsVO logoDetailsVO = new LogoDetailsVO();
				BeanUtils.copyProperties(solution.getLogoDetails(), logoDetailsVO);
				vo.setLogoDetails(logoDetailsVO);
			}

			List<SolutionLocation> locations = solution.getLocations();
			List<SolutionLocationVO> locationsVO = new ArrayList<>();
			if (locations != null && !locations.isEmpty()) {
				for (SolutionLocation location : locations) {
					if (location != null) {
						SolutionLocationVO locationVO = new SolutionLocationVO();
						BeanUtils.copyProperties(location, locationVO);
						locationsVO.add(locationVO);
					}
				}
			}
			vo.setLocations(locationsVO);

			SolutionDivisionVO divisionvo = new SolutionDivisionVO();
			SolutionDivision division = solution.getDivision();
			if (division != null) {
				BeanUtils.copyProperties(division, divisionvo);
				com.daimler.data.dto.solution.SubDivision subdivisionVO = new com.daimler.data.dto.solution.SubDivision();
				if (division.getSubdivision() != null)
					BeanUtils.copyProperties(division.getSubdivision(), subdivisionVO);
				divisionvo.setSubdivision(subdivisionVO);
				vo.setDivision(divisionvo);
			}

			SolutionProjectStatusVO projectStatusVO = new SolutionProjectStatusVO();
			SolutionProjectStatus projectStatus = solution.getProjectStatus();
			if (projectStatus != null) {
				String name = projectStatus.getName();
				if (name.toUpperCase().equalsIgnoreCase("ON TRACK")) {
					name = "Active";
				} else if (name.toUpperCase().equalsIgnoreCase("INACTIVE")) {
					name = "On hold";
				} else if (name.toUpperCase().equalsIgnoreCase("CLOSED")) {
					name = "Closed";
				}
				BeanUtils.copyProperties(projectStatus, projectStatusVO);
				projectStatusVO.setName(name);
				vo.setProjectStatus(projectStatusVO);
			}

			SolutionCurrentPhase currentPhaseVO = new SolutionCurrentPhase();
			CurrentPhase currentPhase = solution.getCurrentPhase();
			if (currentPhase != null) {
				BeanUtils.copyProperties(currentPhase, currentPhaseVO);
				vo.setCurrentPhase(currentPhaseVO);
			}

			List<TeamMemberVO> teamMembersVO = new ArrayList<>();
			List<SolutionTeamMember> teamMembers = solution.getTeamMembers();
			if (teamMembers != null && !teamMembers.isEmpty())
				teamMembersVO = teamMembers.stream().map(n -> toTeamMemberVO(n)).collect(Collectors.toList());
			vo.setTeam(teamMembersVO);

			List<SolutionVO.OpenSegmentsEnum> openSegmentsEnumList = new ArrayList<>();
			if (solution.getOpenSegments() != null && !solution.getOpenSegments().isEmpty()) {
				solution.getOpenSegments().forEach(
						openSegment -> openSegmentsEnumList.add(SolutionVO.OpenSegmentsEnum.valueOf(openSegment)));
			}
			vo.setOpenSegments(openSegmentsEnumList);

			MilestoneVO milestoneVO = new MilestoneVO();
			List<SolutionMilestonePhaseVO> phasesVO = new ArrayList<>();
			List<SolutionMilestone> milestones = solution.getMilestones();
			if (milestones != null && !milestones.isEmpty()) {
				for (SolutionMilestone milestone : milestones) {
					if (milestone != null) {
						SolutionMilestonePhaseVO milestonePhaseVO = new SolutionMilestonePhaseVO();
						milestonePhaseVO.setDescription(milestone.getMilestoneDescription());
						milestonePhaseVO.setMonth(new BigDecimal(milestone.getMonth()));
						milestonePhaseVO.setYear(new BigDecimal(milestone.getYear()));
						SolutionPhase phase = milestone.getPhase();
						SolutionPhaseVO phaseVO = new SolutionPhaseVO();
						BeanUtils.copyProperties(phase, phaseVO);
						milestonePhaseVO.setPhase(phaseVO);
						phasesVO.add(milestonePhaseVO);
					}
				}
			}
			milestoneVO.setPhases(phasesVO);

			SolutionRollOut solutionRollOut = solution.getRollout();
			SolutionRolloutPhaseVO rolloutPhaseVO = new SolutionRolloutPhaseVO();
			List<SolutionRolloutDetailsVO> rolloutDetailsVO = new ArrayList<>();
			rolloutPhaseVO.setDescription("");
			rolloutPhaseVO.setDetails(rolloutDetailsVO);
			if (solutionRollOut != null) {
				List<SolutionRollOutDetail> rollOutDetails = solutionRollOut.getRollOutDetails();
				if (rollOutDetails != null && !rollOutDetails.isEmpty()) {
					for (SolutionRollOutDetail rollOutDetail : rollOutDetails) {
						if (rollOutDetail != null) {
							SolutionRolloutDetailsVO rolloutDetailVO = new SolutionRolloutDetailsVO();
							SolutionLocationVO locationVO = new SolutionLocationVO();
							rolloutDetailVO.setMonth(new BigDecimal(rollOutDetail.getMonth()));
							rolloutDetailVO.setYear(new BigDecimal(rollOutDetail.getYear()));
							SolutionLocation location = rollOutDetail.getLocation();
							if (location != null) {
								BeanUtils.copyProperties(location, locationVO);
							}
							rolloutDetailVO.setLocation(locationVO);
							rolloutDetailsVO.add(rolloutDetailVO);
						}
					}
					rolloutPhaseVO.setDetails(rolloutDetailsVO);
				}
				String rolloutDesc = solutionRollOut.getRollOutDescription();
				if (rolloutDesc == null)
					rolloutDesc = "";
				rolloutPhaseVO.setDescription(rolloutDesc);
			}
			milestoneVO.setRollouts(rolloutPhaseVO);
			vo.setMilestones(milestoneVO);

			SolutionDataSourceVO datasourcesVO = new SolutionDataSourceVO();
			List<SolutionDatasource> datasources = solution.getDataSources();
			if (datasources != null && !datasources.isEmpty()) {
				List<DataSourceSummaryVO> datasourcesSummary = datasources.stream().map(n -> toDataSourceSummaryVO(n))
						.toList();
				datasourcesVO.setDataSources(datasourcesSummary);
			}
			SolutionDataVolume dataVolume = solution.getTotalDataVolume();
			DataVolumeVO dataVolumeVO = new DataVolumeVO();
			if (dataVolume != null)
				BeanUtils.copyProperties(dataVolume, dataVolumeVO);
			datasourcesVO.setDataVolume(dataVolumeVO);
			vo.setDataSources(datasourcesVO);

			List<SolutionLanguage> languages = solution.getLanguages();
			List<LanguageVO> languagesVO = new ArrayList<>();
			if (languages != null && !languages.isEmpty()) {
				for (SolutionLanguage language : languages) {
					if (language != null) {
						LanguageVO languageVO = new LanguageVO();
						BeanUtils.copyProperties(language, languageVO);
						languagesVO.add(languageVO);
					}
				}
			}

			List<SolutionAlgorithm> algorithms = solution.getAlgorithms();
			List<AlgorithmVO> algorithmsVO = new ArrayList<>();
			if (algorithms != null && !algorithms.isEmpty()) {
				for (SolutionAlgorithm algorithm : algorithms) {
					if (algorithm != null) {
						AlgorithmVO algorithmVO = new AlgorithmVO();
						BeanUtils.copyProperties(algorithm, algorithmVO);
						algorithmsVO.add(algorithmVO);
					}
				}
			}

			List<SolutionVisualization> visualizations = solution.getVisualizations();
			List<VisualizationVO> visualizationsVO = new ArrayList<>();
			if (visualizations != null && !visualizations.isEmpty()) {
				for (SolutionVisualization visualization : visualizations) {
					if (visualization != null) {
						VisualizationVO visualizationVO = new VisualizationVO();
						BeanUtils.copyProperties(visualization, visualizationVO);
						visualizationsVO.add(visualizationVO);
					}
				}
			}
									
			List<SolutionCustomerJourneyPhase> customerJourneyPhases = solution.getCustomerJourneyPhases();
			List<CustomerJourneyPhaseVO> customerJourneyPhasesVO = new ArrayList<>();
			if(customerJourneyPhases != null && !customerJourneyPhases.isEmpty()) {
				for (SolutionCustomerJourneyPhase customerJourneyPhase : customerJourneyPhases) {
					if(customerJourneyPhase != null) {
						CustomerJourneyPhaseVO customerJourneyPhaseVO = new CustomerJourneyPhaseVO();
						BeanUtils.copyProperties(customerJourneyPhase,customerJourneyPhaseVO);
						customerJourneyPhasesVO.add(customerJourneyPhaseVO);
					}
				}
			}
			
			List<SolutionMarketingCommunicationChannel> marketingCommunicationChannels = solution.getMarketingCommunicationChannels();
			List<MarketingCommunicationChannelVO> marketingCommunicationChannelsVO = new ArrayList<>();
			if(marketingCommunicationChannels != null && !marketingCommunicationChannels.isEmpty()) {
				for(SolutionMarketingCommunicationChannel marketingCommunicationChannel : marketingCommunicationChannels) {
					if(marketingCommunicationChannel != null) {
						MarketingCommunicationChannelVO marketingCommunicationChannelVO = new MarketingCommunicationChannelVO();
						BeanUtils.copyProperties(marketingCommunicationChannel, marketingCommunicationChannelVO);
						marketingCommunicationChannelsVO.add(marketingCommunicationChannelVO);
					}
				}
				
			}
			
			SolutionPersonalization personalization = solution.getPersonalization(); 
			PersonalizationVO personalizationVO = new PersonalizationVO();			
			if(personalization != null && Objects.nonNull(personalization) ) {
				personalizationVO.setIsChecked(personalization.isChecked());
				personalizationVO.setDescription(personalization.getDescription());
			}	
			else {
				personalizationVO.setIsChecked(false);
				personalizationVO.setDescription("");
			}
			
			SolutionMarketingVO marketingVO = new SolutionMarketingVO();
			marketingVO.setCustomerJourneyPhases(customerJourneyPhasesVO);
			marketingVO.setMarketingCommunicationChannels(marketingCommunicationChannelsVO);
			marketingVO.setPersonalization(personalizationVO);
			marketingVO.setPersonas(solution.getPersonas());
			//setting MarketingRoleSummaryVO
			List<MarketingRoleSummaryVO> rolesVO = new ArrayList<>();
			if(!ObjectUtils.isEmpty(solution.getMarketingRoles())) {
				rolesVO = solution.getMarketingRoles().stream().map(n -> toMarketingRoleSummaryVO(n))
						.collect(Collectors.toList());	
			}
			marketingVO.setMarketingRoles(rolesVO);;
			
			vo.setMarketing(marketingVO);	
			
			//setting Department details
			vo.setDepartment(solution.getDepartment());
			
			SolutionAnalyticsVO analyticsVO = new SolutionAnalyticsVO();
			analyticsVO.setAlgorithms(algorithmsVO);
			analyticsVO.setLanguages(languagesVO);
			analyticsVO.setVisualizations(visualizationsVO);
			vo.setAnalytics(analyticsVO);

			SolutionSharingVO sharingVO = new SolutionSharingVO();

			ResultVO resultVO = new ResultVO();
			SolutionResult result = solution.getResult();
			if (result != null)
				BeanUtils.copyProperties(result, resultVO);
			sharingVO.setResult(resultVO);
			sharingVO.setResultUrl(solution.getResultUrl());
			sharingVO.setGitUrl(solution.getGitUrl());
			vo.setSharing(sharingVO);

			// Setting attachments
			List<FileDetails> solutionFileDetailsList = solution.getAttachments();
			List<FileDetailsVO> solutionFileDetailsVOList = new ArrayList<>();
			if (null != solutionFileDetailsList && !solutionFileDetailsList.isEmpty()) {
				for (FileDetails fileDetails : solutionFileDetailsList) {
					FileDetailsVO fileDetailsVO = new FileDetailsVO();
					BeanUtils.copyProperties(fileDetails, fileDetailsVO);
					solutionFileDetailsVOList.add(fileDetailsVO);
				}
				vo.setAttachments(solutionFileDetailsVOList);
			}

			// Setting digitalValue
			SolutionDigitalValueVO digitalValueDetailsVO = new SolutionDigitalValueVO();
			SolutionDigitalValue digitalValueDetails = solution.getDigitalValueDetails();
			if (digitalValueDetails != null) {
				BeanUtils.copyProperties(digitalValueDetails, digitalValueDetailsVO);
				// Setting projectController
				List<SolutionTeamMember> solutionTeamMembers = digitalValueDetails.getProjectControllers();
				List<TeamMemberVO> projectControllers = new ArrayList<TeamMemberVO>();
				if (null != solutionTeamMembers && !solutionTeamMembers.isEmpty()) {
					projectControllers = solutionTeamMembers.stream().map(n -> toTeamMemberVO(n))
							.collect(Collectors.toList());
				}
				digitalValueDetailsVO.setProjectControllers(projectControllers);
				// Setting costDrivers
				List<CostDriver> costDriverList = digitalValueDetails.getCostDrivers();
				if (costDriverList != null && !costDriverList.isEmpty()) {
					List<CostFactorVO> costFactorVOList = new ArrayList<>();
					for (CostDriver costDriver : costDriverList) {
						CostFactorVO costFactorVO = new CostFactorVO();
						// BeanUtils.copyProperties(costDriver, costFactorVO);
						if (null != costDriver.getCostFactors() && !costDriver.getCostFactors().isEmpty()) {
							for (Factor factor : costDriver.getCostFactors()) {
								costFactorVO = new CostFactorVO();
								BeanUtils.copyProperties(factor, costFactorVO);
								List<CostRampUpYearVO> rampUps = new ArrayList<CostRampUpYearVO>();
								CostRampUpYearVO costRampUpYearVO = null;
								if (null != factor.getRampUps() && !factor.getRampUps().isEmpty()) {
									for (RampUpYear rampUpYear : factor.getRampUps()) {
										costRampUpYearVO = new CostRampUpYearVO();
										BeanUtils.copyProperties(rampUpYear, costRampUpYearVO);
										costRampUpYearVO.setYear(new BigDecimal(rampUpYear.getYear()));
										rampUps.add(costRampUpYearVO);
									}
								}
								costFactorVO.setRampUp(rampUps);
							}
						}
						costFactorVOList.add(costFactorVO);
					}
					digitalValueDetailsVO.setCostDrivers(costFactorVOList);
				}
				// Setting valueDrivers
				List<ValueDriver> valueDriverList = digitalValueDetails.getValueDrivers();
				if (valueDriverList != null && !valueDriverList.isEmpty()) {
					List<ValueFactorVO> valueFactorVOList = new ArrayList<>();
					for (ValueDriver valueDriver : valueDriverList) {
						ValueFactorVO valueFactorVO = new ValueFactorVO();
						if (null != valueDriver.getValueFactors() && !valueDriver.getValueFactors().isEmpty()) {
							for (Factor factor : valueDriver.getValueFactors()) {
								valueFactorVO = new ValueFactorVO();
								BeanUtils.copyProperties(factor, valueFactorVO);
								List<ValueRampUpYearVO> rampUps = new ArrayList<ValueRampUpYearVO>();
								ValueRampUpYearVO valueRampUpYearVO = new ValueRampUpYearVO();
								if (null != factor.getRampUps() && !factor.getRampUps().isEmpty()) {
									for (RampUpYear rampUpYear : factor.getRampUps()) {
										valueRampUpYearVO = new ValueRampUpYearVO();
										BeanUtils.copyProperties(rampUpYear, valueRampUpYearVO);
										valueRampUpYearVO.setYear(new BigDecimal(rampUpYear.getYear()));
										rampUps.add(valueRampUpYearVO);
									}
								}
								valueFactorVO.setRampUp(rampUps);
							}
						}
						// BeanUtils.copyProperties(valueDriver, valueFactorVO);
						valueFactorVOList.add(valueFactorVO);
					}
					digitalValueDetailsVO.setValueDrivers(valueFactorVOList);
				}
				// Setting attachments
				List<FileDetails> fileDetailsList = digitalValueDetails.getAttachments();
				if (fileDetailsList != null && !fileDetailsList.isEmpty()) {
					List<FileDetailsVO> fileDetailsVOList = new ArrayList<>();
					for (FileDetails fileDetails : fileDetailsList) {
						FileDetailsVO fileDetailsVO = new FileDetailsVO();
						BeanUtils.copyProperties(fileDetails, fileDetailsVO);
						fileDetailsVOList.add(fileDetailsVO);
					}
					digitalValueDetailsVO.setAttachments(fileDetailsVOList);
				}
				// Setting assessment
				AssessmentDetails assessmentDetails = digitalValueDetails.getAssessment();
				if (assessmentDetails != null) {
					AssessmentDetailsVO assessmentVO = new AssessmentDetailsVO();
					BeanUtils.copyProperties(assessmentDetails, assessmentVO);
					digitalValueDetailsVO.setAssessment(assessmentVO);
				}

				// Setting permissions
				List<SolutionTeamMember> solutionTeamMemberList = digitalValueDetails.getPermissions();
				if (solutionTeamMemberList != null && !solutionTeamMemberList.isEmpty()) {
					List<TeamMemberVO> teamMemberVOList = new ArrayList<>();
					for (SolutionTeamMember solutionTeamMember : solutionTeamMemberList) {
						TeamMemberVO teamMemberVO = new TeamMemberVO();
						BeanUtils.copyProperties(solutionTeamMember, teamMemberVO);
						if (null != solutionTeamMember.getUserType())
							teamMemberVO.setUserType(UserTypeEnum.valueOf(solutionTeamMember.getUserType()));
						teamMemberVOList.add(teamMemberVO);
					}
					digitalValueDetailsVO.setPermissions(teamMemberVOList);
				}

				// Setting changeLogs
				List<ChangeLogs> changeLogsList = digitalValueDetails.getChangeLogs();
				if (changeLogsList != null && !changeLogsList.isEmpty()) {
					List<ChangeLogVO> changeLogVOList = new ArrayList<>();
					for (ChangeLogs changeLogs : changeLogsList) {
						ChangeLogVO changeLogVO = new ChangeLogVO();
						BeanUtils.copyProperties(changeLogs, changeLogVO);
						if (null != changeLogs.getModifiedBy()) {
							TeamMemberVO teamMemberVO = new TeamMemberVO();
							BeanUtils.copyProperties(changeLogs.getModifiedBy(), teamMemberVO);
							if (StringUtils.hasText(changeLogs.getModifiedBy().getUserType())) {
								teamMemberVO
										.setUserType(UserTypeEnum.valueOf(changeLogs.getModifiedBy().getUserType()));
							}
							changeLogVO.setModifiedBy(teamMemberVO);
						}
						changeLogVOList.add(changeLogVO);
					}
					digitalValueDetailsVO.setChangeLogs(changeLogVOList);
				}
				// settings Calculated Digital value details
				ValueCalculator valueCalculator = digitalValueDetails.getValueCalculator();
				if (null != valueCalculator) {
					ValueCalculatorVO valueCalculatorVO = new ValueCalculatorVO();
					BeanUtils.copyProperties(valueCalculator, valueCalculatorVO);
					// setting calculatedValueRampUpYears
					List<RampUpYear> calculatedValueRampUpYears = valueCalculator.getCalculatedValueRampUpYears();
					List<CalculatedValueRampUpYearVO> calculatedValueRampUpYearsVO = new ArrayList<>();
					if (null != calculatedValueRampUpYears && !calculatedValueRampUpYears.isEmpty())
						calculatedValueRampUpYearsVO = calculatedValueRampUpYears.stream()
								.map(n -> toCalculatedValueRampUpYearVO(n)).collect(Collectors.toList());
					valueCalculatorVO.setCalculatedValueRampUpYears(calculatedValueRampUpYearsVO);
					// Setting costFactorSummary
					if (null != valueCalculator.getCostFactorSummary()) {
						CostFactorSummaryVO costFactorSummaryVO = new CostFactorSummaryVO();
						BeanUtils.copyProperties(valueCalculator.getCostFactorSummary(), costFactorSummaryVO);
						valueCalculatorVO.setCostFactorSummary(costFactorSummaryVO);
					}
					// Setting ValueFactorSummary
					if (null != valueCalculator.getValueFactorSummary()) {
						ValueFactorSummaryVO valueFactorSummaryVO = new ValueFactorSummaryVO();
						BeanUtils.copyProperties(valueCalculator.getValueFactorSummary(), valueFactorSummaryVO);
						valueCalculatorVO.setValueFactorSummary(valueFactorSummaryVO);
					}
					// Setting Calculated Digital value
					CalculatedDigitalValueVO calculatedDigitalValueVO = new CalculatedDigitalValueVO();
					calculatedDigitalValueVO.setValueAt(new BigDecimal(100));
					if (null != valueCalculator.getCalculatedDigitalValue()) {
						BeanUtils.copyProperties(valueCalculator.getCalculatedDigitalValue(), calculatedDigitalValueVO);
					}
					// setting old digital Value
					if (calculatedDigitalValueVO.getValue() != null) {
						digitalValueDetailsVO.setDigitalValue(calculatedDigitalValueVO.getValue());
					} else if ((calculatedDigitalValueVO.getValue() == null && !calculatedValueRampUpYearsVO.isEmpty())
							|| digitalValueDetailsVO.getDigitalValue() == null) {
						LOGGER.debug(
								"Setting digital value as Zero since no value present at 100% and existing digital value is null");
						digitalValueDetailsVO.setDigitalValue(BigDecimal.ZERO);
					}

					valueCalculatorVO.setCalculatedDigitalValue(calculatedDigitalValueVO);
					digitalValueDetailsVO.setValueCalculator(valueCalculatorVO);
				}
			}

			vo.setDigitalValue(digitalValueDetailsVO);

			// Setting dataCompliance
			SolutionDataComplianceVO solutionDataComplianceVO = new SolutionDataComplianceVO();
			SolutionDataCompliance solutionDataCompliance = solution.getDataComplianceDetails();
			if (null != solutionDataCompliance) {
				BeanUtils.copyProperties(solutionDataCompliance, solutionDataComplianceVO);

				// Setting attachments
				List<FileDetails> dataComplianceFileDetailsList = solutionDataCompliance.getAttachments();
				List<FileDetailsVO> dataComplianceFileDetailsVOList = new ArrayList<>();
				if (null != dataComplianceFileDetailsList && !dataComplianceFileDetailsList.isEmpty()) {
					for (FileDetails fileDetails : dataComplianceFileDetailsList) {
						FileDetailsVO fileDetailsVO = new FileDetailsVO();
						BeanUtils.copyProperties(fileDetails, fileDetailsVO);
						dataComplianceFileDetailsVOList.add(fileDetailsVO);
					}
					solutionDataComplianceVO.setAttachments(dataComplianceFileDetailsVOList);
				}
				// Setting link
				List<SolutionComplianceLink> solutionComplianceLinks = solutionDataCompliance.getLinkDetails();
				List<LinkVO> linkVOList = new ArrayList<>();
				if (null != solutionComplianceLinks && !solutionComplianceLinks.isEmpty()) {
					for (SolutionComplianceLink solutionComplianceLink : solutionComplianceLinks) {
						if (solutionComplianceLink != null) {
							LinkVO linkVO = new LinkVO();
							BeanUtils.copyProperties(solutionComplianceLink, linkVO);
							linkVOList.add(linkVO);
						}
					}
					solutionDataComplianceVO.setLinks(linkVOList);
				}
				// Setting complianceOfficer
				List<TeamMemberVO> teamMemberVOList = new ArrayList<>();
				List<SolutionTeamMember> solutionTeamMembers = solutionDataCompliance.getComplianceOfficers();
				if (solutionTeamMembers != null && !solutionTeamMembers.isEmpty())
					teamMemberVOList = solutionTeamMembers.stream().map(n -> toTeamMemberVO(n))
							.collect(Collectors.toList());
				solutionDataComplianceVO.setComplianceOfficers(teamMemberVOList);

			}
			vo.setDataCompliance(solutionDataComplianceVO);

			SolutionPortfolioVO solutionPortfolioVO = new SolutionPortfolioVO();
			List<SolutionPlatform> platforms = solution.getPlatforms();
			List<PlatformVO> platformsVO = new ArrayList<>();
			if (platforms != null && !platforms.isEmpty()) {
				for (SolutionPlatform platform : platforms) {
					if (platform != null) {
						PlatformVO platformVO = new PlatformVO();
						BeanUtils.copyProperties(platform, platformVO);
						platformsVO.add(platformVO);
					}
				}
			}
			solutionPortfolioVO.setPlatforms(platformsVO);
			solutionPortfolioVO.setSolutionOnCloud(solution.isSolutionOnCloud());
			solutionPortfolioVO.setUsesExistingInternalPlatforms(solution.isUsesExistingInternalPlatforms());
			solutionPortfolioVO.setDnaNotebookId(solution.getDnaNotebookId());
			solutionPortfolioVO.setDnaDataikuProjectId(solution.getDataikuProjectKey());
			solutionPortfolioVO.setDnaSubscriptionAppId(solution.getDnaSubscriptionAppId());
			vo.setPortfolio(solutionPortfolioVO);

			// Setting SkillSummaryVO
			if (!ObjectUtils.isEmpty(solution.getSkills())) {
				List<SkillSummaryVO> skillsVO = solution.getSkills().stream().map(n -> toSkillSummaryVO(n))
						.collect(Collectors.toList());
				vo.setSkills(skillsVO);
			}

			vo.setId(entity.getId());
		}
		return vo;
	}

	/*
	 * To convert SolutionDatasource to DataSourceSummaryVO
	 */
	private DataSourceSummaryVO toDataSourceSummaryVO(SolutionDatasource solutionDatasource) {
		DataSourceSummaryVO dataSourceSummaryVO = null;
		if (solutionDatasource != null) {
			dataSourceSummaryVO = new DataSourceSummaryVO();
			dataSourceSummaryVO.setDataSource(solutionDatasource.getName());
			dataSourceSummaryVO.setWeightage(
					solutionDatasource.getWeightage() != null ? solutionDatasource.getWeightage() : BigDecimal.ZERO);
		}
		return dataSourceSummaryVO;
	}
	
	/*
	 * To convert SkillSummary to SkillSummaryVO
	 * 
	 */
	private SkillSummaryVO toSkillSummaryVO(SkillSummary skillSummary) {
		SkillSummaryVO skillSummaryVO = null;
		if (skillSummary != null) {
			skillSummaryVO = new SkillSummaryVO();
			BeanUtils.copyProperties(skillSummary, skillSummaryVO);
		}
		return skillSummaryVO;
	}
	
	/*
	 * To convert MArketingRoleSummary to MArketingRoleSummaryVO
	 */
	private MarketingRoleSummaryVO toMarketingRoleSummaryVO(MarketingRoleSummary marketingRoleSummary) {
		MarketingRoleSummaryVO roleSummaryVO = null;
		if(marketingRoleSummary != null) {
			roleSummaryVO = new MarketingRoleSummaryVO();
			BeanUtils.copyProperties(marketingRoleSummary, roleSummaryVO);
		}
		return roleSummaryVO;
	}
	


	private TeamMemberVO toTeamMemberVO(SolutionTeamMember teamMember) {
		TeamMemberVO teamMemberVO = new TeamMemberVO();
		if (teamMember != null) {
			BeanUtils.copyProperties(teamMember, teamMemberVO);
			teamMemberVO.setShortId(teamMember.getId());
			if (teamMember.getUserType() != null)
				teamMemberVO.setUserType(UserTypeEnum.valueOf(teamMember.getUserType().toUpperCase()));
		}
		return teamMemberVO;
	}

	/**
	 * toRampUpYearVO
	 * 
	 * @param RampUpYear
	 * @return CalculatedValueRampUpYearVO
	 */
	private CalculatedValueRampUpYearVO toCalculatedValueRampUpYearVO(RampUpYear rampUpYear) {
		CalculatedValueRampUpYearVO vo = new CalculatedValueRampUpYearVO();
		if (null != rampUpYear) {
			BeanUtils.copyProperties(rampUpYear, vo);
			vo.setYear(new BigDecimal(rampUpYear.getYear()));
		}
		return vo;
	}

	/**
	 * toValueDriverJson
	 * 
	 * @param valueFactorVO
	 * @return ValueDriver
	 */
	private ValueDriver toValueDriverJson(ValueFactorVO valueFactorVO) {
		ValueDriver valueDriver = new ValueDriver();
		if (null != valueFactorVO) {
			List<Factor> valueFactors = new ArrayList<Factor>();
			Factor factor = new Factor();
			BeanUtils.copyProperties(valueFactorVO, factor);
			List<RampUpYear> rampUps = new ArrayList<RampUpYear>();
			RampUpYear rampUpYear = null;
			if (null != valueFactorVO.getRampUp() && !valueFactorVO.getRampUp().isEmpty()) {
				for (ValueRampUpYearVO valueRampUpYearVO : valueFactorVO.getRampUp()) {
					rampUpYear = new RampUpYear();
					BeanUtils.copyProperties(valueRampUpYearVO, rampUpYear);
					rampUpYear.setYear(valueRampUpYearVO.getYear().intValue());
					rampUps.add(rampUpYear);
				}
			}
			factor.setRampUps(rampUps);
			valueFactors.add(factor);
			valueDriver.setValueFactors(valueFactors);
		}
		return valueDriver;
	}

	/**
	 * toCostDriverJson
	 * 
	 * @param costFactorVO
	 * @return CostDriver
	 */
	private CostDriver toCostDriverJson(CostFactorVO costFactorVO) {
		CostDriver costDriver = new CostDriver();
		if (null != costFactorVO) {
			List<Factor> costFactors = new ArrayList<Factor>();
			Factor factor = new Factor();
			BeanUtils.copyProperties(costFactorVO, factor);
			List<RampUpYear> rampUps = new ArrayList<RampUpYear>();
			RampUpYear rampUpYear = null;
			if (null != costFactorVO.getRampUp() && !costFactorVO.getRampUp().isEmpty()) {
				for (CostRampUpYearVO costRampUpYearVO : costFactorVO.getRampUp()) {
					rampUpYear = new RampUpYear();
					BeanUtils.copyProperties(costRampUpYearVO, rampUpYear);
					rampUpYear.setYear(costRampUpYearVO.getYear().intValue());
					rampUps.add(rampUpYear);
				}
			}

			factor.setRampUps(rampUps);
			costFactors.add(factor);
			costDriver.setCostFactors(costFactors);
		}
		return costDriver;
	}

	/**
	 * toFileDetailsJson
	 * 
	 * @param fileDetailsVO
	 * @return FileDetails
	 */
	private FileDetails toFileDetailsJson(FileDetailsVO fileDetailsVO) {
		FileDetails fileDetails = new FileDetails();
		if (null != fileDetailsVO) {
			BeanUtils.copyProperties(fileDetailsVO, fileDetails);
		}
		return fileDetails;
	}

	/**
	 * 
	 * @param changeLogVO
	 * @return ChangeLogs
	 */
	private ChangeLogs toChangeLogsJson(ChangeLogVO changeLogVO) {
		ChangeLogs changeLogs = new ChangeLogs();
		if (null != changeLogVO) {
			BeanUtils.copyProperties(changeLogVO, changeLogs);
			if (null != changeLogVO.getModifiedBy()) {
				SolutionTeamMember solutionTeamMember = new SolutionTeamMember();
				BeanUtils.copyProperties(changeLogVO.getModifiedBy(), solutionTeamMember);
				if (!StringUtils.isEmpty(changeLogVO.getModifiedBy().getUserType()))
					solutionTeamMember.setUserType(changeLogVO.getModifiedBy().getUserType().name());
				changeLogs.setModifiedBy(solutionTeamMember);
			}
		}
		return changeLogs;
	}

	private SolutionTeamMember toTeamMemeberJson(TeamMemberVO teamMemberVO) {
		SolutionTeamMember teamMember = new SolutionTeamMember();
		if (teamMemberVO != null) {
			BeanUtils.copyProperties(teamMemberVO, teamMember);
			teamMember.setId(teamMemberVO.getShortId());
			if (teamMemberVO.getUserType() != null)
				teamMember.setUserType(teamMemberVO.getUserType().name());
		}
		return teamMember;
	}

	/**
	 * toRampUpYear
	 * 
	 * @param CalculatedValueRampUpYearVO
	 * @return RampUpYear
	 */
	private RampUpYear toRampUpYear(CalculatedValueRampUpYearVO vo) {
		RampUpYear rampUpYear = new RampUpYear();
		if (vo != null) {
			BeanUtils.copyProperties(vo, rampUpYear);
			rampUpYear.setYear(vo.getYear().intValue());
		}

		return rampUpYear;
	}

	@Override
	public SolutionNsql toEntity(SolutionVO vo) {
		SolutionNsql entity = new SolutionNsql();
		if (vo != null) {
			String id = vo.getId();
			if (id != null && !id.isEmpty() && !id.trim().isEmpty()) {
				entity.setId(id);
			}

			Solution solution = new Solution();
			BeanUtils.copyProperties(vo, solution);
			solution.setPublish(vo.isPublish());
			if (vo.getCreatedBy() != null) {
				solution.setCreatedBy(new CreatedBy(vo.getCreatedBy().getId(), vo.getCreatedBy().getFirstName(),
						vo.getCreatedBy().getLastName(), vo.getCreatedBy().getDepartment(),
						vo.getCreatedBy().getEmail(), vo.getCreatedBy().getMobileNumber()));
			}

			// Logo Details
			if (vo.getLogoDetails() != null) {
				LogoDetails logoDetails = new LogoDetails();
				BeanUtils.copyProperties(vo.getLogoDetails(), logoDetails);
				logoDetails.setIsPredefined(vo.getLogoDetails().isIsPredefined());
				solution.setLogoDetails(logoDetails);
			}

			List<SolutionLocation> locations = new ArrayList<>();
			List<SolutionLocationVO> locationsVO = vo.getLocations();
			if (locationsVO != null && !locationsVO.isEmpty()) {
				for (SolutionLocationVO locationVO : locationsVO) {
					if (locationVO != null) {
						SolutionLocation location = new SolutionLocation();
						BeanUtils.copyProperties(locationVO, location);
						locations.add(location);
					}
				}
			}
			solution.setLocations(locations);

			SolutionDivisionVO divisionvo = vo.getDivision();
			SolutionDivision division = new SolutionDivision();
			if (divisionvo != null) {
				BeanUtils.copyProperties(divisionvo, division);
				SubDivision subdivision = new com.daimler.data.db.jsonb.SubDivision();
				if (divisionvo.getSubdivision() != null)
					BeanUtils.copyProperties(divisionvo.getSubdivision(), subdivision);
				division.setSubdivision(subdivision);
				solution.setDivision(division);
			}

			SolutionProjectStatusVO projectStatusVO = vo.getProjectStatus();
			SolutionProjectStatus projectStatus = new SolutionProjectStatus();
			if (projectStatusVO != null) {
				BeanUtils.copyProperties(projectStatusVO, projectStatus);
				solution.setProjectStatus(projectStatus);
			}

			SolutionCurrentPhase currentPhaseVO = vo.getCurrentPhase();
			CurrentPhase currentPhase = new CurrentPhase();
			if (currentPhaseVO != null) {
				BeanUtils.copyProperties(currentPhaseVO, currentPhase);
				solution.setCurrentPhase(currentPhase);
			} else {
				// Add default phase as kickoff always to enable AllSolutions filter to work
				// properly
				// May be remove hardcoding instead fetch from db?? But dont see much benefit as
				// id is still hardcoded.
				currentPhase.setId("1");
				currentPhase.setName("Kick-off");
				solution.setCurrentPhase(currentPhase);
			}

			List<TeamMemberVO> teamMembersVO = vo.getTeam();
			List<SolutionTeamMember> teamMembers = new ArrayList<>();
			if (teamMembersVO != null && !teamMembersVO.isEmpty())
				teamMembers = teamMembersVO.stream().map(n -> toTeamMemeberJson(n)).collect(Collectors.toList());
			vo.setTeam(teamMembersVO);
			solution.setTeamMembers(teamMembers);

			List<SolutionVO.OpenSegmentsEnum> openSegmentsVOList = vo.getOpenSegments();
			List<String> openSegmentList = new ArrayList<>();
			if (openSegmentsVOList != null && !openSegmentsVOList.isEmpty()) {
				openSegmentsVOList.forEach(openSegmentsEnum -> {
					openSegmentList.add(openSegmentsEnum.name());
				});
				solution.setOpenSegments(openSegmentList);
			}

			MilestoneVO milestoneMasterVO = vo.getMilestones();
			if (milestoneMasterVO != null) {
				List<SolutionMilestonePhaseVO> phases = milestoneMasterVO.getPhases();
				List<SolutionMilestone> milestones = new ArrayList<>();
				if (phases != null && !phases.isEmpty()) {
					for (SolutionMilestonePhaseVO milestoneVO : phases) {
						if (milestoneVO != null) {
							SolutionMilestone milestone = new SolutionMilestone();
							milestone.setMilestoneDescription(milestoneVO.getDescription());
							milestone.setMonth(milestoneVO.getMonth().intValue());
							milestone.setYear(milestoneVO.getYear().intValue());
							SolutionPhase phase = new SolutionPhase();
							SolutionPhaseVO phaseVO = milestoneVO.getPhase();
							BeanUtils.copyProperties(phaseVO, phase);
							milestone.setPhase(phase);
							milestones.add(milestone);
						}
					}
				}
				solution.setMilestones(milestones);

				SolutionRolloutPhaseVO rolloutMasterVO = milestoneMasterVO.getRollouts();
				SolutionRollOut rolloutMaster = new SolutionRollOut();
				rolloutMaster.setRollOutDescription("");
				List<SolutionRollOutDetail> rollOutDetails = new ArrayList<>();
				rolloutMaster.setRollOutDetails(rollOutDetails);
				if (rolloutMasterVO != null) {
					String rolloutDesc = rolloutMasterVO.getDescription();
					if (rolloutDesc == null)
						rolloutDesc = "";
					rolloutMaster.setRollOutDescription(rolloutDesc);
					List<SolutionRolloutDetailsVO> rolloutDetailsVO = rolloutMasterVO.getDetails();
					if (rolloutDetailsVO != null && !rolloutDetailsVO.isEmpty()) {
						for (SolutionRolloutDetailsVO rolloutDetailVO : rolloutDetailsVO) {
							if (rolloutDetailVO != null) {
								SolutionRollOutDetail rolloutDetail = new SolutionRollOutDetail();
								rolloutDetail.setMonth(rolloutDetailVO.getMonth().intValue());
								rolloutDetail.setYear(rolloutDetailVO.getYear().intValue());
								SolutionLocation location = new SolutionLocation();
								SolutionLocationVO locationVO = rolloutDetailVO.getLocation();
								if (locationVO != null)
									BeanUtils.copyProperties(locationVO, location);
								rolloutDetail.setLocation(location);
								rollOutDetails.add(rolloutDetail);
							}
						}
					}
					rolloutMaster.setRollOutDetails(rollOutDetails);
				}
				solution.setRollout(rolloutMaster);
			}

			SolutionDataSourceVO dataSourcesVO = vo.getDataSources();
			List<SolutionDatasource> datasources = new ArrayList<>();
			SolutionDataVolume totalDataVolume = new SolutionDataVolume();
			if (dataSourcesVO != null) {
				List<DataSourceSummaryVO > datasourcesNames = dataSourcesVO.getDataSources();
				Optional.ofNullable(datasourcesNames).ifPresent(l -> l.forEach(dataSourceSummaryVO -> {
					SolutionDatasource solutionDatasource = new SolutionDatasource();
					solutionDatasource = this.toSolutionDatasource(dataSourceSummaryVO);
					datasources.add(solutionDatasource);
				}));
				DataVolumeVO dataVolumeVO = dataSourcesVO.getDataVolume();
				if (dataVolumeVO != null)
					BeanUtils.copyProperties(dataVolumeVO, totalDataVolume);
			}
			solution.setTotalDataVolume(totalDataVolume);
			solution.setDataSources(datasources);
			
			//setting Department details
			solution.setDepartment(vo.getDepartment());
			
			SolutionMarketingVO marketingVO = vo.getMarketing();
			List<MarketingRoleSummary> roleSummary = new ArrayList<>();
			if(marketingVO != null) {
				List<MarketingRoleSummaryVO> marketingRoles = marketingVO.getMarketingRoles();
				if (marketingRoles != null && marketingRoles.size() > 0) {
					roleSummary = marketingRoles.stream().map(n -> toMarketingRoleSummary(n))
							.collect(Collectors.toList());
				}
				solution.setMarketingRoles(roleSummary);				
				solution.setPersonas(marketingVO.getPersonas());
				PersonalizationVO personalizationVO = marketingVO.getPersonalization();
				SolutionPersonalization personalization = new SolutionPersonalization();
				if(personalizationVO != null && Objects.nonNull(personalizationVO)) {
					personalization.setChecked(personalizationVO.isIsChecked());
					personalization.setDescription(personalizationVO.getDescription());
					solution.setPersonalization(personalization);
				}
				else{
					personalization.setChecked(false);
					personalization.setDescription("");
					solution.setPersonalization(personalization);
				}
				List<CustomerJourneyPhaseVO> customerJourneyPhasesVO = marketingVO.getCustomerJourneyPhases();
				if (customerJourneyPhasesVO != null && !customerJourneyPhasesVO.isEmpty()) {
					List<SolutionCustomerJourneyPhase> customerJourneyPhases = new ArrayList<>();
					for (CustomerJourneyPhaseVO customerJourneyPhaseVO : customerJourneyPhasesVO) {
						if (customerJourneyPhaseVO != null) {
							SolutionCustomerJourneyPhase customerJourneyPhase = new SolutionCustomerJourneyPhase();
							BeanUtils.copyProperties(customerJourneyPhaseVO, customerJourneyPhase);
							customerJourneyPhases.add(customerJourneyPhase);
						}
					}
					solution.setCustomerJourneyPhases(customerJourneyPhases);
				}
				List<MarketingCommunicationChannelVO> marketingCommunicationChannelsVO = marketingVO.getMarketingCommunicationChannels();
				if (marketingCommunicationChannelsVO != null && !marketingCommunicationChannelsVO.isEmpty()) {
					List<SolutionMarketingCommunicationChannel> marketingCommunicationChannels = new ArrayList<>();
					for (MarketingCommunicationChannelVO marketingCommunicationChannelVO : marketingCommunicationChannelsVO) {
						if (marketingCommunicationChannelVO != null) {
							SolutionMarketingCommunicationChannel marketingCommunicationChannel = new SolutionMarketingCommunicationChannel();
							BeanUtils.copyProperties(marketingCommunicationChannelVO, marketingCommunicationChannel);
							marketingCommunicationChannels.add(marketingCommunicationChannel);
						}
					}
					solution.setMarketingCommunicationChannels(marketingCommunicationChannels);
				}
			}

			SolutionAnalyticsVO analyticsVO = vo.getAnalytics();
			if (analyticsVO != null) {
				List<AlgorithmVO> algorithmsVO = analyticsVO.getAlgorithms();
				if (algorithmsVO != null && !algorithmsVO.isEmpty()) {
					List<SolutionAlgorithm> algorithms = new ArrayList<>();
					for (AlgorithmVO algorithmVO : algorithmsVO) {
						if (algorithmVO != null) {
							SolutionAlgorithm algorithm = new SolutionAlgorithm();
							BeanUtils.copyProperties(algorithmVO, algorithm);
							algorithms.add(algorithm);
						}
					}
					solution.setAlgorithms(algorithms);
				}
				List<LanguageVO> languagesVO = analyticsVO.getLanguages();
				if (languagesVO != null && !languagesVO.isEmpty()) {
					List<SolutionLanguage> languages = new ArrayList<>();
					for (LanguageVO languageVO : languagesVO) {
						if (languageVO != null) {
							SolutionLanguage language = new SolutionLanguage();
							BeanUtils.copyProperties(languageVO, language);
							languages.add(language);
						}
					}
					solution.setLanguages(languages);
				}
				List<VisualizationVO> visualizationsVO = analyticsVO.getVisualizations();
				if (visualizationsVO != null && !visualizationsVO.isEmpty()) {
					List<SolutionVisualization> visualizations = new ArrayList<>();
					for (VisualizationVO visualizationVO : visualizationsVO) {
						if (visualizationVO != null) {
							SolutionVisualization visualization = new SolutionVisualization();
							BeanUtils.copyProperties(visualizationVO, visualization);
							visualizations.add(visualization);
						}
					}
					solution.setVisualizations(visualizations);
				}
			}

			SolutionSharingVO sharingVO = vo.getSharing();
			if (sharingVO != null) {
				solution.setGitUrl(sharingVO.getGitUrl());
				solution.setResultUrl(sharingVO.getResultUrl());
				ResultVO resultVO = sharingVO.getResult();
				if (resultVO != null) {
					SolutionResult result = new SolutionResult();
					BeanUtils.copyProperties(resultVO, result);
					solution.setResult(result);
				}
			}

			// Setting attachments
			List<FileDetailsVO> fileDetailsVOList = vo.getAttachments();
			List<FileDetails> fileDetailsList = new ArrayList<>();
			if (null != fileDetailsVOList && !fileDetailsVOList.isEmpty()) {
				fileDetailsList = fileDetailsVOList.stream().map(n -> toFileDetailsJson(n))
						.collect(Collectors.toList());
				solution.setAttachments(fileDetailsList);
			}

			// Setting digitalValueDetails
			SolutionDigitalValueVO digitalValueDetailsVO = vo.getDigitalValue();
			SolutionDigitalValue digitalValueDetails = new SolutionDigitalValue();
			if (digitalValueDetailsVO != null) {
				BeanUtils.copyProperties(digitalValueDetailsVO, digitalValueDetails);
				// Setting projectController
				List<TeamMemberVO> projectControllers = digitalValueDetailsVO.getProjectControllers();
				List<SolutionTeamMember> solutionTeamMembers = new ArrayList<SolutionTeamMember>();
				if (projectControllers != null) {
					solutionTeamMembers = projectControllers.stream().map(n -> toTeamMemeberJson(n))
							.collect(Collectors.toList());
				}
				digitalValueDetails.setProjectControllers(solutionTeamMembers);

				// Setting costDrivers
				List<CostFactorVO> costFactorVOList = digitalValueDetailsVO.getCostDrivers();
				List<CostDriver> costDriverList = new ArrayList<>();
				if (null != costFactorVOList && !costFactorVOList.isEmpty()) {
					costDriverList = costFactorVOList.stream().map(n -> toCostDriverJson(n))
							.collect(Collectors.toList());
					digitalValueDetails.setCostDrivers(costDriverList);
				}

				// Setting valueDrivers
				List<ValueFactorVO> valueFactorVOList = digitalValueDetailsVO.getValueDrivers();
				List<ValueDriver> valueDriverList = new ArrayList<>();
				if (null != valueFactorVOList && !valueFactorVOList.isEmpty()) {
					valueDriverList = valueFactorVOList.stream().map(n -> toValueDriverJson(n))
							.collect(Collectors.toList());
					digitalValueDetails.setValueDrivers(valueDriverList);
				}

				// Setting attachments
				List<FileDetailsVO> digitalValueFileDetailsVOList = digitalValueDetailsVO.getAttachments();
				List<FileDetails> digitalValueFileDetailsList = new ArrayList<>();
				if (null != digitalValueFileDetailsVOList && !digitalValueFileDetailsVOList.isEmpty()) {
					digitalValueFileDetailsList = digitalValueFileDetailsVOList.stream().map(n -> toFileDetailsJson(n))
							.collect(Collectors.toList());
					digitalValueDetails.setAttachments(digitalValueFileDetailsList);
				}

				// Setting assessment
				AssessmentDetailsVO assessmentDetailsVO = digitalValueDetailsVO.getAssessment();
				AssessmentDetails assessmentDetails = new AssessmentDetails();
				if (null != assessmentDetailsVO) {
					BeanUtils.copyProperties(assessmentDetailsVO, assessmentDetails);
					digitalValueDetails.setAssessment(assessmentDetails);
				}

				// Setting permissions
				List<TeamMemberVO> teamPermissionMembersVO = digitalValueDetailsVO.getPermissions();
				if (teamPermissionMembersVO != null && !teamPermissionMembersVO.isEmpty()) {
					List<SolutionTeamMember> teamPermissionMembers = teamPermissionMembersVO.stream()
							.map(n -> toTeamMemeberJson(n)).collect(Collectors.toList());
					digitalValueDetails.setPermissions(teamPermissionMembers);
				}

				// Setting changeLogs
				List<ChangeLogVO> changeLogVOList = digitalValueDetailsVO.getChangeLogs();
				List<ChangeLogs> changeLogsList = new ArrayList<>();
				if (null != changeLogVOList && !changeLogVOList.isEmpty()) {
					changeLogsList = changeLogVOList.stream().map(n -> toChangeLogsJson(n))
							.collect(Collectors.toList());
					digitalValueDetails.setChangeLogs(changeLogsList);
				}
				// Setting ValueCalculator
				ValueCalculatorVO valueCalculatorVO = digitalValueDetailsVO.getValueCalculator();
				ValueCalculator valueCalculator = new ValueCalculator();
				if (null != valueCalculatorVO) {
					BeanUtils.copyProperties(valueCalculatorVO, valueCalculator);
					List<CalculatedValueRampUpYearVO> calculatedValueRampUpYearVOs = valueCalculatorVO
							.getCalculatedValueRampUpYears();
					List<RampUpYear> calculatedValueRampUpYears = new ArrayList<RampUpYear>();
					if (null != calculatedValueRampUpYearVOs && !calculatedValueRampUpYearVOs.isEmpty())
						calculatedValueRampUpYears = calculatedValueRampUpYearVOs.stream().map(n -> toRampUpYear(n))
								.collect(Collectors.toList());
					valueCalculator.setCalculatedValueRampUpYears(calculatedValueRampUpYears);
					// Setting Cost Factor summary
					if (null != valueCalculatorVO.getCostFactorSummary()) {
						CostFactorSummary costFactorSummary = new CostFactorSummary();
						BeanUtils.copyProperties(valueCalculatorVO.getCostFactorSummary(), costFactorSummary);
						valueCalculator.setCostFactorSummary(costFactorSummary);
					}
					// Setting Value Factor summary
					if (null != valueCalculatorVO.getValueFactorSummary()) {
						ValueFactorSummary valueFactorSummary = new ValueFactorSummary();
						BeanUtils.copyProperties(valueCalculatorVO.getValueFactorSummary(), valueFactorSummary);
						valueCalculator.setValueFactorSummary(valueFactorSummary);
					}
					// Setting Calculated Digital value
					if (null != valueCalculatorVO.getCalculatedDigitalValue()) {
						CalculatedDigitalValue calculatedDigitalValue = new CalculatedDigitalValue();
						BeanUtils.copyProperties(valueCalculatorVO.getCalculatedDigitalValue(), calculatedDigitalValue);
						valueCalculator.setCalculatedDigitalValue(calculatedDigitalValue);
						if (calculatedDigitalValue.getValue() != null) {
							digitalValueDetails.setDigitalValue(calculatedDigitalValue.getValue());
						}
					}
					digitalValueDetails.setValueCalculator(valueCalculator);
				}

			}
			solution.setDigitalValueDetails(digitalValueDetails);

			// Setting dataComplianceDetails
			SolutionDataCompliance dataComplianceDetails = new SolutionDataCompliance();
			SolutionDataComplianceVO solutionDataComplianceVO = vo.getDataCompliance();
			if (null != solutionDataComplianceVO) {
				BeanUtils.copyProperties(solutionDataComplianceVO, dataComplianceDetails);
				dataComplianceDetails.setQuickCheck(solutionDataComplianceVO.isQuickCheck());
				dataComplianceDetails.setUseCaseDescAndEval(solutionDataComplianceVO.isUseCaseDescAndEval());
				dataComplianceDetails.setExpertGuidelineNeeded(solutionDataComplianceVO.isExpertGuidelineNeeded());
				dataComplianceDetails.setReadyForImplementation(solutionDataComplianceVO.isReadyForImplementation());
				// Setting attachments
				List<FileDetailsVO> dataComplianceFileDetailsVOList = solutionDataComplianceVO.getAttachments();
				List<FileDetails> dataComplianceFileDetailsList = new ArrayList<>();
				if (null != dataComplianceFileDetailsVOList && !dataComplianceFileDetailsVOList.isEmpty()) {
					dataComplianceFileDetailsList = dataComplianceFileDetailsVOList.stream()
							.map(n -> toFileDetailsJson(n)).collect(Collectors.toList());
				}
				dataComplianceDetails.setAttachments(dataComplianceFileDetailsList);

				// Setting linkDetails
				List<LinkVO> linkVOs = solutionDataComplianceVO.getLinks();
				List<SolutionComplianceLink> solutionComplianceLinks = new ArrayList<>();
				if (null != linkVOs && !linkVOs.isEmpty()) {
					for (LinkVO linkVO : linkVOs) {
						if (linkVO != null) {
							SolutionComplianceLink solutionComplianceLink = new SolutionComplianceLink();
							BeanUtils.copyProperties(linkVO, solutionComplianceLink);
							solutionComplianceLinks.add(solutionComplianceLink);
						}
					}
					dataComplianceDetails.setLinkDetails(solutionComplianceLinks);
				}

				// Setting complianceOfficer
				List<TeamMemberVO> teamMemberVOList = solutionDataComplianceVO.getComplianceOfficers();
				List<SolutionTeamMember> solutionTeamMembers = new ArrayList<>();
				if (teamMemberVOList != null && !teamMemberVOList.isEmpty())
					solutionTeamMembers = teamMemberVOList.stream().map(n -> toTeamMemeberJson(n))
							.collect(Collectors.toList());
				dataComplianceDetails.setComplianceOfficers(solutionTeamMembers);

			}
			solution.setDataComplianceDetails(dataComplianceDetails);

			SolutionPortfolioVO solutionPortfolioVO = vo.getPortfolio();
			if (solutionPortfolioVO != null) {
				solution.setUsesExistingInternalPlatforms(solutionPortfolioVO.isUsesExistingInternalPlatforms());
				solution.setSolutionOnCloud(solutionPortfolioVO.isSolutionOnCloud());
				solution.setDnaNotebookId(solutionPortfolioVO.getDnaNotebookId());
				solution.setDataikuProjectKey(solutionPortfolioVO.getDnaDataikuProjectId());
				solution.setDnaSubscriptionAppId(solutionPortfolioVO.getDnaSubscriptionAppId());
				List<SolutionPlatform> platforms = new ArrayList<>();
				List<PlatformVO> platformsVO = solutionPortfolioVO.getPlatforms();
				if (platformsVO != null && !platformsVO.isEmpty()) {
					for (PlatformVO platformVO : platformsVO) {
						if (platformVO != null) {
							SolutionPlatform platform = new SolutionPlatform();
							BeanUtils.copyProperties(platformVO, platform);
							platforms.add(platform);
						}
					}
				}
				solution.setPlatforms(platforms);
			}

			// Setting SkillSummary
			if (!ObjectUtils.isEmpty(vo.getSkills())) {
				List<SkillSummary> skills = vo.getSkills().stream().map(n -> toSkillSummary(n))
						.collect(Collectors.toList());
				solution.setSkills(skills);
			}

			entity.setData(solution);
		}
		return entity;
	}

	/*
	 * To convert 
	 */
	private SolutionDatasource toSolutionDatasource(DataSourceSummaryVO vo) {
		SolutionDatasource solutionDatasource = null;
		if (vo != null) {
			solutionDatasource = new SolutionDatasource();
			solutionDatasource.setName(vo.getDataSource());
			solutionDatasource.setWeightage(vo.getWeightage());
		}
		return solutionDatasource;
	}
	
	/*
	 * Converting SkillSummaryVO to SkillSummary (vo to entity)
	 */
	private SkillSummary toSkillSummary(SkillSummaryVO skillSummaryVO) {
		SkillSummary skillSummary = null;
		if (skillSummaryVO != null) {
			skillSummary = new SkillSummary();
			BeanUtils.copyProperties(skillSummaryVO, skillSummary);
		}
		return skillSummary;
	}
	
	/*
	 * Converting MarketingRoleSummaryVO to SkillSummary (vo to entity)
	 */
	private MarketingRoleSummary toMarketingRoleSummary(MarketingRoleSummaryVO marketingRoleSummaryVO) {
		MarketingRoleSummary marketingRoleSummary = null;
		if (marketingRoleSummaryVO != null) {
			marketingRoleSummary = new MarketingRoleSummary();
			BeanUtils.copyProperties(marketingRoleSummaryVO, marketingRoleSummary);
		}
		return marketingRoleSummary;
	}

	public SolutionCollection applyBookMarkflag(List<SolutionVO> solutionVOListVO, List<String> bookmarkedSolutions,
			String userId) {
		if (solutionVOListVO != null && solutionVOListVO.size() > 0) {
			for (SolutionVO sol : solutionVOListVO) {
				if (sol != null) {
					if (bookmarkedSolutions != null)
						if (bookmarkedSolutions.contains(sol.getId())) {
							sol.setBookmarked(true);
						} else {
							sol.setBookmarked(false);
						}
				}
			}
		}
		SolutionCollection solutionCollection = new SolutionCollection();
		solutionCollection.setRecords(solutionVOListVO);
		return solutionCollection;
	}

	/**
	 * valueCalculator Calculate Digital value based on Value driver & Cost Driver
	 * 
	 * @param solutionDigitalValueVO
	 * @return
	 */
	public ValueCalculatorVO valueCalculator(SolutionDigitalValueVO solutionDigitalValueVO) {
		ValueCalculatorVO valueCalculatorVO = new ValueCalculatorVO();
		List<ValueRampUpYearVO> consolidatedValueRampUpVOs = consolidateValueRampUpYearVO(
				solutionDigitalValueVO.getValueDrivers());
		List<CostRampUpYearVO> consolidatedCostRampUpYearVOs = consolidateCostRampUpYearVO(
				solutionDigitalValueVO.getCostDrivers());
		List<CalculatedValueRampUpYearVO> calculatedValueRampUpYearVOs = calculateCalculatedValueRampUpYearVO(
				consolidatedValueRampUpVOs, consolidatedCostRampUpYearVOs);

		valueCalculatorVO.setCalculatedValueRampUpYears(calculatedValueRampUpYearVOs);

		// setting CalculatedDigitalValueVO
		CalculatedDigitalValueVO calculatedDigitalValueVO = new CalculatedDigitalValueVO();
		calculatedDigitalValueVO.setValueAt(new BigDecimal(100));
		if (null != calculatedValueRampUpYearVOs && !calculatedValueRampUpYearVOs.isEmpty()) {
			for (CalculatedValueRampUpYearVO calculatedValueRampUpYearVO : calculatedValueRampUpYearVOs) {
				if (null != calculatedValueRampUpYearVO.getPercent()
						&& calculatedValueRampUpYearVO.getPercent().compareTo(new BigDecimal("100")) >= 0) {
					calculatedDigitalValueVO.setYear(calculatedValueRampUpYearVO.getYear());
					calculatedDigitalValueVO.setValue(calculatedValueRampUpYearVO.getValue());
					break;
				}
			}
			valueCalculatorVO.setCalculatedDigitalValue(calculatedDigitalValueVO);
		}

		// Setting breakEvenPoint
		valueCalculatorVO
				.setBreakEvenPoint(calculateBreakEvenPoint(consolidatedCostRampUpYearVOs, consolidatedValueRampUpVOs));

		// Setting ValueFactorSummaryVO
		ValueFactorSummaryVO valueFactorSummaryVO = new ValueFactorSummaryVO();
		if (null != consolidatedValueRampUpVOs && !consolidatedValueRampUpVOs.isEmpty()) {
			String valueYearDuration = consolidatedValueRampUpVOs.get(0).getYear() + "-"
					+ consolidatedValueRampUpVOs.get(consolidatedValueRampUpVOs.size() - 1).getYear();
			valueFactorSummaryVO.setYear(valueYearDuration);
			BigDecimal value = BigDecimal.ZERO;
			for (ValueRampUpYearVO valueRampUpYearVO : consolidatedValueRampUpVOs) {
				value = value.add(valueRampUpYearVO.getValue());
			}
			valueFactorSummaryVO.setValue(value);
		}
		valueCalculatorVO.setValueFactorSummary(valueFactorSummaryVO);

		// Setting CostFactorSummaryVO
		CostFactorSummaryVO costFactorSummaryVO = new CostFactorSummaryVO();
		if (null != consolidatedCostRampUpYearVOs && !consolidatedCostRampUpYearVOs.isEmpty()) {
			String costYearDuration = consolidatedCostRampUpYearVOs.get(0).getYear() + "-"
					+ consolidatedCostRampUpYearVOs.get(consolidatedCostRampUpYearVOs.size() - 1).getYear();
			costFactorSummaryVO.setYear(costYearDuration);
			BigDecimal cost = BigDecimal.ZERO;
			for (CostRampUpYearVO costRampUpYearVO : consolidatedCostRampUpYearVOs) {
				cost = cost.add(costRampUpYearVO.getValue());
			}
			costFactorSummaryVO.setValue(cost);
		}
		valueCalculatorVO.setCostFactorSummary(costFactorSummaryVO);

		return valueCalculatorVO;
	}

	/**
	 * calculateBreakEvenPoint
	 * <P>
	 * responsible to calculate breakevenPoint
	 * <P>
	 * if Consolidated value >= Consolidated cost
	 * 
	 * @param consolidatedCostRampUpYearVOs
	 * @param consolidatedValueRampUpVOs
	 * @return
	 */
	private BigDecimal calculateBreakEvenPoint(List<CostRampUpYearVO> consolidatedCostRampUpYearVOs,
			List<ValueRampUpYearVO> consolidatedValueRampUpVOs) {
		BigDecimal breakEvenPoint = BigDecimal.ZERO;
		BigDecimal cost = BigDecimal.ZERO;
		BigDecimal value = BigDecimal.ZERO;
		BigDecimal year = null;
		if (null != consolidatedCostRampUpYearVOs && !consolidatedCostRampUpYearVOs.isEmpty()) {
			for (int i = 0; i < consolidatedCostRampUpYearVOs.size(); i++) {
				cost = cost.add(consolidatedCostRampUpYearVOs.get(i).getValue());
				if (null != consolidatedValueRampUpVOs && !consolidatedValueRampUpVOs.isEmpty()
						&& consolidatedValueRampUpVOs.get(0).getYear()
								.compareTo(consolidatedCostRampUpYearVOs.get(i).getYear()) <= 0) {
					value = BigDecimal.ZERO;
					for (int j = 0; j < consolidatedValueRampUpVOs.size(); j++) {
						if (consolidatedValueRampUpVOs.get(j).getYear()
								.compareTo(consolidatedCostRampUpYearVOs.get(i).getYear()) <= 0) {
							value = value.add(consolidatedValueRampUpVOs.get(j).getValue());
							year = consolidatedValueRampUpVOs.get(j).getYear();
						}
						if (value.compareTo(cost) >= 0) {
							breakEvenPoint = year;
							break;
						}
						if (consolidatedValueRampUpVOs.get(j).getYear()
								.compareTo(consolidatedCostRampUpYearVOs.get(i).getYear()) >= 0)
							break;
					}
					if (value.compareTo(cost) >= 0) {
						break;
					}
				}

			}
		}

		return breakEvenPoint;
	}

	/**
	 * consolidateValueRampUpYearVO Consolidate all ValueRampUpYearVO of multiple
	 * ValueFactorVO into single List Calculate Average of percentage for different
	 * year Summation of value of the same year
	 * 
	 * @param valueDrivers
	 * @return List<ValueRampUpYearVO>
	 */
	private List<ValueRampUpYearVO> consolidateValueRampUpYearVO(List<ValueFactorVO> valueDrivers) {
		List<ValueRampUpYearVO> consolidatedValueRampUpVOs = new ArrayList<ValueRampUpYearVO>();
		Map<BigDecimal, Integer> occurrenceOfYearsMap = new HashMap<BigDecimal, Integer>();
		if (null != valueDrivers && !valueDrivers.isEmpty()) {
			for (ValueFactorVO valueFactorVO : valueDrivers) {
				if (consolidatedValueRampUpVOs.isEmpty()) {
					consolidatedValueRampUpVOs = valueFactorVO.getRampUp();
					for (ValueRampUpYearVO valueRampUpYearVO : consolidatedValueRampUpVOs) {
						occurrenceOfYearsMap.put(valueRampUpYearVO.getYear(), 1);
					}
				} else {
					Map<BigDecimal, ValueRampUpYearVO> consolidatedValueRampUpVOsMap = consolidatedValueRampUpVOs
							.stream().collect(Collectors.toMap(ValueRampUpYearVO::getYear, Function.identity()));
					for (ValueRampUpYearVO valueRampUpYearVO : valueFactorVO.getRampUp()) {
						ValueRampUpYearVO tempValueRampUpYearVO = consolidatedValueRampUpVOsMap
								.get(valueRampUpYearVO.getYear());
						if (null != tempValueRampUpYearVO) {
							if (null != valueRampUpYearVO.getValue()) {
								tempValueRampUpYearVO
										.setValue(tempValueRampUpYearVO.getValue().add(valueRampUpYearVO.getValue()));
							}
							if (null != valueRampUpYearVO.getPercent()) {
								tempValueRampUpYearVO.setPercent(
										tempValueRampUpYearVO.getPercent().add(valueRampUpYearVO.getPercent()));
							}
							consolidatedValueRampUpVOsMap.put(tempValueRampUpYearVO.getYear(), tempValueRampUpYearVO);
							occurrenceOfYearsMap.put(tempValueRampUpYearVO.getYear(),
									occurrenceOfYearsMap.get(tempValueRampUpYearVO.getYear()) + 1);
						} else {
							consolidatedValueRampUpVOsMap.put(valueRampUpYearVO.getYear(), valueRampUpYearVO);
							occurrenceOfYearsMap.put(valueRampUpYearVO.getYear(), 1);
						}
					}
					consolidatedValueRampUpVOs = (new TreeMap<>(consolidatedValueRampUpVOsMap)).values().stream()
							.collect(Collectors.toCollection(ArrayList::new));
				}
			}
			for (ValueRampUpYearVO valueRampUpYearVO : consolidatedValueRampUpVOs) {
				valueRampUpYearVO.setPercent(valueRampUpYearVO.getPercent().divide(
						new BigDecimal(occurrenceOfYearsMap.get(valueRampUpYearVO.getYear())), 2,
						RoundingMode.HALF_UP));
			}
		}
		return consolidatedValueRampUpVOs;
	}

	/**
	 * consolidateCostRampUpYearVO Consolidate all CostRampUpYearVO of multiple
	 * CostFactorVO into single one Summation of Cost for the same year
	 * 
	 * @param costDrivers
	 * @return List<CostRampUpYearVO>
	 */
	private List<CostRampUpYearVO> consolidateCostRampUpYearVO(List<CostFactorVO> costDrivers) {
		List<CostRampUpYearVO> consolidatedCostRampUpYearVOs = new ArrayList<CostRampUpYearVO>();
		if (null != costDrivers && !costDrivers.isEmpty()) {
			for (CostFactorVO costFactorVO : costDrivers) {
				if (consolidatedCostRampUpYearVOs.isEmpty()) {
					consolidatedCostRampUpYearVOs = costFactorVO.getRampUp();
				} else {
					Map<BigDecimal, CostRampUpYearVO> consolidatedCostRampUpMap = consolidatedCostRampUpYearVOs.stream()
							.collect(Collectors.toMap(CostRampUpYearVO::getYear, Function.identity()));
					for (CostRampUpYearVO costRampUpYearVO : costFactorVO.getRampUp()) {
						CostRampUpYearVO tempCostRampUpYearVO = consolidatedCostRampUpMap
								.get(costRampUpYearVO.getYear());
						if (null != tempCostRampUpYearVO) {
							if (null != costRampUpYearVO.getValue())
								tempCostRampUpYearVO
										.setValue(tempCostRampUpYearVO.getValue().add(costRampUpYearVO.getValue()));
							consolidatedCostRampUpMap.put(tempCostRampUpYearVO.getYear(), tempCostRampUpYearVO);
						} else {
							consolidatedCostRampUpMap.put(costRampUpYearVO.getYear(), costRampUpYearVO);
						}
					}
					consolidatedCostRampUpYearVOs = (new TreeMap<>(consolidatedCostRampUpMap)).values().stream()
							.collect(Collectors.toCollection(ArrayList::new));
				}
			}
		}
		return consolidatedCostRampUpYearVOs;
	}

	/**
	 * calculateCalculatedValueRampUpYearVO Calculate final value for digital value
	 * 
	 * @param consolidatedValueRampUpVOs
	 * @param consolidatedCostRampUpYearVOs
	 * @return List<CalculatedValueRampUpYearVO>
	 */
	private List<CalculatedValueRampUpYearVO> calculateCalculatedValueRampUpYearVO(
			List<ValueRampUpYearVO> consolidatedValueRampUpVOs, List<CostRampUpYearVO> consolidatedCostRampUpYearVOs) {
		List<CalculatedValueRampUpYearVO> calculatedValueRampUpYearVOs = new ArrayList<CalculatedValueRampUpYearVO>();
		if (null != consolidatedValueRampUpVOs && !consolidatedValueRampUpVOs.isEmpty()) {
			for (ValueRampUpYearVO valueRampUpYearVO : consolidatedValueRampUpVOs) {
				CalculatedValueRampUpYearVO calculatedValueRampUpYearVO = new CalculatedValueRampUpYearVO();
				BeanUtils.copyProperties(valueRampUpYearVO, calculatedValueRampUpYearVO);
				calculatedValueRampUpYearVOs.add(calculatedValueRampUpYearVO);
			}
		}
		Map<BigDecimal, CalculatedValueRampUpYearVO> consolidatedCalculatedValueRampUpMap = calculatedValueRampUpYearVOs
				.stream().collect(Collectors.toMap(CalculatedValueRampUpYearVO::getYear, Function.identity()));

		if (null != consolidatedCostRampUpYearVOs && !consolidatedCostRampUpYearVOs.isEmpty()) {
			for (CostRampUpYearVO costRampUpYearVO : consolidatedCostRampUpYearVOs) {
				CalculatedValueRampUpYearVO temp = consolidatedCalculatedValueRampUpMap.get(costRampUpYearVO.getYear());
				if (null != temp) {
					temp.setValue(temp.getValue().subtract(costRampUpYearVO.getValue()));
					consolidatedCalculatedValueRampUpMap.put(costRampUpYearVO.getYear(), temp);
				} else {
					CalculatedValueRampUpYearVO calculatedValueRampUpYearVO = new CalculatedValueRampUpYearVO();
					BeanUtils.copyProperties(costRampUpYearVO, calculatedValueRampUpYearVO);
					calculatedValueRampUpYearVO
							.setValue(BigDecimal.ZERO.subtract(calculatedValueRampUpYearVO.getValue()));
					consolidatedCalculatedValueRampUpMap.put(costRampUpYearVO.getYear(), calculatedValueRampUpYearVO);
				}
			}
		}

		calculatedValueRampUpYearVOs = (new TreeMap<>(consolidatedCalculatedValueRampUpMap)).values().stream()
				.collect(Collectors.toCollection(ArrayList::new));
		return calculatedValueRampUpYearVOs;
	}

	/*
	 * public SolutionCollection addMetaData(List<SolutionVO> solutionVOListVO,
	 * List<String> bookmarkedSolutions, String userId) { int activeSolutions = 0;
	 * int onHoldSolutions = 0; int closedSolutions = 0; int bookMarkedSolutions =
	 * 0; int createdBySolutions = 0; int mySolutions = 0;
	 * 
	 * int kickOffSols = 0; int ideationSols = 0; int pocSols = 0; int pilotSols =
	 * 0; int professionalizationSols = 0; int rolloutSols = 0;
	 * 
	 * if (solutionVOListVO != null && solutionVOListVO.size() > 0) { for(SolutionVO
	 * sol : solutionVOListVO) { if(sol!=null) { SolutionProjectStatusVO status =
	 * sol.getProjectStatus(); if(status!= null ) { String projectStatusName =
	 * status.getName(); if("Active".equalsIgnoreCase(projectStatusName))
	 * activeSolutions++; if("On hold".equalsIgnoreCase(projectStatusName))
	 * onHoldSolutions++; if("Closed".equalsIgnoreCase(projectStatusName))
	 * closedSolutions++; }
	 * 
	 * if(bookmarkedSolutions != null) if(bookmarkedSolutions.contains(sol.getId()))
	 * { bookMarkedSolutions++; sol.setBookmarked(true); }else {
	 * sol.setBookmarked(false); }
	 * 
	 * CreatedByVO solCreatedBy = sol.getCreatedBy(); if(solCreatedBy!=null)
	 * if(userId.equalsIgnoreCase(solCreatedBy.getId())) createdBySolutions++;
	 * 
	 * SolutionCurrentPhase currentPhase = sol.getCurrentPhase(); if(currentPhase !=
	 * null && currentPhase.getName()!= null) { String phaseName =
	 * currentPhase.getName(); switch(phaseName) { case "Serie" : kickOffSols++;
	 * break; case "Ideation" : ideationSols++; break; case
	 * "Concept Development / Proof of concept" : pocSols++; break; case "Pilot" :
	 * pilotSols++; break; case "Professionalization" : professionalizationSols++;
	 * break; case "Rollout" : rolloutSols++; break; default : break; } }
	 * 
	 * } } } SolutionCollection solutionCollection = new SolutionCollection();
	 * solutionCollection.setRecords(solutionVOListVO);
	 * 
	 * SolutionAnalyticsVO metaData = new SolutionAnalyticsVO();
	 * 
	 * SolutionPhaseAnalyticsVO phaseAnalytics = new SolutionPhaseAnalyticsVO();
	 * phaseAnalytics.setKickOff(String.valueOf(kickOffSols));
	 * phaseAnalytics.setIdeation(String.valueOf(ideationSols));
	 * phaseAnalytics.setConceptDevelopment(String.valueOf(pocSols));
	 * phaseAnalytics.setPilot(String.valueOf(pilotSols));
	 * phaseAnalytics.setProfessionalization(String.valueOf(professionalizationSols)
	 * ); phaseAnalytics.setRollout(String.valueOf(rolloutSols));
	 * metaData.setPhaseAnalytics(phaseAnalytics);
	 * 
	 * SolutionUseCaseCountsVO useCaseCounts = new SolutionUseCaseCountsVO();
	 * useCaseCounts.setActive(String.valueOf(activeSolutions));
	 * useCaseCounts.setOnHold(String.valueOf(onHoldSolutions));
	 * useCaseCounts.setClosed(String.valueOf(closedSolutions));
	 * useCaseCounts.setBookMarked(String.valueOf(bookMarkedSolutions));
	 * useCaseCounts.setCreatedBy(String.valueOf(createdBySolutions));
	 * useCaseCounts.setMemberOf(String.valueOf(mySolutions));
	 * metaData.setUsecaseCounts(useCaseCounts);
	 * 
	 * solutionCollection.setAnalytics(metaData);
	 * 
	 * return solutionCollection;
	 * 
	 * }
	 */

	/**
	 * digitalValueCompare Comparing existing Digital value with new one for
	 * differences
	 * 
	 * @param request
	 * @param existing
	 * @param userStore
	 * @return
	 */
	public SolutionDigitalValueVO digitalValueCompare(SolutionDigitalValueVO request, SolutionDigitalValueVO existing,
			CreatedByVO currentUser) {
		List<ChangeLogVO> changeLogsVO = this.jsonObjectCompare(request, existing, currentUser);
		if (null != existing.getChangeLogs()) {
			changeLogsVO.addAll(existing.getChangeLogs());
		}
		request.setChangeLogs(changeLogsVO);

		return request;
	}

	/**
	 * Simple GSON based json objects compare and difference provider
	 * 
	 * @param request
	 * @param existing
	 * @param currentUser
	 * @return
	 */
	public List<ChangeLogVO> jsonObjectCompare(Object request, Object existing, CreatedByVO currentUser) {
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, Object>>() {
		}.getType();
		Map<String, Object> leftMap = gson.fromJson(gson.toJson(existing), type);
		Map<String, Object> rightMap = gson.fromJson(gson.toJson(request), type);

		Map<String, Object> leftFlatMap = SolutionAssembler.flatten(leftMap);
		Map<String, Object> rightFlatMap = SolutionAssembler.flatten(rightMap);

		MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);

		TeamMemberVO teamMemberVO = new TeamMemberVO();
		BeanUtils.copyProperties(currentUser, teamMemberVO);
		teamMemberVO.setShortId(currentUser.getId());
		Date changeDate = new Date();

		List<ChangeLogVO> changeLogsVO = new ArrayList<ChangeLogVO>();
		ChangeLogVO changeLogVO = null;
		// Checking for Removed values
		if (null != difference.entriesOnlyOnLeft() && !difference.entriesOnlyOnLeft().isEmpty()) {
			for (Entry<String, Object> entry : difference.entriesOnlyOnLeft().entrySet()) {
				if (!(entry.getKey().toString().contains(ConstantsUtility.CHANGE_LOGS)
						|| entry.getKey().toString().contains(ConstantsUtility.VALUE_CALCULATOR)
						|| entry.getKey().toString().contains(ConstantsUtility.ID))) {
					changeLogVO = new ChangeLogVO();
					changeLogVO.setModifiedBy(teamMemberVO);
					changeLogVO.setChangeDate(changeDate);
					changeLogVO.setFieldChanged(entry.getKey());
					changeLogVO.setOldValue(entry.getValue().toString());
					// setting change Description Starts
					changeLogVO.setChangeDescription(
							toChangeDescription(entry.getKey(), entry.getValue().toString(), null));
					changeLogsVO.add(changeLogVO);
				}
			}
		}
		// Checking for Added values
		if (null != difference.entriesOnlyOnRight() && !difference.entriesOnlyOnRight().isEmpty()) {
			for (Entry<String, Object> entry : difference.entriesOnlyOnRight().entrySet()) {
				if (!(entry.getKey().toString().contains(ConstantsUtility.CHANGE_LOGS)
						|| entry.getKey().toString().contains(ConstantsUtility.VALUE_CALCULATOR)
						|| entry.getKey().toString().contains(ConstantsUtility.ID))) {
					changeLogVO = new ChangeLogVO();
					changeLogVO.setModifiedBy(teamMemberVO);
					changeLogVO.setChangeDate(changeDate);
					changeLogVO.setFieldChanged(entry.getKey());
					changeLogVO.setNewValue(entry.getValue().toString());
					// setting change Description
					changeLogVO.setChangeDescription(
							toChangeDescription(entry.getKey(), null, entry.getValue().toString()));
					changeLogsVO.add(changeLogVO);
				}
			}
		}
		// Checking for value differences
		if (null != difference.entriesDiffering() && !difference.entriesDiffering().isEmpty()) {
			for (Entry<String, ValueDifference<Object>> entry : difference.entriesDiffering().entrySet()) {
				if (!(entry.getKey().toString().contains(ConstantsUtility.CHANGE_LOGS)
						|| entry.getKey().toString().contains(ConstantsUtility.VALUE_CALCULATOR)
						|| entry.getKey().toString().contains(ConstantsUtility.ID))) {
					changeLogVO = new ChangeLogVO();
					changeLogVO.setModifiedBy(teamMemberVO);
					changeLogVO.setChangeDate(changeDate);
					changeLogVO.setFieldChanged(entry.getKey());
					changeLogVO.setOldValue(entry.getValue().leftValue().toString());
					changeLogVO.setNewValue(entry.getValue().rightValue().toString());
					// setting change Description
					changeLogVO.setChangeDescription(toChangeDescription(entry.getKey(),
							entry.getValue().leftValue().toString(), entry.getValue().rightValue().toString()));
					changeLogsVO.add(changeLogVO);
				}
			}
		}
		return changeLogsVO;
	}

	/**
	 * flatten the map
	 * 
	 * @param map
	 * @return Map<String, Object>
	 */
	public static Map<String, Object> flatten(Map<String, Object> map) {
		if (null == map || map.isEmpty()) {
			return new HashMap<String, Object>();
		} else {
			return map.entrySet().stream().flatMap(SolutionAssembler::flatten).collect(LinkedHashMap::new,
					(m, e) -> m.put("/" + e.getKey(), e.getValue()), LinkedHashMap::putAll);
		}
	}

	/**
	 * flatten map entry
	 * 
	 * @param entry
	 * @return
	 */
	private static Stream<Entry<String, Object>> flatten(Entry<String, Object> entry) {

		if (entry == null) {
			return Stream.empty();
		}

		if (entry.getValue() instanceof Map<?, ?>) {
			Map<?, ?> properties = (Map<?, ?>) entry.getValue();
			return properties.entrySet().stream()
					.flatMap(e -> flatten(new SimpleEntry<>(entry.getKey() + "/" + e.getKey(), e.getValue())));
		}

		if (entry.getValue() instanceof List<?>) {
			List<?> list = (List<?>) entry.getValue();
			return IntStream.range(0, list.size())
					.mapToObj(i -> new SimpleEntry<String, Object>(entry.getKey() + "/" + i, list.get(i)))
					.flatMap(SolutionAssembler::flatten);
		}

		return Stream.of(entry);
	}

	private String toHumanReadableFormat(String raw) {
		if (raw != null) {
			String seperated = raw.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])",
					"(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
			String formatted = Character.toUpperCase(seperated.charAt(0)) + seperated.substring(1);
			return formatted;
		} else
			return raw;
	}

	/**
	 * toChangeDescription convert given keyString to changeDescription
	 * 
	 * @param keyString
	 * @param fromValue
	 * @param toValue
	 * @return changeDescription
	 */
	private String toChangeDescription(String keyString, String fromValue, String toValue) {
		keyString = keyString.substring(1);
		String[] keySet = keyString.split("/");
		String at = null;
		int indexValue = 0;
		String fieldValue = "";
		StringBuilder changeDescription = new StringBuilder();
		if (keySet.length > 0) {
			fieldValue = ConstantsUtility.staticMap.get(keySet[0]) != null ? ConstantsUtility.staticMap.get(keySet[0])
					: keySet[0];
			fieldValue = toHumanReadableFormat(fieldValue);
			changeDescription.append(fieldValue + ": ");
		}
		boolean flag = false;
		for (int i = (keySet.length - 1), index = keySet.length; i >= 0; i--) {
			if (!keySet[i].matches("[0-9]") && !flag) {
				String keySetField = ConstantsUtility.staticMap.get(keySet[i]) != null
						? ConstantsUtility.staticMap.get(keySet[i])
						: keySet[i];
				changeDescription.append(toHumanReadableFormat(keySetField));
				flag = true;
			} else if (keySet[i].matches("[0-9]")) {
				indexValue = Integer.parseInt(keySet[i]) + 1;
				at = " at index " + String.valueOf(indexValue);
				index = i;
			} else {
				String keySetField = (ConstantsUtility.staticMap.get(keySet[i]) != null
						? ConstantsUtility.staticMap.get(keySet[i])
						: keySet[i]);
				changeDescription.append(" of " + toHumanReadableFormat(keySetField));
			}
			if (StringUtils.hasText(at) && index != i) {
				changeDescription.append(at);
				at = null;
			}

		}
		if (!StringUtils.hasText(fromValue)) {
			changeDescription.append(" `" + toValue + "` added . ");
		} else if (!StringUtils.hasText(toValue)) {
			changeDescription.append(" `" + fromValue + "` removed . ");
		} else {
			changeDescription.append(" changed from `" + fromValue + "` to `" + toValue + "` .");
		}

		return changeDescription.toString();
	}

	/**
	 * maskDigitalValues masking digital value details based on permissions
	 * <p>
	 * creator can see the details
	 * <p>
	 * users having permission can see the details
	 * 
	 * @param solutionVOListVO
	 * @param userId
	 * @return SolutionCollection
	 */
	public SolutionCollection maskDigitalValues(List<SolutionVO> solutionVOListVO, String userId,
			boolean portfolioView) {
		SolutionCollection solutionCollection = new SolutionCollection();
		List<SolutionVO> records = new ArrayList<>();
		records = solutionVOListVO.stream().map(n -> this.maskDigitalValue(n, userId, portfolioView))
				.collect(Collectors.toList());
		solutionCollection.setRecords(records);
		return solutionCollection;
	}

	/**
	 * maskDigitalValue
	 * <p>
	 * masking digital value details based on permissions
	 * </p>
	 * <p>
	 * creator can see the details
	 * </p>
	 * <p>
	 * Team members can see the digital value details
	 * </p>
	 * 
	 * @param vo
	 * @param userId
	 * @param isAdmin
	 * @return SolutionVO
	 */
	public SolutionVO maskDigitalValue(SolutionVO vo, String userId, boolean portfolioView) {
		LOGGER.trace("Entering maskDigitalValue");
		if (vo != null && vo.getDigitalValue() != null) {
			LOGGER.debug("Checking if user is either team member or creator of the solution");
			boolean userLinkedToSolution = false;
			if (!ObjectUtils.isEmpty(vo.getDigitalValue().getPermissions())
					&& vo.getDigitalValue().getPermissions().stream().anyMatch(
							n -> (StringUtils.hasText(n.getShortId()) && n.getShortId().equalsIgnoreCase(userId)))) {
				userLinkedToSolution = true;
			} else if (!ObjectUtils.isEmpty(vo.getTeam()) && vo.getTeam().stream()
					.anyMatch(n -> (StringUtils.hasText(n.getShortId()) && n.getShortId().equalsIgnoreCase(userId)))) {
				userLinkedToSolution = true;
			} else if (!ObjectUtils.isEmpty(vo.getCreatedBy()) && StringUtils.hasText(vo.getCreatedBy().getId())
					&& vo.getCreatedBy().getId().equalsIgnoreCase(userId)) {
				userLinkedToSolution = true;
			}
			LOGGER.debug("Checking if user has permission to see digital value");
			if (!userLinkedToSolution && portfolioView) {
				LOGGER.debug("User has permission to view only old digital value");
				SolutionDigitalValueVO digitalValueVO = new SolutionDigitalValueVO();
				digitalValueVO.setDigitalValue(vo.getDigitalValue().getDigitalValue());
				ValueCalculatorVO valueCalculatorVO = new ValueCalculatorVO();
				CalculatedDigitalValueVO calculatedDigitalValueVO = (vo.getDigitalValue().getValueCalculator() != null
						&& vo.getDigitalValue().getValueCalculator().getCalculatedDigitalValue() != null)
								? vo.getDigitalValue().getValueCalculator().getCalculatedDigitalValue()
								: null;
				valueCalculatorVO.setCalculatedDigitalValue(calculatedDigitalValueVO);
				digitalValueVO.setValueCalculator(valueCalculatorVO);
				vo.setDigitalValue(digitalValueVO);
			} else if (!userLinkedToSolution && !portfolioView) {
				LOGGER.debug("User does not have permission to view digitalValue details");
				vo.setDigitalValue(null);
			}
		}
		return vo;
	}

	/**
	 * cloneDigitalValueVO
	 * <P>
	 * creating new copy of SolutionDigitalValueVO
	 * 
	 * @param vo
	 * @return SolutionDigitalValueVO
	 */
	public static SolutionDigitalValueVO cloneDigitalValueVO(SolutionDigitalValueVO vo) {
		SolutionDigitalValueVO solutionDigitalValueVO = new SolutionDigitalValueVO();
		BeanUtils.copyProperties(vo, solutionDigitalValueVO);
		solutionDigitalValueVO.setValueCalculator(new ValueCalculatorVO());
		// Cloning CostDrivers
		if (null != vo.getCostDrivers() && !vo.getCostDrivers().isEmpty()) {
			List<CostFactorVO> costDrivers = new ArrayList<CostFactorVO>();
			CostFactorVO costFactorVO = new CostFactorVO();
			for (CostFactorVO tempCostFactorVO : vo.getCostDrivers()) {
				costFactorVO = new CostFactorVO();
				BeanUtils.copyProperties(tempCostFactorVO, costFactorVO);
				if (null != tempCostFactorVO.getRampUp() && !tempCostFactorVO.getRampUp().isEmpty()) {
					List<CostRampUpYearVO> rampUps = new ArrayList<CostRampUpYearVO>();
					CostRampUpYearVO costRampUpYearVO = new CostRampUpYearVO();
					for (CostRampUpYearVO TempCostRampUpYearVO : tempCostFactorVO.getRampUp()) {
						costRampUpYearVO = new CostRampUpYearVO();
						BeanUtils.copyProperties(TempCostRampUpYearVO, costRampUpYearVO);
						rampUps.add(costRampUpYearVO);
					}
					costFactorVO.setRampUp(rampUps);
				}
				costDrivers.add(costFactorVO);
			}
			solutionDigitalValueVO.setCostDrivers(costDrivers);
		}
		// Cloning ValueDrivers
		if (null != vo.getValueDrivers() && !vo.getValueDrivers().isEmpty()) {
			List<ValueFactorVO> valueDrivers = new ArrayList<ValueFactorVO>();
			ValueFactorVO valueFactorVO = new ValueFactorVO();
			for (ValueFactorVO tempValueFactorVO : vo.getValueDrivers()) {
				valueFactorVO = new ValueFactorVO();
				BeanUtils.copyProperties(tempValueFactorVO, valueFactorVO);
				if (null != tempValueFactorVO.getRampUp() && !tempValueFactorVO.getRampUp().isEmpty()) {
					List<ValueRampUpYearVO> rampUps = new ArrayList<ValueRampUpYearVO>();
					ValueRampUpYearVO valueRampUpYearVO = new ValueRampUpYearVO();
					for (ValueRampUpYearVO tempValueRampUpYearVO : tempValueFactorVO.getRampUp()) {
						valueRampUpYearVO = new ValueRampUpYearVO();
						BeanUtils.copyProperties(tempValueRampUpYearVO, valueRampUpYearVO);
						rampUps.add(valueRampUpYearVO);
					}
					valueFactorVO.setRampUp(rampUps);
				}
				valueDrivers.add(valueFactorVO);
			}
			solutionDigitalValueVO.setValueDrivers(valueDrivers);
		}
		return solutionDigitalValueVO;
	}

}
