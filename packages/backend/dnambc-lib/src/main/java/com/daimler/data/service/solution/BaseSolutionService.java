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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.SolutionAssembler;
import com.daimler.data.db.entities.DataikuNsql;
import com.daimler.data.db.entities.SolutionNsql;
import com.daimler.data.db.jsonb.SubDivision;
import com.daimler.data.db.jsonb.solution.SkillSummary;
import com.daimler.data.db.jsonb.solution.SolutionAlgorithm;
import com.daimler.data.db.jsonb.solution.SolutionDatasource;
import com.daimler.data.db.jsonb.solution.SolutionDivision;
import com.daimler.data.db.jsonb.solution.SolutionLanguage;
import com.daimler.data.db.jsonb.solution.SolutionPlatform;
import com.daimler.data.db.jsonb.solution.SolutionVisualization;
import com.daimler.data.db.repo.dataiku.DataikuCustomRepository;
import com.daimler.data.db.repo.solution.SolutionCustomRepository;
import com.daimler.data.db.repo.solution.SolutionRepository;
import com.daimler.data.dto.algorithm.AlgorithmVO;
import com.daimler.data.dto.appsubscription.SubscriptionVO;
import com.daimler.data.dto.datasource.DataSourceVO;
import com.daimler.data.dto.divisions.DivisionVO;
import com.daimler.data.dto.divisions.SubdivisionVO;
import com.daimler.data.dto.language.LanguageVO;
import com.daimler.data.dto.notebook.NotebookVO;
import com.daimler.data.dto.platform.PlatformVO;
import com.daimler.data.dto.relatedProduct.RelatedProductVO;
import com.daimler.data.dto.skill.SkillVO;
import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.solution.SolutionAnalyticsVO;
import com.daimler.data.dto.solution.SolutionPortfolioVO;
import com.daimler.data.dto.solution.SolutionVO;
import com.daimler.data.dto.solution.TeamMemberVO;
import com.daimler.data.dto.tag.TagVO;
import com.daimler.data.dto.visualization.VisualizationVO;
import com.daimler.data.service.algorithm.AlgorithmService;
import com.daimler.data.service.appsubscription.AppSubscriptionService;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.dataiku.DataikuService;
import com.daimler.data.service.datasource.DataSourceService;
import com.daimler.data.service.language.LanguageService;
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
	private AppSubscriptionService appSubscriptionService;

	@Autowired
	private SkillService skillService;

	public BaseSolutionService() {
		super();
	}

	@Override
	@Transactional
	public List<SolutionVO> getAllWithFilters(Boolean published, List<String> phases, List<String> dataVolumes,
			List<Map<String, List<String>>> divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags, int offset, int limit, String sortBy, String sortOrder) {
		List<SolutionNsql> solutionEntities = customRepo.getAllWithFilters(published, phases, dataVolumes, divisions,
				locations, statuses, solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags, null,
				offset, limit, sortBy, sortOrder);
		if (solutionEntities != null && !solutionEntities.isEmpty())
			return solutionEntities.stream().map(n -> solutionAssembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	@Transactional
	public Long getCount(Boolean published, List<String> phases, List<String> dataVolumes,
			List<Map<String, List<String>>> divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags) {
		return customRepo.getCount(published, phases, dataVolumes, divisions, locations, statuses, solutionType, userId,
				isAdmin, bookmarkedSolutions, searchTerms, tags);
	}

	@Override
	@Transactional
	public SolutionVO create(SolutionVO vo) {

		boolean isUpdate = vo != null && vo.getId() != null ? true : false;
		boolean noteBookAttachedAlready = false;
		String notebookEvent = "provisioned";
		SolutionVO prevVo = new SolutionVO();
		if (isUpdate) {
			String prevNotebookId = "";
			String currNotebookId = "";
			prevVo = this.getById(vo.getId());
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
		}
		updateTags(vo);
		updateDataSources(vo);
		updateRelatedProducts(vo);
		LOGGER.debug("Updating Skills if not available.");
		updateSkills(vo);
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

		if (responseSolutionVO != null && responseSolutionVO.getId() != null && vo.getPortfolio() != null
		// && StringUtils.isNotEmpty(vo.getPortfolio().getDnaSubscriptionAppId())
		) {
			LOGGER.info("Updating Solution Id in DnA Malware scan service subscription...");
			appSubscriptionService.updateSolIdForSubscribedAppId(vo.getPortfolio().getDnaSubscriptionAppId(),
					responseSolutionVO.getId());
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
			solutionNsqlList.forEach(solutionNsql -> {
				if (category.equals(TAG_CATEGORY.TAG)) {
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
					LOGGER.debug("Deleting Skill:{} from solutions.", tagName);
					if (!ObjectUtils.isEmpty(solutionNsql.getData().getSkills())) {
						List<SkillSummary> skills = solutionNsql.getData().getSkills().stream()
								.filter(x -> !x.getNeededSkill().equals(tagName)).collect(Collectors.toList());
						solutionNsql.getData().setSkills(skills);
						customRepo.update(solutionNsql);
					}
				} else if (category.equals(TAG_CATEGORY.DIVISION)) {
					LOGGER.debug("Deleting Division:{} from solutions.", tagName);
					SolutionDivision soldivision = solutionNsql.getData().getDivision();
					if (Objects.nonNull(soldivision) && StringUtils.hasText(soldivision.getId())
							&& soldivision.getId().equals(tagName)) {
						soldivision.setName(null);
						soldivision.setId(null);
						soldivision.setSubdivision(null);
						customRepo.update(solutionNsql);
					}
				}

			});
		}
	}

	@Override
	@Transactional
	public void updateForEachSolution(String oldValue, String newValue, TAG_CATEGORY category, Object updateObject) {
		List<SolutionNsql> solutionNsqlList = null;
		if (StringUtils.hasText(oldValue)) {
			solutionNsqlList = customRepo.getAllWithFilters(null, null, null, null, null, null, null, null, true, null,
					Arrays.asList(oldValue), null, null, 0, 999999999, null, null);
		}
		if (!ObjectUtils.isEmpty(solutionNsqlList)) {
			solutionNsqlList.forEach(solutionNsql -> {
				if (category.equals(TAG_CATEGORY.DIVISION)) {
					SolutionDivision soldivision = solutionNsql.getData().getDivision();
					DivisionVO divisionVO = (DivisionVO) updateObject;
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
			List<String> dataSources = vo.getDataSources().getDataSources();
			if (dataSources != null) {
				dataSources.forEach(dataSource -> {
					DataSourceVO dataSourceVO = dataSourceService.getByUniqueliteral("name", dataSource);
					if (dataSourceVO != null && dataSourceVO.getName() != null
							&& dataSourceVO.getName().equalsIgnoreCase(dataSource)) {
						return;
					} else {
						DataSourceVO newDataSourceVO = new DataSourceVO();
						newDataSourceVO.setName(dataSource);
						dataSourceService.create(newDataSourceVO);
					}
				});
			}

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
		LOGGER.trace("Entering deleteById.");
		SolutionVO solutionVO = this.getById(id);
		if (notebookAllowed) {
			LOGGER.info("Updating Notebook linkage.");
			NotebookVO availableNotebook = notebookService.getByUniqueliteral("solutionId", id);
			if (availableNotebook != null) {
				notebookService.updateSolutionIdofDnaNotebook("unlink", availableNotebook.getId(), null);
			}
		}

		SubscriptionVO availableSubscriptionVO = appSubscriptionService.getByUniqueliteral("solutionId", id);
		if (availableSubscriptionVO != null) {
			appSubscriptionService.updateSolIdForSubscribedAppId(availableSubscriptionVO.getAppId(), null);
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
				message = "Solution " + solutionName + " is delete by user " + userName;
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

}
