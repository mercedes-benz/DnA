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

package com.daimler.data.service.solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import com.daimler.data.db.jsonb.solution.*;
import com.daimler.data.dto.attachment.FileDetailsVO;
import com.daimler.data.dto.solution.*;
import com.daimler.data.service.attachment.AttachmentService;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.config.AVScannerClient;
import com.daimler.data.assembler.SolutionAssembler;
import com.daimler.data.client.dashboard.DashboardClient;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.DataikuNsql;
import com.daimler.data.db.entities.SolutionNsql;
import com.daimler.data.db.jsonb.SubDivision;
import com.daimler.data.db.repo.dataiku.DataikuCustomRepository;
import com.daimler.data.db.repo.solution.SolutionCustomRepository;
import com.daimler.data.db.repo.solution.SolutionRepository;
import com.daimler.data.dto.algorithm.AlgorithmVO;
import com.daimler.data.dto.datasource.DataSourceVO;
import com.daimler.data.dto.divisions.DivisionVO;
import com.daimler.data.dto.divisions.SubdivisionVO;
import com.daimler.data.dto.language.LanguageVO;
import com.daimler.data.dto.marketingRole.MarketingRoleVO;
import com.daimler.data.dto.notebook.NotebookVO;
import com.daimler.data.dto.platform.PlatformVO;
import com.daimler.data.dto.relatedProduct.RelatedProductVO;
import com.daimler.data.dto.skill.SkillVO;
import com.daimler.data.dto.tag.TagVO;
import com.daimler.data.dto.visualization.VisualizationVO;
import com.daimler.data.service.algorithm.AlgorithmService;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.dataiku.DataikuService;
import com.daimler.data.service.datasource.DataSourceService;
import com.daimler.data.service.language.LanguageService;
import com.daimler.data.service.marketingRoles.MarketingRoleService;
import com.daimler.data.service.notebook.NotebookService;
import com.daimler.data.service.platform.PlatformService;
import com.daimler.data.service.relatedproduct.RelatedProductService;
import com.daimler.data.service.skill.SkillService;
import com.daimler.data.service.tag.TagService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.service.visualization.VisualizationService;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@SuppressWarnings(value = "unused")
public class BaseSolutionService extends BaseCommonService<SolutionVO, SolutionNsql, String>
		implements SolutionService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseSolutionService.class);

	@Value("${dna.feature.jupyternotebook}")
	private boolean notebookAllowed;

	@Value("${dna.feature.dataiku}")
	private boolean dataikuAllowed;

	@Autowired
	private UserStore userStore;

	@Autowired
	private KafkaProducerService kafkaProducer;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private TagService tagService;
	@Autowired
	private DataSourceService dataSourceService;
	@Autowired
	private LanguageService languageService;
	@Autowired
	private AlgorithmService algorithmService;
	@Autowired
	private VisualizationService visualizationService;
	@Autowired
	private PlatformService platformService;
	@Autowired
	private RelatedProductService relatedProductService;

	@Autowired
	private SolutionCustomRepository customRepo;
	@Autowired
	private SolutionRepository jpaRepo;
	@Autowired
	private SolutionAssembler solutionAssembler;

	@Autowired
	private NotebookService notebookService;

	@Autowired
	private DataikuService dataikuService;
	@Autowired
	private DataikuCustomRepository dataikuCustomRepo;

	@Autowired
	private SkillService skillService;

	@Autowired
	private AVScannerClient aVScannerClient;
	
	@Autowired
	private DashboardClient dashboardClient;	
	
	@Autowired
	private MarketingRoleService marketingRoleService;

	@Autowired
	private AttachmentService attachmentService;

	public BaseSolutionService() {
		super();
	}

	@Override
	@Transactional
	public List<SolutionVO> getAllWithFilters(Boolean published, List<String> phases, List<String> dataVolumes,
			String division, List<String> locations, List<String> statuses, String solutionType, String userId,
			Boolean isAdmin, List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin, Boolean hasDigitalValue, Boolean hasNotebook, int offset, int limit,
			String sortBy, String sortOrder) {
		List<SolutionNsql> solutionEntities = customRepo.getAllWithFiltersUsingNativeQuery(published, phases,
				dataVolumes, division, locations, statuses, solutionType, userId, isAdmin, bookmarkedSolutions,
				searchTerms, tags, null, divisionsAdmin, hasDigitalValue, hasNotebook, offset, limit, sortBy,
				sortOrder);
		if (solutionEntities != null && !solutionEntities.isEmpty())
			return solutionEntities.stream().map(n -> solutionAssembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	@Transactional
	public Long getCount(Boolean published, List<String> phases, List<String> dataVolumes, String division,
			List<String> locations, List<String> statuses, String solutionType, String userId, Boolean isAdmin,
			List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags, List<String> divisionsAdmin,
			Boolean hasDigitalValue, Boolean hasNotebook) {
		return customRepo.getCountUsingNativeQuery(published, phases, dataVolumes, division, locations, statuses,
				solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags, divisionsAdmin, hasDigitalValue,
				hasNotebook);
	}

	@Override
	@Transactional
	public SolutionVO create(SolutionVO vo) {

		boolean isUpdate = vo != null && vo.getId() != null ? true : false;
		boolean noteBookAttachedAlready = false;
		String notebookEvent = "provisioned";
		SolutionVO prevVo = new SolutionVO();
		String prevDnaSubscriptionAppId = "";
		if (isUpdate) {
			String prevNotebookId = "";
			String currNotebookId = "";
			prevVo = this.getById(vo.getId());
			if (prevVo != null && prevVo.getPortfolio() != null
					&& StringUtils.hasText(prevVo.getPortfolio().getDnaSubscriptionAppId())) {
				prevDnaSubscriptionAppId = prevVo.getPortfolio().getDnaSubscriptionAppId();
			}
			if (prevVo != null && prevVo.getPortfolio() != null && prevVo.getPortfolio().getDnaNotebookId() != null)
				prevNotebookId = prevVo.getPortfolio().getDnaNotebookId();
			if (vo != null && vo.getPortfolio() != null && vo.getPortfolio().getDnaNotebookId() != null)
				currNotebookId = vo.getPortfolio().getDnaNotebookId();
			if (prevNotebookId != null && !"".equals(prevNotebookId)) {
				if (currNotebookId != null && currNotebookId.equalsIgnoreCase(prevNotebookId)) {
					notebookEvent = "";
				}
//    			else 
//    				notebookEvent = "unlink old + provisioned to new sol";
			}
			boolean found = false;
			String prevFileName, curFileName;
			List<FileDetailsVO> prevFileList = prevVo.getAttachments();
			String productName = prevVo.getProductName();
			List<FileDetailsVO> curFileList = vo.getAttachments();
			FileDetailsVO tempFile= null;
			try {
				if (prevFileList != null && !prevFileList.isEmpty()) {
					for (FileDetailsVO prevFile : prevFileList) {
						if (curFileList != null && !curFileList.isEmpty() && prevFile.getId() != null) {
							tempFile = curFileList.stream().filter(x -> prevFile.getId().equalsIgnoreCase(x.getId())).findAny().orElse(null);
						}
						if (tempFile == null) {
							try {
								attachmentService.deleteFileFromS3Bucket(prevFile.getId());
								log.info("Deleting unused attachment found after solution update");
							}catch (Exception e){
								log.error("Failed to delete attachment from solution with an exception {}", e.getMessage());
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("Empty attachments in an array with an exception {}", e.getMessage());
			}
		}
		updateTags(vo);
		updateDataSources(vo);
		updateRelatedProducts(vo);
					
		if(StringUtils.hasText(vo.getDepartment())) {
			LOGGER.info("Calling dashboardService to update departments {}", vo.getDepartment());
			dashboardClient.updateDepartments(vo);
		}
		
		
		LOGGER.debug("Updating Skills if not available.");
		updateSkills(vo);
		
		LOGGER.debug("Updating Roles if not available.");
		updateRoles(vo);
		
		SolutionAnalyticsVO analyticsVO = vo.getAnalytics();
		if (analyticsVO != null) {
			SolutionAnalyticsVO analyticsWithIdsInfo = new SolutionAnalyticsVO();
			List<AlgorithmVO> algorithms = analyticsVO.getAlgorithms();
			List<AlgorithmVO> algosWithIds = updateAlgorithms(algorithms);
			analyticsWithIdsInfo.setAlgorithms(algosWithIds);
			List<LanguageVO> languages = analyticsVO.getLanguages();
			List<LanguageVO> languagesWithIds = updateLanguages(languages);
			analyticsWithIdsInfo.setLanguages(languagesWithIds);
			List<VisualizationVO> visualizations = analyticsVO.getVisualizations();
			List<VisualizationVO> visualizationsWithIds = updateVisualization(visualizations);
			analyticsWithIdsInfo.setVisualizations(visualizationsWithIds);
			vo.setAnalytics(analyticsWithIdsInfo);
		}
		SolutionPortfolioVO portfolioVO = vo.getPortfolio();
		List<PlatformVO> platforms = new ArrayList<>();
		if (portfolioVO != null) {
			platforms = portfolioVO.getPlatforms();
			List<PlatformVO> platformsWithIds = updatePlatforms(platforms);
			portfolioVO.setPlatforms(platformsWithIds);
			vo.setPortfolio(portfolioVO);
		}
		SolutionVO responseSolutionVO = super.create(vo);
		if (notebookAllowed) {
			if (responseSolutionVO != null && responseSolutionVO.getId() != null && vo.getPortfolio() != null) {
				LOGGER.info("Updating Solution Id in DnA Notebook...");
				notebookService.updateSolutionIdofDnaNotebook(notebookEvent, vo.getPortfolio().getDnaNotebookId(),
						responseSolutionVO.getId());
			}
		}
		if (dataikuAllowed) {
			if (responseSolutionVO != null && responseSolutionVO.getId() != null && vo.getPortfolio() != null) {
				LOGGER.info("Updating Solution Id in DnA Dataiku...");
				dataikuService.updateSolutionIdOfDataIkuProjectId(vo.getPortfolio().getDnaDataikuProjectId(),
						responseSolutionVO.getId());
			}
		}

		if (responseSolutionVO != null && responseSolutionVO.getId() != null) {
			String currDnaSubscriptionAppId = "";
			if (responseSolutionVO.getPortfolio() != null
					&& StringUtils.hasText(responseSolutionVO.getPortfolio().getDnaSubscriptionAppId())) {
				currDnaSubscriptionAppId = responseSolutionVO.getPortfolio().getDnaSubscriptionAppId();
			}

			if (!currDnaSubscriptionAppId.equals(prevDnaSubscriptionAppId)) {
				LOGGER.info("Updating Solution Id in DnA Malware scan service subscription...");
				aVScannerClient.updateSolIdForSubscribedAppId(currDnaSubscriptionAppId, prevDnaSubscriptionAppId,
						responseSolutionVO.getId());
			}

		}
		String eventType = "";
		String solutionName = responseSolutionVO.getProductName();
		String solutionId = responseSolutionVO.getId();
		List<ChangeLogVO> changeLogs = new ArrayList<>();
		CreatedByVO currentUser = this.userStore.getVO();
		boolean isPublishedOrCreated = false;
		if (isUpdate) {
			eventType = "Solution_update";
			changeLogs = solutionAssembler.jsonObjectCompare(vo, prevVo, currentUser);
			if (vo.isPublish())
				isPublishedOrCreated = true;
		} else {
			eventType = "Solution_create";
			isPublishedOrCreated = true;
		}

		List<String> teamMembers = new ArrayList<>();
		List<String> teamMembersEmails = new ArrayList<>();
		for (TeamMemberVO user : responseSolutionVO.getTeam()) {
			teamMembers.add(user.getShortId());
			teamMembersEmails.add(user.getEmail());
		}
		if (isPublishedOrCreated) {
			LOGGER.debug("Publishing message on solution event for solution {} ", solutionName);
			this.publishEventMessages(eventType, solutionId, changeLogs, solutionName, teamMembers, teamMembersEmails);
		} else {
			LOGGER.debug(
					"Not publishing message on solution event for solution {} , as it is still in draft stage and not published",
					solutionName);
		}
		return responseSolutionVO;
	}

	
	

	@Transactional
	@Override
	public void deleteTagForEachSolution(String tagName, String relatedProductName, TAG_CATEGORY category) {

		CreatedByVO currentUser = this.userStore.getVO();
		TeamMemberVO ModifyingteamMemberVO = new TeamMemberVO();
		BeanUtils.copyProperties(currentUser, ModifyingteamMemberVO);
		ModifyingteamMemberVO.setShortId(currentUser.getId());
		String userId = currentUser != null ? currentUser.getId() : "dna_system";
		String userName = this.currentUserName(currentUser);

		List<SolutionNsql> notifyingSolutions = null;

		List<SolutionNsql> solutionNsqlList = null;
		if (!StringUtils.hasText(tagName) && StringUtils.hasText(relatedProductName)) {
			solutionNsqlList = customRepo.getAllWithFilters(null, null, null, null, null, null, null, null, true, null,
					null, null, Arrays.asList(relatedProductName), 0, 999999999, null, null);
		} else if (StringUtils.hasText(tagName) && !StringUtils.hasText(relatedProductName)) {
			solutionNsqlList = customRepo.getAllWithFilters(null, null, null, null, null, null, null, null, true, null,
					Arrays.asList(tagName), null, null, 0, 999999999, null, null);
		} else {
			solutionNsqlList = customRepo.getAllWithFilters(null, null, null, null, null, null, null, null, true, null,
					Arrays.asList(tagName), null, Arrays.asList(relatedProductName), 0, 999999999, null, null);
		}
		// solutionNsqlList = customRepo.getAllWithFilters(null, null, null, null, null,
		// null, null, null, true, null, Arrays.asList(tagName),
		// Arrays.asList(relatedProductName), null,0, 999999999,null,null);
		if (solutionNsqlList != null && !solutionNsqlList.isEmpty()) {
			notifyingSolutions = solutionNsqlList;
			solutionNsqlList.forEach(solutionNsql -> {

				String fieldName = "";
				String changeDescription = "";
				String message = "";
				List<String> teamMembers = new ArrayList<>();
				List<String> teamMembersEmails = new ArrayList<>();
				Solution solutionJson = solutionNsql.getData();
				String solutionName = solutionJson.getProductName();
				List<ChangeLogVO> changeLogs = new ArrayList<>();
				ChangeLogVO changeLog = new ChangeLogVO();
				changeLog.setChangeDate(new Date());
				changeLog.setModifiedBy(ModifyingteamMemberVO);
				changeLog.setNewValue(null);
				changeLog.setOldValue(tagName);
				List<SolutionTeamMember> solutionTeamMembers = solutionJson.getTeamMembers();
				for (SolutionTeamMember user : solutionTeamMembers) {
					teamMembers.add(user.getShortId());
					teamMembersEmails.add(user.getEmail());
				}

				if (category.equals(TAG_CATEGORY.TAG)) {
					changeLog.setChangeDescription("Tags: Tag '" + tagName + "' removed.");
					changeLog.setFieldChanged("/tags/");
					message = "Tag " + tagName + " has been deleted by Admin " + userName
							+ ". Cascading update to Solution " + solutionName
							+ " has been applied to remove references.";
					List<String> tags = solutionNsql.getData().getTags();
					if (tags != null && !tags.isEmpty()) {
						Iterator<String> itr = tags.iterator();
						while (itr.hasNext()) {
							String tag = itr.next();
							if (tag.equals(tagName)) {
								itr.remove();
								break;
							}
						}
						customRepo.update(solutionNsql);
					}
				} else if (category.equals(TAG_CATEGORY.DS)) {
					changeLog.setChangeDescription("Datasources: Datasource '" + tagName + "' removed.");
					changeLog.setFieldChanged("/dataSources/");
					message = "Datasource " + tagName + " has been deleted by Admin " + userName
							+ ". Cascading update to Solution " + solutionName
							+ " has been applied to remove references.";
					List<SolutionDatasource> dataSources = solutionNsql.getData().getDataSources();
					if (dataSources != null && !dataSources.isEmpty()) {
						Iterator<SolutionDatasource> itr = dataSources.iterator();
						while (itr.hasNext()) {
							SolutionDatasource ds = itr.next();
							if (ds.getName().equals(tagName)) {
								itr.remove();
								break;
							}
						}
						customRepo.update(solutionNsql);
					}
				} else if (category.equals(TAG_CATEGORY.LANG)) {
					changeLog.setChangeDescription("Languages: Language '" + tagName + "' removed.");
					changeLog.setFieldChanged("/languages/");
					message = "Language " + tagName + " has been deleted by Admin " + userName
							+ ". Cascading update to Solution " + solutionName
							+ " has been applied to remove references.";
					List<SolutionLanguage> languages = solutionNsql.getData().getLanguages();
					if (languages != null && !languages.isEmpty()) {
						Iterator<SolutionLanguage> itr = languages.iterator();
						while (itr.hasNext()) {
							SolutionLanguage ds = itr.next();
							if (ds.getName().equals(tagName)) {
								itr.remove();
								break;
							}
						}
						customRepo.update(solutionNsql);
					}
				} else if (category.equals(TAG_CATEGORY.ALGO)) {
					changeLog.setChangeDescription("Algorithms: Algorithm '" + tagName + "' removed.");
					changeLog.setFieldChanged("/algorithms/");
					message = "Algorithm " + tagName + " has been deleted by Admin " + userName
							+ ". Cascading update to Solution " + solutionName
							+ " has been applied to remove references.";
					List<SolutionAlgorithm> algorithms = solutionNsql.getData().getAlgorithms();
					if (algorithms != null && !algorithms.isEmpty()) {
						Iterator<SolutionAlgorithm> itr = algorithms.iterator();
						while (itr.hasNext()) {
							SolutionAlgorithm ds = itr.next();
							if (ds.getName().equals(tagName)) {
								itr.remove();
								break;
							}
						}
						customRepo.update(solutionNsql);
					}
				} else if (category.equals(TAG_CATEGORY.PLATFORM)) {
					changeLog.setChangeDescription("Platforms: Platform '" + tagName + "' removed.");
					changeLog.setFieldChanged("/platforms/");
					message = "Platform " + tagName + " has been deleted by Admin " + userName
							+ ". Cascading update to Solution " + solutionName
							+ " has been applied to remove references.";
					List<SolutionPlatform> platforms = solutionNsql.getData().getPlatforms();
					if (platforms != null && !platforms.isEmpty()) {
						Iterator<SolutionPlatform> itr = platforms.iterator();
						while (itr.hasNext()) {
							SolutionPlatform ds = itr.next();
							if (ds.getName().equals(tagName)) {
								itr.remove();
								break;
							}
						}
						customRepo.update(solutionNsql);
					}
				} else if (category.equals(TAG_CATEGORY.VISUALIZATION)) {
					changeLog.setChangeDescription("Visualizations: Visualization '" + tagName + "' removed.");
					changeLog.setFieldChanged("/visualizations/");
					message = "Visualization " + tagName + " has been deleted by Admin " + userName
							+ ". Cascading update to Solution " + solutionName
							+ " has been applied to remove references.";
					List<SolutionVisualization> visualizations = solutionNsql.getData().getVisualizations();
					if (visualizations != null && !visualizations.isEmpty()) {
						Iterator<SolutionVisualization> itr = visualizations.iterator();
						while (itr.hasNext()) {
							SolutionVisualization ds = itr.next();
							if (ds.getName().equals(tagName)) {
								itr.remove();
								break;
							}
						}
						customRepo.update(solutionNsql);
					}
				} else if (category.equals(TAG_CATEGORY.RELATEDPRODUCT)) {
					changeLog.setChangeDescription("RelatedProducts: RelatedProduct '" + tagName + "' removed.");
					changeLog.setFieldChanged("/relatedProducts/");
					message = "RelatedProduct " + tagName + " has been deleted by Admin " + userName
							+ ". Cascading update to Solution " + solutionName
							+ " has been applied to remove references.";
					List<String> relatedProducts = solutionNsql.getData().getRelatedProducts();
					if (relatedProducts != null && !relatedProducts.isEmpty()) {
						Iterator<String> itr = relatedProducts.iterator();
						while (itr.hasNext()) {
							String relatedProduct = itr.next();
							if (relatedProduct.equals(relatedProductName)) {
								itr.remove();
								break;
							}
						}
						customRepo.update(solutionNsql);
					}
				} else if (category.equals(TAG_CATEGORY.SKILL)) {
					changeLog.setChangeDescription("Skills: Skill '" + tagName + "' removed.");
					changeLog.setFieldChanged("/skills/");
					message = "Skill " + tagName + " has been deleted by Admin " + userName
							+ ". Cascading update to Solution " + solutionName
							+ " has been applied to remove references.";
					LOGGER.debug("Deleting Skill:{} from solutions.", tagName);
					if (!ObjectUtils.isEmpty(solutionNsql.getData().getSkills())) {
						List<SkillSummary> skills = solutionNsql.getData().getSkills().stream()
								.filter(x -> !x.getNeededSkill().equals(tagName)).collect(Collectors.toList());
						solutionNsql.getData().setSkills(skills);
						customRepo.update(solutionNsql);
					}
				} else if (category.equals(TAG_CATEGORY.MARKETINGROLE)) {
					changeLog.setChangeDescription("MArketing Roles: Marketing role '" + tagName + "' removed.");
					changeLog.setFieldChanged("/marketingRoles/");
					message = "MarketingRole " + tagName + " has been deleted by Admin " + userName
							+ ". Cascading update to Solution " + solutionName
							+ " has been applied to remove references.";
					LOGGER.info("Deleting MArketingRole:{} from solutions.", tagName);
					if (!ObjectUtils.isEmpty(solutionNsql.getData().getMarketingRoles())) {
						List<MarketingRoleSummary> marketingRoles = solutionNsql.getData().getMarketingRoles().stream()
								.filter(x -> !x.getRole().equals(tagName)).collect(Collectors.toList());
						solutionNsql.getData().setMarketingRoles(marketingRoles);
						customRepo.update(solutionNsql);
					}
				} else if (category.equals(TAG_CATEGORY.DIVISION)) {
					changeLog.setChangeDescription("Divisions: Division '" + tagName + "' removed.");
					changeLog.setFieldChanged("/divisions/");
					message = "Division " + tagName + " has been deleted by Admin " + userName
							+ ". Cascading update to Solution " + solutionName
							+ " has been applied to remove references.";
					LOGGER.debug("Deleting Division:{} from solutions.", tagName);
					SolutionDivision soldivision = solutionNsql.getData().getDivision();
					if (Objects.nonNull(soldivision) && StringUtils.hasText(soldivision.getId())
							&& soldivision.getName().equals(tagName)) {
						soldivision.setName(null);
						soldivision.setId(null);
						soldivision.setSubdivision(null);
						customRepo.update(solutionNsql);
					}
				} else if (category.equals(TAG_CATEGORY.DEPARTMENT)) {
					changeLog.setChangeDescription("Departments: Department '" + tagName + "' removed.");
					changeLog.setFieldChanged("/departments/");
					message = "Department " + tagName + " has been deleted by Admin " + userName
							+ ". Cascading update to Solution " + solutionName
							+ " has been applied to remove references.";
					LOGGER.debug("Deleting Department:{} from solutions.", tagName);
					String department = solutionNsql.getData().getDepartment();
					if (StringUtils.hasText(department) && department.equals(tagName)) {
						solutionNsql.getData().setDepartment(null);
						customRepo.update(solutionNsql);
					}					
				}
				changeLogs.add(changeLog);
				LOGGER.debug(
						"Publishing message on solution update event for solution {}, after admin action on {} and {}",
						solutionName, category, tagName);
				kafkaProducer.send("Solution Updated after Admin action", solutionNsql.getId(), "", userId, message,
						true, teamMembers, teamMembersEmails, changeLogs);
			});

		}
	}

	@Override
	@Transactional
	public void updateForEachSolution(String oldValue, String newValue, TAG_CATEGORY category, Object updateObject) {
		List<SolutionNsql> solutionNsqlList = null;
		String eventType = "Solution_update";		
		CreatedByVO currentUser = this.userStore.getVO();
		TeamMemberVO ModifyingteamMemberVO = new TeamMemberVO();
		BeanUtils.copyProperties(currentUser, ModifyingteamMemberVO);
		ModifyingteamMemberVO.setShortId(currentUser.getId());		
		String userId = currentUser != null ? currentUser.getId() : "dna_system";
		String userName = this.currentUserName(currentUser);
		ChangeLogVO changeLog = new ChangeLogVO();
		changeLog.setChangeDate(new Date());
		changeLog.setModifiedBy(ModifyingteamMemberVO);
		changeLog.setNewValue(null);
		changeLog.setOldValue(oldValue);
		if (StringUtils.hasText(oldValue)) {
			solutionNsqlList = customRepo.getAllWithFilters(null, null, null, null, null, null, null, null, true, null,
					Arrays.asList(oldValue), null, null, 0, 999999999, null, null);
		}
		if (!ObjectUtils.isEmpty(solutionNsqlList)) {
			solutionNsqlList.forEach(solutionNsql -> {				
				List<String> teamMembers = new ArrayList<>();
				List<String> teamMembersEmails = new ArrayList<>();
				Solution solutionJson = solutionNsql.getData();
				List<ChangeLogVO> changeLogs = new ArrayList<>();						
				List<SolutionTeamMember> solutionTeamMembers = solutionJson.getTeamMembers();
				for (SolutionTeamMember user : solutionTeamMembers) {
					teamMembers.add(user.getShortId());
					teamMembersEmails.add(user.getEmail());
				}
				if (category.equals(TAG_CATEGORY.DIVISION)) {					
					SolutionDivision soldivision = solutionNsql.getData().getDivision();
					DivisionVO divisionVO = (DivisionVO) updateObject;
					changeLog.setChangeDescription("Divisions: Division '" + divisionVO.getName() + "' updated.");
					changeLog.setFieldChanged("/divisions/");
					changeLogs.add(changeLog);
					if (Objects.nonNull(soldivision) && StringUtils.hasText(soldivision.getId())
							&& soldivision.getId().equals(divisionVO.getId())) {
						soldivision.setName(divisionVO.getName().toUpperCase());
						SubDivision subdivision = soldivision.getSubdivision();
						List<SubdivisionVO> subdivisionlist = divisionVO.getSubdivisions();
						if (Objects.nonNull(subdivision)) {
							if (ObjectUtils.isEmpty(subdivisionlist)) {
								soldivision.setSubdivision(null);
							} else {
								boolean exists = false;
								for (SubdivisionVO value : subdivisionlist) {
									if (StringUtils.hasText(value.getId())
											&& value.getId().equals(subdivision.getId())) {
										SubDivision subdiv = new SubDivision();
										subdiv.setId(value.getId());
										subdiv.setName(value.getName().toUpperCase());
										soldivision.setSubdivision(subdiv);
										exists = true;
										break;
									}
								}
								if (!exists) {
									soldivision.setSubdivision(null);
								}
							}
						}
						customRepo.update(solutionNsql);
					}
				}
				log.info("Solution Updated after Admin action");	
				this.publishEventMessages(eventType, solutionNsql.getId(), changeLogs, 
						solutionNsql.getData().getProductName(), teamMembers, teamMembersEmails);
			});
		}
	}

	@Override
	public void deleteInActiveSolutionsOlderThan(Calendar startDate) {

		log.info("Deleting solutions that are In-Active and are less than or equal to:" + startDate.toString());

		List<SolutionNsql> solutionNsqlList = customRepo.getAllWithFilters(null, null, null, null, null,
				Arrays.asList("5"), null, null, true, null, null, null, null, 0, 999999999, null, null);
		// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd")
		log.info("DateToCompare:" + startDate.get(Calendar.DAY_OF_MONTH) + "," + startDate.get(Calendar.MONTH) + ","
				+ startDate.get(Calendar.YEAR));
		if (!solutionNsqlList.isEmpty()) {
			solutionNsqlList.forEach(solutionNsql -> {
				String id = solutionNsql.getId();
				Date closeDate = solutionNsql.getData().getCloseDate();
				if (closeDate != null) {
					Calendar solCloseDate = Calendar.getInstance();
					solCloseDate.setTime(closeDate);
					log.info("SolutionInActiveDate:" + solCloseDate.get(Calendar.DAY_OF_MONTH) + ","
							+ solCloseDate.get(Calendar.MONTH) + "," + solCloseDate.get(Calendar.YEAR));
					if (DateUtils.isSameDay(startDate, solCloseDate) || solCloseDate.before(startDate)) {
						log.info(
								"Deleting Inactive Solution as its older than the required idle period, Solution data is:"
										+ solutionNsql.toString());
						this.deleteById(id);
					}

				}
			});
		}

	}

	private void updateTags(SolutionVO vo) {
		List<String> tags = vo.getTags();
		if (tags != null && !tags.isEmpty()) {
			tags.forEach(tag -> {
				TagVO existingTagVO = tagService.getByUniqueliteral("name", tag);
				if (existingTagVO != null && existingTagVO.getName() != null
						&& existingTagVO.getName().equalsIgnoreCase(tag))
					return;
				else {
					TagVO newTagVO = new TagVO();
					newTagVO.setId(null);
					newTagVO.setName(tag);
					tagService.create(newTagVO);
				}
			});
		}
	}

	/*
	 * To create a new skills if not exist
	 * 
	 */
	private void updateSkills(SolutionVO vo) {
		List<String> skills = vo.getSkills() != null ? vo.getSkills().stream().filter(Objects::nonNull)
				.map(n -> n.getNeededSkill()).collect(Collectors.toList()) : null;
		if (!ObjectUtils.isEmpty(skills)) {
			skills.forEach(skill -> {
				LOGGER.debug("Checking if Skill:{} already existing", skill);
				SkillVO existingSkillVO = skillService.getByUniqueliteral("name", skill);
				if (existingSkillVO != null && existingSkillVO.getName() != null
						&& existingSkillVO.getName().equalsIgnoreCase(skill))
					return;
				else {
					LOGGER.debug("Adding new Skill:{} in db", skill);
					SkillVO newSkillVO = new SkillVO();
					newSkillVO.setId(null);
					newSkillVO.setName(skill);
					skillService.create(newSkillVO);
				}
			});
		}
	}
	
	/*
	 * To create a new Roles if not exist
	 * 
	 */
	private void updateRoles(SolutionVO vo) {		
		List<String> roles = vo.getMarketing().getMarketingRoles() != null ? vo.getMarketing().getMarketingRoles().stream().filter(Objects :: nonNull)
				.map(n->n.getRole()).collect(Collectors.toList()) : null;
		if (!ObjectUtils.isEmpty(roles)) {
			roles.forEach(role -> {
				LOGGER.info("Checking if Role:{} already exists", role);
				MarketingRoleVO existingRoleVO = marketingRoleService.getByUniqueliteral("name", role);
				if (existingRoleVO != null && existingRoleVO.getName() != null
						&& existingRoleVO.getName().equalsIgnoreCase(role))
					return;
				else {
					LOGGER.info("Adding new Role:{} in db", role);
					MarketingRoleVO newRoleVO = new MarketingRoleVO();
					newRoleVO.setId(null);
					newRoleVO.setName(role);					
					marketingRoleService.create(newRoleVO);
				}
			});
		}
	}

	private List<AlgorithmVO> updateAlgorithms(List<AlgorithmVO> algorithms) {
		List<AlgorithmVO> algoWithIds = new ArrayList<>();
		List<AlgorithmVO> existingAlgos = algorithmService.getAll();
		boolean algorithmsExists = false;
		if (existingAlgos != null && !existingAlgos.isEmpty())
			algorithmsExists = true;
		final boolean algorithmsExistsFinal = algorithmsExists;
		if (algorithms != null && !algorithms.isEmpty()) {
			algoWithIds = algorithms.stream().map(algorithm -> {
				if (algorithmsExistsFinal) {
					AlgorithmVO existingAlgoWithId = existingAlgos.stream()
							.filter(n -> algorithm.getName().equalsIgnoreCase(n.getName())).findAny().orElse(null);
					if (existingAlgoWithId != null)
						return existingAlgoWithId;
				}
				AlgorithmVO newAlgoVO = new AlgorithmVO();
				newAlgoVO.setId(null);
				newAlgoVO.setName(algorithm.getName());
				newAlgoVO = algorithmService.create(newAlgoVO);
				return newAlgoVO;
			}).collect(Collectors.toList());
		}
		return algoWithIds;
	}

	private List<LanguageVO> updateLanguages(List<LanguageVO> languages) {
		List<LanguageVO> languagesWithIds = new ArrayList<>();
		List<LanguageVO> existingLanguages = languageService.getAll();
		boolean languagesExists = false;
		if (existingLanguages != null && !existingLanguages.isEmpty())
			languagesExists = true;
		final boolean languagesExistsFinal = languagesExists;
		if (languages != null && !languages.isEmpty()) {
			languagesWithIds = languages.stream().map(language -> {
				if (languagesExistsFinal) {
					LanguageVO existingLanguageWithId = existingLanguages.stream()
							.filter(n -> language.getName().equalsIgnoreCase(n.getName())).findAny().orElse(null);
					if (existingLanguageWithId != null)
						return existingLanguageWithId;
				}
				LanguageVO newLanguageVO = new LanguageVO();
				newLanguageVO.setId(null);
				newLanguageVO.setName(language.getName());
				newLanguageVO = languageService.create(newLanguageVO);
				return newLanguageVO;
			}).collect(Collectors.toList());
		}
		return languagesWithIds;
	}

	private List<VisualizationVO> updateVisualization(List<VisualizationVO> visualizations) {
		List<VisualizationVO> visualizationsWithIds = new ArrayList<>();
		List<VisualizationVO> existingVisualizations = visualizationService.getAll();
		boolean visualizationsExists = false;
		if (existingVisualizations != null && !existingVisualizations.isEmpty())
			visualizationsExists = true;
		final boolean visualizationsExistsFinal = visualizationsExists;
		if (visualizations != null && !visualizations.isEmpty()) {
			visualizationsWithIds = visualizations.stream().map(visualization -> {
				if (visualizationsExistsFinal) {
					VisualizationVO existingVisualizationWithId = existingVisualizations.stream()
							.filter(n -> visualization.getName().equalsIgnoreCase(n.getName())).findAny().orElse(null);
					if (existingVisualizationWithId != null)
						return existingVisualizationWithId;
				}
				VisualizationVO newVisualizationVO = new VisualizationVO();
				newVisualizationVO.setId(null);
				newVisualizationVO.setName(visualization.getName());
				newVisualizationVO = visualizationService.create(newVisualizationVO);
				return newVisualizationVO;
			}).collect(Collectors.toList());
		}
		return visualizationsWithIds;
	}

	private List<PlatformVO> updatePlatforms(List<PlatformVO> platforms) {
		List<PlatformVO> platformsWithIds = new ArrayList<>();
		List<PlatformVO> existingPlatforms = platformService.getAll();
		boolean platformsExists = false;
		if (existingPlatforms != null && !existingPlatforms.isEmpty())
			platformsExists = true;
		final boolean platformsExistsFinal = platformsExists;
		if (platforms != null && !platforms.isEmpty()) {
			platformsWithIds = platforms.stream().map(platform -> {
				if (platformsExistsFinal) {
					PlatformVO existingPlatformWithId = existingPlatforms.stream()
							.filter(n -> platform.getName().equalsIgnoreCase(n.getName())).findAny().orElse(null);
					if (existingPlatformWithId != null)
						return existingPlatformWithId;
				}
				PlatformVO newPlatformVO = new PlatformVO();
				newPlatformVO.setId(null);
				newPlatformVO.setName(platform.getName());
				newPlatformVO = platformService.create(newPlatformVO);
				return newPlatformVO;
			}).collect(Collectors.toList());
		}
		return platformsWithIds;
	}

	private void updateDataSources(SolutionVO vo) {
		if (vo.getDataSources() != null) {
			List<DataSourceSummaryVO> dataSources = vo.getDataSources().getDataSources();
			Optional.ofNullable(dataSources).ifPresent(l -> l.forEach(dataSourceSummaryVO -> {
				DataSourceVO dataSourceVO = dataSourceService.getByUniqueliteral("name",
						dataSourceSummaryVO.getDataSource());
				if (dataSourceVO != null && dataSourceVO.getName() != null
						&& dataSourceVO.getName().equalsIgnoreCase(dataSourceSummaryVO.getDataSource())) {
					return;
				} else {
					DataSourceVO newDataSourceVO = new DataSourceVO();
					newDataSourceVO.setName(dataSourceSummaryVO.getDataSource());
					dataSourceService.create(newDataSourceVO);
				}
			}));
		}
	}

	private void updateRelatedProducts(SolutionVO vo) {
		List<String> relatedProducts = vo.getRelatedProducts();
		if (relatedProducts != null && !relatedProducts.isEmpty()) {
			relatedProducts.forEach(relatedProduct -> {
				RelatedProductVO existingRpVO = relatedProductService.getByUniqueliteral("name", relatedProduct);
				if (existingRpVO != null && existingRpVO.getName() != null
						&& existingRpVO.getName().equalsIgnoreCase(relatedProduct))
					return;
				else {
					RelatedProductVO newRpVO = new RelatedProductVO();
					newRpVO.setId(null);
					newRpVO.setName(relatedProduct);
					relatedProductService.create(newRpVO);
				}
			});
		}
	}

	/**
	 * getChangeLogsById Fetch change logs for solution of given id
	 * 
	 * @param id
	 * @return List<ChangeLogVO>
	 */
	@Override
	public List<ChangeLogVO> getChangeLogsBySolutionId(String id) {
		SolutionVO solutionVO = this.getById(id);
		if (null != solutionVO && null != solutionVO.getDigitalValue()
				&& null != solutionVO.getDigitalValue().getChangeLogs()) {
			return solutionVO.getDigitalValue().getChangeLogs();
		} else {
			return new ArrayList<ChangeLogVO>();
		}

	}

	@Override
	@Transactional
	public boolean deleteById(String id) {
		SolutionVO solutionVO = this.getById(id);
		if (notebookAllowed) {
			LOGGER.info("Updating Notebook linkage.");
			NotebookVO availableNotebook = notebookService.getByUniqueliteral("solutionId", id);
			if (availableNotebook != null) {
				notebookService.updateSolutionIdofDnaNotebook("unlink", availableNotebook.getId(), id);
			}
		}

		if (solutionVO != null && solutionVO.getId() != null && solutionVO.getPortfolio() != null
				&& StringUtils.hasText(solutionVO.getPortfolio().getDnaSubscriptionAppId())) {
			LOGGER.info("Updating malware scan subscription linkage.");
			aVScannerClient.updateSolIdForSubscribedAppId(solutionVO.getPortfolio().getDnaSubscriptionAppId(), "",
					null);
		}

		if (dataikuAllowed) {
			LOGGER.info("Updating Dataiku linkage.");
			DataikuNsql dataikuEntity = dataikuCustomRepo.findbyUniqueLiteral("solutionId", id);
			if (dataikuEntity != null && dataikuEntity.getData() != null) {
				dataikuService.updateSolutionIdOfDataIkuProjectId(dataikuEntity.getData().getProjectKey(), null);
			}
		}

		if (solutionVO != null && solutionVO.getId() != null) {
			String eventType = "Solution_delete";
			String solutionName = solutionVO.getProductName();
			String solutionId = solutionVO.getId();
			List<String> teamMembers = new ArrayList<>();
			List<String> teamMembersEmails = new ArrayList<>();
			for (TeamMemberVO user : solutionVO.getTeam()) {
				teamMembers.add(user.getShortId());
				teamMembersEmails.add(user.getEmail());
			}
			this.publishEventMessages(eventType, solutionId, null, solutionName, teamMembers, teamMembersEmails);
		}

		return super.deleteById(id);
	}

	private void publishEventMessages(String eventType, String solutionId, List<ChangeLogVO> changeLogs,
			String solutionName, List<String> subscribedUsers, List<String> subscribedUsersEmail) {
		try {
			String message = "";
			Boolean mailRequired = true;
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "dna_system";
			String userName = super.currentUserName(currentUser);

			/*
			 * if(subscribedUsers!=null && !subscribedUsers.isEmpty() &&
			 * subscribedUsers.contains(userId)) {
			 * LOGGER.info("Removed current userid from subscribedUsers");
			 * subscribedUsers.remove(userId); }
			 */

			if ("Solution_delete".equalsIgnoreCase(eventType)) {
				eventType = "Solution Deleted";
				message = "Solution " + solutionName + " is deleted by user " + userName;
				LOGGER.info("Publishing message on solution delete for solution {} by userId {}", solutionName, userId);
			}
			if ("Solution_update".equalsIgnoreCase(eventType)) {
				eventType = "Solution Updated";
				message = "Solution " + solutionName + " is updated by user " + userName;
				LOGGER.info("Publishing message on solution update for solution {} by userId {}", solutionName, userId);
			}
			if ("Solution_create".equalsIgnoreCase(eventType)) {
				eventType = "Solution Created";
				message = "Added as team member to Solution " + solutionName + " by user " + userName;
				LOGGER.info("Publishing message on solution create for solution {} by userId {}", solutionName, userId);
			}
			if (eventType != null && eventType != "") {
				kafkaProducer.send(eventType, solutionId, "", userId, message, mailRequired, subscribedUsers,
						subscribedUsersEmail, changeLogs);
			}
		} catch (Exception e) {
			LOGGER.trace("Failed while publishing solution event msg {} ", e.getMessage());
		}
	}

	@Override
	public ResponseEntity<GenericMessage> malwareScanUnsubscribe(String solutionId) {
		try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			boolean isAdmin = userInfoService.isAdmin(userId);
			boolean isOwner = false;
			SolutionVO solutionVO = this.getById(solutionId);
			if (StringUtils.hasText(userId)) {
				String createdBy = solutionVO.getCreatedBy() != null ? solutionVO.getCreatedBy().getId() : null;
				isOwner = (createdBy != null && createdBy.equals(userId));
			}
			if (!isAdmin && !isOwner) {
				LOGGER.debug("User {} not authorized to unsubscribe malware scan service", userId);
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to unsubscribe malware scan. Only solution owner or an admin can unsubscribe");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
			if (solutionVO != null && solutionVO.getId() != null && solutionVO.getPortfolio() != null) {
				solutionVO.getPortfolio().setDnaSubscriptionAppId(null);
				this.create(solutionVO);
			}
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("success");
			LOGGER.info("Solution {} unsubscribed successfully", solutionId);
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			MessageDescription invalidMsg = new MessageDescription("No Solution with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			LOGGER.error("No Solution with the given id {} , couldnt unsubscribe.", solutionId);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to unsubscribe due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOGGER.error("Failed to unsubscribe malware scan for solution with id {} , due to internal error.",
					solutionId);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Integer getCountBasedPublishSolution(Boolean published) {
		return customRepo.getCountBasedPublishSolution(published);
	}

}
