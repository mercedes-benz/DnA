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
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.daimler.data.db.jsonb.*;
import com.daimler.data.db.jsonb.dataproduct.*;
import com.daimler.data.dto.dataproduct.*;
import com.daimler.data.dto.dataproduct.ChangeLogVO;
import com.daimler.data.dto.dataproduct.DivisionVO;
import com.daimler.data.dto.dataproduct.SubdivisionVO;
import com.daimler.data.dto.dataproduct.TeamMemberVO;
import com.daimler.data.dto.datatransfer.*;
import com.daimler.data.dto.datatransfer.ConsumerResponseVO;
import com.daimler.data.dto.datatransfer.DataTransferConsumerRequestVO;
import com.daimler.data.dto.tag.TagVO;

import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.DataProductNsql;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.dataproduct.DataProductTeamMemberVO.UserTypeEnum;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class DataProductAssembler implements GenericAssembler<DataProductVO, DataProductNsql> {

	public DataProductTeamMemberVO toParseDataProductTeamMemberVO(String value) {
		DataProductTeamMemberVO vo = null;

		if(value != null) {
			JSONObject jsonObject = new JSONObject(value);
			vo = new DataProductTeamMemberVO();
			vo.setFirstName((String) jsonObject.get("firstName"));
			vo.setLastName((String) jsonObject.get("lastName"));
			vo.setDepartment((String) jsonObject.get("department"));
			vo.setMobileNumber((String) jsonObject.get("mobileNumber"));
			vo.setShortId((String) jsonObject.get("id"));
			vo.setEmail((String) jsonObject.get("email"));
		}
		return vo;
	}

	@Override
	public DataProductVO toVo(DataProductNsql entity) {
		DataProductVO vo = null;
		if (entity != null && entity.getData() != null) {
			vo = new DataProductVO();
			vo.setId(entity.getId());
			DataProduct dataProduct = entity.getData();
			if (dataProduct != null) {
				BeanUtils.copyProperties(dataProduct, vo);
				vo.setIsPublish(entity.getData().getPublish());
				if(Objects.nonNull(dataProduct.getAdditionalInformation())) {
					vo.setAdditionalInformation(dataProduct.getAdditionalInformation());
				}
				if (Objects.nonNull(dataProduct.getCreatedBy())) {
					CreatedByVO createdByVO = new CreatedByVO();
					BeanUtils.copyProperties(dataProduct.getCreatedBy(), createdByVO);
					vo.setCreatedBy(createdByVO);
				}
				if (Objects.nonNull(dataProduct.getModifiedBy())) {
					CreatedByVO updatedByVO = new CreatedByVO();
					BeanUtils.copyProperties(dataProduct.getModifiedBy(), updatedByVO);
					vo.setModifiedBy(updatedByVO);
				}
				if (Objects.nonNull(dataProduct.getCarLaFunction())) {
					CarLaFunctionVO carLaFunctionVO = new CarLaFunctionVO();
					BeanUtils.copyProperties(dataProduct.getCarLaFunction(), carLaFunctionVO);
					vo.setCarLaFunction(carLaFunctionVO);
				}

				List<PlatformVO> platforms = dataProduct.getPlatform();
				List<PlatformVO> platformsVO = new ArrayList<>();
				if (platforms != null && !platforms.isEmpty()) {
					for (PlatformVO platform : platforms) {
						if (platform != null) {
							PlatformVO platformVO = new PlatformVO();
							BeanUtils.copyProperties(platform, platformVO);
							platformsVO.add(platformVO);
						}
					}
					vo.setPlatform(platformsVO);
				}

				List<FrontendToolsVO> frontEndTools = dataProduct.getFrontEndTools();
				List<FrontendToolsVO> frontEndToolsVo = new ArrayList<>();
				if (frontEndTools != null && !frontEndTools.isEmpty()) {
					for (FrontendToolsVO frontEndTool : frontEndTools) {
						if (frontEndTool != null) {
							FrontendToolsVO frontEndToolVo = new FrontendToolsVO();
							BeanUtils.copyProperties(frontEndTool, frontEndToolVo);
							frontEndToolsVo.add(frontEndToolVo);
						}
					}
					vo.setFrontEndTools(frontEndToolsVo);
				}
				
				if (dataProduct.getTags() != null && !ObjectUtils.isEmpty(dataProduct.getTags())) {
					BeanUtils.copyProperties(dataProduct.getTags(), vo.getTags());
				}

				if (Objects.nonNull(dataProduct.getAgileReleaseTrain())) {
					AgileReleaseTrainVO agileReleaseTrain = new AgileReleaseTrainVO();
					BeanUtils.copyProperties(dataProduct.getAgileReleaseTrain(), agileReleaseTrain);
					vo.setAgileReleaseTrain(agileReleaseTrain);
				}
				if (Objects.nonNull(dataProduct.getCorporateDataCatalog())) {
					CorporateDataCatalogVO corporateDataCatalog = new CorporateDataCatalogVO();
					BeanUtils.copyProperties(dataProduct.getCorporateDataCatalog(), corporateDataCatalog);
					vo.setCorporateDataCatalog(corporateDataCatalog);
				}
				
				if(Objects.nonNull(dataProduct.getAccess())) {
					List<String> accesstypes = dataProduct.getAccess().getAccessType();
					if(accesstypes != null && accesstypes.size()>0 ) {
						if(accesstypes.contains("Kafka") || accesstypes.contains("API")) {
							if(vo.getAccess().getConfidentiality().equals("Internal")) {								
								vo.getAccess().setMinimumInformationCheck(false);								
							}
							else {
								vo.getAccess().setMinimumInformationCheck(true);
							}
						}
						else {
							vo.getAccess().setMinimumInformationCheck(false);
						}
					}
					AccessVO accessVO = new AccessVO();
					BeanUtils.copyProperties(dataProduct.getAccess(), accessVO);
					if(Objects.nonNull(dataProduct.getAccess()) && Objects.nonNull(dataProduct.getAccess().isDeletionRequirements()))
						accessVO.setDeletionRequirements(dataProduct.getAccess().isDeletionRequirements());
					if(Objects.nonNull(dataProduct.getAccess()) && Objects.nonNull(dataProduct.getAccess().isPersonalRelatedData()))
						accessVO.setPersonalRelatedData(dataProduct.getAccess().isPersonalRelatedData());
					if(Objects.nonNull(dataProduct.getAccess()) && Objects.nonNull(dataProduct.getAccess().isRestrictDataAccess()))
						accessVO.setRestrictDataAccess(dataProduct.getAccess().isRestrictDataAccess());
					vo.setAccess(accessVO);
				}
				if(Objects.nonNull(dataProduct.getHowToAccessTemplate())) {
					HowToAccessTemplateVO howToAccessTemplateVO = new HowToAccessTemplateVO();
					BeanUtils.copyProperties(dataProduct.getHowToAccessTemplate(), howToAccessTemplateVO);
					if(Objects.nonNull(dataProduct.getHowToAccessTemplate().getAccessDetails()) && dataProduct.getHowToAccessTemplate().getAccessDetails().size() > 0) {
						List<AccessDetailsVO> accessDetailsVOs = new ArrayList<>();
						List<AccessDetails> accessDetailsList = dataProduct.getHowToAccessTemplate().getAccessDetails();
						for(AccessDetails accessDetails : accessDetailsList) {
							AccessDetailsVO accessDetailsVO = new AccessDetailsVO();
							BeanUtils.copyProperties(accessDetails, accessDetailsVO);
							List<StepDetails> stepDetails = accessDetails.getStepDetails();
							List<StepDetailsVO> stepDetailsVOs = new ArrayList<>();
							if(Objects.nonNull(stepDetails) && stepDetails.size() > 0) {
								for(StepDetails stepDetail : stepDetails) {
									StepDetailsVO stepDetailVO = new StepDetailsVO(); 
									BeanUtils.copyProperties(stepDetail, stepDetailVO);
									stepDetailVO.setStepNumber(stepDetail.getStepNumber().intValue());
									stepDetailsVOs.add(stepDetailVO);
									accessDetailsVO.setStepCollectionVO(stepDetailsVOs);
								}
							}												
							accessDetailsVOs.add(accessDetailsVO);
						}
						howToAccessTemplateVO.setAccessDetailsCollectionVO(accessDetailsVOs);												
					}
					vo.setHowToAccessTemplate(howToAccessTemplateVO);
				}
				
				DataProductContactInformation dataProductContactInformation = dataProduct.getContactInformation();
				if (dataProductContactInformation != null) {
					DataProductContactInformationVO contactInformationVO = new DataProductContactInformationVO();
					BeanUtils.copyProperties(dataProductContactInformation, contactInformationVO);
					DivisionVO divisionvo = new DivisionVO();
					Division division = dataProductContactInformation.getDivision();
					if (division != null) {
						BeanUtils.copyProperties(division, divisionvo);
						SubdivisionVO subdivisionVO = new SubdivisionVO();
						if (division.getSubdivision() != null)
							BeanUtils.copyProperties(division.getSubdivision(), subdivisionVO);
						divisionvo.setSubdivision(subdivisionVO);
						contactInformationVO.setDivision(divisionvo);
					}

//					TeamMember productOwner = dataProductContactInformation.getProductOwner();
//					DataProductTeamMemberVO productOwnerVo = new DataProductTeamMemberVO();												
//					BeanUtils.copyProperties(productOwner,productOwnerVo);
//					contactInformationVO.setProductOwner(productOwnerVo);
				
					contactInformationVO.setName(toTeamMemberVO(dataProductContactInformation.getName()));				
					contactInformationVO.setInformationOwner(toTeamMemberVO(dataProductContactInformation.getInformationOwner()));	
					vo.setProductOwner(toTeamMemberVO(dataProductContactInformation.getProductOwner()));					
					vo.setContactInformation(contactInformationVO);
				}

				if (dataProduct.getClassificationConfidentiality() != null) {
					DataProductClassificationConfidentialityVO classificationConfidentialityVO = new DataProductClassificationConfidentialityVO();
					BeanUtils.copyProperties(dataProduct.getClassificationConfidentiality(),
							classificationConfidentialityVO);
					if(Objects.nonNull(vo.getAccess()) && Objects.nonNull(dataProduct.getAccess().getConfidentiality())) {
						classificationConfidentialityVO.setConfidentiality(dataProduct.getAccess().getConfidentiality());
					}
					vo.setClassificationConfidentiality(classificationConfidentialityVO);
				}

				if (dataProduct.getPersonalRelatedData() != null) {
					DataProductPersonalRelatedDataVO personalRelatedDataVO = new DataProductPersonalRelatedDataVO();
					BeanUtils.copyProperties(dataProduct.getPersonalRelatedData(), personalRelatedDataVO);
					if(Objects.nonNull(vo.getAccess()) && Objects.nonNull(dataProduct.getAccess().isPersonalRelatedData())) {					
						personalRelatedDataVO.setPersonalRelatedData(dataProduct.getAccess().isPersonalRelatedData());
					}											
					vo.setPersonalRelatedData(personalRelatedDataVO);
				}

				if (dataProduct.getTransnationalDataTransfer() != null) {
					DataProductTransnationalDataTransferVO transnationalDataTransferVO = new DataProductTransnationalDataTransferVO();
					BeanUtils.copyProperties(dataProduct.getTransnationalDataTransfer(), transnationalDataTransferVO);
					vo.setTransnationalDataTransfer(transnationalDataTransferVO);
				}

				if (dataProduct.getDeletionRequirement() != null) {
					DataProductDeletionRequirementVO deletionRequirementVO = new DataProductDeletionRequirementVO();
					BeanUtils.copyProperties(dataProduct.getDeletionRequirement(), deletionRequirementVO);
					String insiderInfo = "";
					if(dataProduct.getTransnationalDataTransfer() != null) {
						insiderInfo = dataProduct.getTransnationalDataTransfer().getInsiderInformation() != null ? dataProduct.getTransnationalDataTransfer().getInsiderInformation() : "";
					}					
					deletionRequirementVO.setInsiderInformation(insiderInfo);
					if(Objects.nonNull(vo.getAccess()) && Objects.nonNull(dataProduct.getAccess().isDeletionRequirements())) {
						deletionRequirementVO.setDeletionRequirements(dataProduct.getAccess().isDeletionRequirements());
					}					
					vo.setDeletionRequirement(deletionRequirementVO);
				}
				if (dataProduct.getOpenSegments() != null && !ObjectUtils.isEmpty(dataProduct.getOpenSegments())) {
					List<DataProductVO.OpenSegmentsEnum> openSegmentsEnumList = new ArrayList<>();
					dataProduct.getOpenSegments().forEach(openSegment -> openSegmentsEnumList
							.add(DataProductVO.OpenSegmentsEnum.valueOf(openSegment)));
					vo.setOpenSegments(openSegmentsEnumList);
				}

				if (!ObjectUtils.isEmpty(dataProduct.getDatatransfersAssociated())) {
					vo.setDatatransfersAssociated(dataProduct.getDatatransfersAssociated());
				} else {
					vo.setDatatransfersAssociated(new ArrayList<>());
				}
			}
		}
		return vo;
	}

	@Override
	public DataProductNsql toEntity(DataProductVO vo) {
		DataProductNsql entity = null;
		if (vo != null) {
			entity = new DataProductNsql();
			String id = vo.getId();
			if (StringUtils.hasText(id)) {
				entity.setId(id);
			}
			DataProduct dataProduct = new DataProduct();
			
			if (vo != null) {
				BeanUtils.copyProperties(vo, dataProduct);
				dataProduct.setNotifyUsers(vo.isNotifyUsers());
				if(Objects.nonNull(vo.getAdditionalInformation())) {
					dataProduct.setAdditionalInformation(vo.getAdditionalInformation());
				}
				dataProduct.setPublish(vo.isIsPublish());
				if (Objects.nonNull(vo.getCreatedBy())) {
					CreatedBy userDetails = new CreatedBy();
					BeanUtils.copyProperties(vo.getCreatedBy(), userDetails);
					dataProduct.setCreatedBy(userDetails);
				}
				if (Objects.nonNull(vo.getModifiedBy())) {
					CreatedBy userDetails = new CreatedBy();
					BeanUtils.copyProperties(vo.getModifiedBy(), userDetails);
					dataProduct.setModifiedBy(userDetails);
				}
				if (Objects.nonNull(vo.getCarLaFunction())) {
					CarLaFunctionVO carLaFunctionVO = new CarLaFunctionVO();
					BeanUtils.copyProperties(vo.getCarLaFunction(), carLaFunctionVO);
					dataProduct.setCarLaFunction(carLaFunctionVO);
				}

				List<PlatformVO> platforms = vo.getPlatform();
				List<PlatformVO> platformsVO = new ArrayList<>();
				if (platforms != null && !platforms.isEmpty()) {
					for (PlatformVO platform : platforms) {
						if (platform != null) {
							PlatformVO platformVO = new PlatformVO();
							BeanUtils.copyProperties(platform, platformVO);
							platformsVO.add(platformVO);
						}
					}
					dataProduct.setPlatform(platformsVO);
				}
				
				if (!ObjectUtils.isEmpty(vo.getTags())) {
					List<String> tagsList = new ArrayList<>();
					vo.getTags().forEach(tag -> {
						tagsList.add(tag);
					});
					dataProduct.setTags(tagsList);
				}

				List<FrontendToolsVO> frontEndTools = vo.getFrontEndTools();
				List<FrontendToolsVO> frontEndToolsVo = new ArrayList<>();
				if (frontEndTools != null && !frontEndTools.isEmpty()) {
					for (FrontendToolsVO frontEndTool : frontEndTools) {
						if (frontEndTool != null) {
							FrontendToolsVO frontEndToolVo = new FrontendToolsVO();
							BeanUtils.copyProperties(frontEndTool, frontEndToolVo);
							frontEndToolsVo.add(frontEndToolVo);
						}
					}
					dataProduct.setFrontEndTools(frontEndToolsVo);
				}

				if (Objects.nonNull(vo.getAgileReleaseTrain())) {
					AgileReleaseTrainVO agileReleaseTrain = new AgileReleaseTrainVO();
					BeanUtils.copyProperties(vo.getAgileReleaseTrain(), agileReleaseTrain);
					dataProduct.setAgileReleaseTrain(agileReleaseTrain);
				}
				if (Objects.nonNull(vo.getCorporateDataCatalog())) {
					CorporateDataCatalogVO corporateDataCatalog = new CorporateDataCatalogVO();
					BeanUtils.copyProperties(vo.getCorporateDataCatalog(), corporateDataCatalog);
					dataProduct.setCorporateDataCatalog(corporateDataCatalog);
				}
				
				if(Objects.nonNull(vo.getAccess())) {
					List<String> accesstypes = vo.getAccess().getAccessType();
					if(accesstypes != null && accesstypes.size()>0 ) {
						if(accesstypes.contains("Kafka") || accesstypes.contains("API")) {
							if(vo.getAccess().getConfidentiality().equals("Internal")) {								
								vo.getAccess().setMinimumInformationCheck(false);								
							}
							else {
								vo.getAccess().setMinimumInformationCheck(true);
							}
						}
						else {
							vo.getAccess().setMinimumInformationCheck(false);
						}
					}
					Access access = new Access();
					BeanUtils.copyProperties(vo.getAccess(), access);
					if(Objects.nonNull(vo.getAccess()) && Objects.nonNull(vo.getAccess().isDeletionRequirements()))
						access.setDeletionRequirements(vo.getAccess().isDeletionRequirements());
					if(Objects.nonNull(vo.getAccess()) && Objects.nonNull(vo.getAccess().isPersonalRelatedData()))
						access.setPersonalRelatedData(vo.getAccess().isPersonalRelatedData());
					if(Objects.nonNull(vo.getAccess()) && Objects.nonNull(vo.getAccess().isRestrictDataAccess()))
						access.setRestrictDataAccess(vo.getAccess().isRestrictDataAccess());
					dataProduct.setAccess(access);
				}
				if(Objects.nonNull(vo.getHowToAccessTemplate())) {
					HowToAccessTemplate howToAccessTemplate = new HowToAccessTemplate();
					BeanUtils.copyProperties(vo.getHowToAccessTemplate(), howToAccessTemplate);
					if(Objects.nonNull(vo.getHowToAccessTemplate().getAccessDetailsCollectionVO()) && vo.getHowToAccessTemplate().getAccessDetailsCollectionVO().size() > 0) {
						List<AccessDetailsVO> accessDetailsVOs = vo.getHowToAccessTemplate().getAccessDetailsCollectionVO();
						List<AccessDetails> accessDetailsList = new ArrayList<>();
						for(AccessDetailsVO accessDetailsVO : accessDetailsVOs) {
							AccessDetails accessDetails = new AccessDetails();
							BeanUtils.copyProperties(accessDetailsVO, accessDetails);
							List<StepDetailsVO> stepDetailsVO = accessDetailsVO.getStepCollectionVO();	
							List<StepDetails> stepDetails = new ArrayList<>();
							if(Objects.nonNull(stepDetailsVO) && stepDetailsVO.size() > 0) {
								for(StepDetailsVO stepDetailVO : stepDetailsVO) {									
									StepDetails stepDetail = new StepDetails();
									BeanUtils.copyProperties(stepDetailVO, stepDetail);	
									stepDetail.setStepNumber(stepDetailVO.getStepNumber().longValue());
									stepDetails.add(stepDetail);									
								}
								accessDetails.setStepDetails(stepDetails);
							}														
							accessDetailsList.add(accessDetails);
						}
						howToAccessTemplate.setAccessDetails(accessDetailsList);												
					}
					dataProduct.setHowToAccessTemplate(howToAccessTemplate);
				}
//				if (!ObjectUtils.isEmpty(vo.getUsers())) {
//					List<TeamMember> users = vo.getUsers().stream().map(n -> toTeamMemberJson(n))
//							.collect(Collectors.toList());
//					dataProduct.setUsers(users);
//				}
				DataProductContactInformationVO dataProductContactInformationVO = vo.getContactInformation();
				if (dataProductContactInformationVO != null) {
					DataProductContactInformation contactInformation = new DataProductContactInformation();
					BeanUtils.copyProperties(dataProductContactInformationVO, contactInformation);
					DivisionVO divisionVO = dataProductContactInformationVO.getDivision();
					if (divisionVO != null) {
						Division division = new Division();
						BeanUtils.copyProperties(divisionVO, division);
						if (divisionVO.getSubdivision() != null) {
							Subdivision subdivision = new Subdivision();
							BeanUtils.copyProperties(divisionVO.getSubdivision(), subdivision);
							division.setSubdivision(subdivision);
						}
						contactInformation.setDivision(division);
					}
//					DataProductTeamMemberVO productOwnerVo = dataProductContactInformationVO.getProductOwner();
//					TeamMember productOwner = new TeamMember();
//					BeanUtils.copyProperties(productOwnerVo, productOwner);
//					contactInformation.setProductOwner(productOwner);
					contactInformation.setName(toTeamMemberJson(dataProductContactInformationVO.getName()));					
					contactInformation.setInformationOwner(toTeamMemberJson(dataProductContactInformationVO.getInformationOwner()));
					contactInformation.setProductOwner(toTeamMemberJson(vo.getProductOwner()));					
					dataProduct.setContactInformation(contactInformation);
				}

				if (vo.getClassificationConfidentiality() != null) {
					DataProductClassificationConfidentiality classificationConfidentiality = new DataProductClassificationConfidentiality();
					BeanUtils.copyProperties(vo.getClassificationConfidentiality(),
							classificationConfidentiality);
					if(Objects.nonNull(vo.getAccess()) && vo.getAccess().getConfidentiality() != null) {
						classificationConfidentiality.setConfidentiality(vo.getAccess().getConfidentiality());
					}
					dataProduct.setClassificationConfidentiality(classificationConfidentiality);
				}

				DataProductPersonalRelatedDataVO personalRelatedDataVO = vo.getPersonalRelatedData();
				if (personalRelatedDataVO != null) {
					DataProductPersonalRelatedData personalRelatedData = new DataProductPersonalRelatedData();
					BeanUtils.copyProperties(personalRelatedDataVO, personalRelatedData);
					personalRelatedData.setPersonalRelatedData(vo.getAccess().isPersonalRelatedData());
					personalRelatedData.setContactAwareTransfer(personalRelatedDataVO.isContactAwareTransfer());
					personalRelatedData.setObjectionsToTransfer(personalRelatedDataVO.isObjectionsToTransfer());
					dataProduct.setPersonalRelatedData(personalRelatedData);
				}

				DataProductTransnationalDataTransferVO transnationalDataTransferVO = vo
						.getTransnationalDataTransfer();
				if (transnationalDataTransferVO != null) {
					DataProductTransnationalDataTransfer transnationalDataTransfer = new DataProductTransnationalDataTransfer();
					BeanUtils.copyProperties(transnationalDataTransferVO, transnationalDataTransfer);
					transnationalDataTransfer.setDataTransferred(transnationalDataTransferVO.isDataTransferred());
					transnationalDataTransfer.setNotWithinEU(transnationalDataTransferVO.isNotWithinEU());
					transnationalDataTransfer.setContactAwareTransfer(transnationalDataTransferVO.isContactAwareTransfer());
					transnationalDataTransfer.setObjectionsToTransfer(transnationalDataTransferVO.isObjectionsToTransfer());
					String insiderInfo = "";
					if(vo.getDeletionRequirement() != null) {
						insiderInfo = vo.getDeletionRequirement().getInsiderInformation() != null ? vo.getDeletionRequirement().getInsiderInformation() : "";
					}					
					transnationalDataTransfer.setInsiderInformation(insiderInfo);
					dataProduct.setTransnationalDataTransfer(transnationalDataTransfer);
				}

				DataProductDeletionRequirementVO deletionRequirementVO = vo.getDeletionRequirement();
				if (deletionRequirementVO != null) {
					DataProductDeletionRequirement deletionRequirement = new DataProductDeletionRequirement();
					BeanUtils.copyProperties(deletionRequirementVO, deletionRequirement);
					deletionRequirement.setDeletionRequirements(vo.getAccess().isDeletionRequirements());
					dataProduct.setDeletionRequirement(deletionRequirement);
				}

				if (!ObjectUtils.isEmpty(vo.getOpenSegments())) {
					List<String> openSegmentList = new ArrayList<>();
					vo.getOpenSegments().forEach(openSegmentsEnum -> {
						openSegmentList.add(openSegmentsEnum.name());
					});
					dataProduct.setOpenSegments(openSegmentList);
				}

				if (!ObjectUtils.isEmpty(vo.getDatatransfersAssociated())) {
					dataProduct.setDatatransfersAssociated(vo.getDatatransfersAssociated());
				} else {
					dataProduct.setDatatransfersAssociated(new ArrayList<>());
				}
			}
			entity.setData(dataProduct);
		}

		return entity;
	}

	private DataProductTeamMemberVO toTeamMemberVO(TeamMember teamMember) {
		DataProductTeamMemberVO vo = null;
		if (teamMember != null) {
			vo = new DataProductTeamMemberVO();
			BeanUtils.copyProperties(teamMember, vo);
			if (StringUtils.hasText(teamMember.getUserType())) {
				vo.setUserType(UserTypeEnum.valueOf(teamMember.getUserType()));
			}
		}
		return vo;
	}

	private TeamMember toTeamMemberJson(DataProductTeamMemberVO vo) {
		TeamMember teamMember = null;
		if (vo != null) {
			teamMember = new TeamMember();
			BeanUtils.copyProperties(vo, teamMember);
			teamMember.setAddedByProvider(vo.isAddedByProvider());
			if (vo.getUserType() != null) {
				teamMember.setUserType(vo.getUserType().name());
			}
		}
		return teamMember;
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

		Map<String, Object> leftFlatMap = DataProductAssembler.flatten(leftMap);
		Map<String, Object> rightFlatMap = DataProductAssembler.flatten(rightMap);

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
				changeLogVO = new ChangeLogVO();
				changeLogVO.setModifiedBy(teamMemberVO);
				changeLogVO.setChangeDate(changeDate);
				changeLogVO.setFieldChanged(entry.getKey());
				changeLogVO.setOldValue(entry.getValue().toString());
				// setting change Description Starts
				changeLogVO
						.setChangeDescription(toChangeDescription(entry.getKey(), entry.getValue().toString(), null));
				changeLogsVO.add(changeLogVO);
			}
		}
		// Checking for Added values
		if (null != difference.entriesOnlyOnRight() && !difference.entriesOnlyOnRight().isEmpty()) {
			for (Entry<String, Object> entry : difference.entriesOnlyOnRight().entrySet()) {
				changeLogVO = new ChangeLogVO();
				changeLogVO.setModifiedBy(teamMemberVO);
				changeLogVO.setChangeDate(changeDate);
				changeLogVO.setFieldChanged(entry.getKey());
				changeLogVO.setNewValue(entry.getValue().toString());
				// setting change Description
				changeLogVO
						.setChangeDescription(toChangeDescription(entry.getKey(), null, entry.getValue().toString()));
				changeLogsVO.add(changeLogVO);

			}
		}
		// Checking for value differences
		if (null != difference.entriesDiffering() && !difference.entriesDiffering().isEmpty()) {
			for (Entry<String, ValueDifference<Object>> entry : difference.entriesDiffering().entrySet()) {
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
			return map.entrySet().stream().flatMap(DataProductAssembler::flatten).collect(LinkedHashMap::new,
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
					.flatMap(DataProductAssembler::flatten);
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
		StringBuilder changeDescription = new StringBuilder();
		if (keySet.length > 0) {
			String fieldValue = toHumanReadableFormat(keySet[0]);
			changeDescription.append(fieldValue + ": ");
		}
		boolean flag = false;
		for (int i = (keySet.length - 1), index = keySet.length; i >= 0; i--) {
			if (!keySet[i].matches("[0-9]") && !flag) {
				changeDescription.append(toHumanReadableFormat(keySet[i]));
				flag = true;
			} else if (keySet[i].matches("[0-9]")) {
				indexValue = Integer.parseInt(keySet[i]) + 1;
				at = " at index " + String.valueOf(indexValue);
				index = i;
			} else {
				changeDescription.append(" of " + toHumanReadableFormat(keySet[i]));
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

	public ProviderVO convertDatatransferProviderForm(DataProductVO existingDataProduct) {
		ProviderVO providerVO = new ProviderVO();
		DataProductContactInformationVO contactInformation = existingDataProduct.getContactInformation();
		providerVO.setRecordStatus(existingDataProduct.getRecordStatus());
		providerVO.setDataTransferId(existingDataProduct.getDataProductId());
		providerVO.setNotifyUsers(existingDataProduct.isNotifyUsers());

		ProviderResponseVO providerResponseVO = new ProviderResponseVO();
		providerResponseVO.setContactInformation(new ProviderContactInformationVO());
		providerResponseVO.getContactInformation().setDivision(new com.daimler.data.dto.datatransfer.DivisionVO());
		providerResponseVO.getContactInformation().getDivision().setSubdivision(new com.daimler.data.dto.datatransfer.SubdivisionVO());
		providerResponseVO.getContactInformation().setInformationOwner(new DataTransferTeamMemberVO());
		providerResponseVO.getContactInformation().setName(new DataTransferTeamMemberVO());
		providerResponseVO.setClassificationConfidentiality(new ProviderClassificationConfidentialityVO());
		providerResponseVO.setPersonalRelatedData(new ProviderPersonalRelatedDataVO());
		providerResponseVO.setTransnationalDataTransfer(new ProviderTransnationalDataTransferVO());
		providerResponseVO.setDeletionRequirement(new ProviderDeletionRequirementVO());
		providerResponseVO.setCreatedBy(existingDataProduct.getCreatedBy());
		providerResponseVO.setCreatedDate(existingDataProduct.getCreatedDate());
		
		BeanUtils.copyProperties(existingDataProduct, providerResponseVO);
		if(Objects.nonNull(contactInformation)) {
			BeanUtils.copyProperties(contactInformation, providerResponseVO.getContactInformation());
			if(Objects.nonNull(contactInformation.getDivision()))
				BeanUtils.copyProperties(contactInformation.getDivision(), providerResponseVO.getContactInformation().getDivision());
			if(Objects.nonNull(contactInformation.getDivision().getSubdivision()))
				BeanUtils.copyProperties(contactInformation.getDivision().getSubdivision(), providerResponseVO.getContactInformation().getDivision().getSubdivision());
			if(Objects.nonNull(contactInformation.getInformationOwner()))
				BeanUtils.copyProperties(contactInformation.getInformationOwner(), providerResponseVO.getContactInformation().getInformationOwner());
			if(Objects.nonNull(contactInformation.getName()))
				BeanUtils.copyProperties(contactInformation.getName(), providerResponseVO.getContactInformation().getName());
		}
		if(Objects.nonNull(existingDataProduct)) {
			if(Objects.nonNull(existingDataProduct.getClassificationConfidentiality()))
				BeanUtils.copyProperties(existingDataProduct.getClassificationConfidentiality(), providerResponseVO.getClassificationConfidentiality());
			if(Objects.nonNull(existingDataProduct.getPersonalRelatedData()))
				BeanUtils.copyProperties(existingDataProduct.getPersonalRelatedData(), providerResponseVO.getPersonalRelatedData());
			if(Objects.nonNull(existingDataProduct.getTransnationalDataTransfer()))
				BeanUtils.copyProperties(existingDataProduct.getTransnationalDataTransfer(), providerResponseVO.getTransnationalDataTransfer());
			if(Objects.nonNull(existingDataProduct.getDeletionRequirement()))
				BeanUtils.copyProperties(existingDataProduct.getDeletionRequirement(), providerResponseVO.getDeletionRequirement());
		}

		if (contactInformation.getInformationOwner().getUserType() != null) {
			providerResponseVO.getContactInformation().getInformationOwner().setUserType(DataTransferTeamMemberVO.UserTypeEnum.valueOf(contactInformation.getInformationOwner().getUserType().toString().toUpperCase()));
		}
		if (contactInformation.getName().getUserType() != null) {
			providerResponseVO.getContactInformation().getName().setUserType(DataTransferTeamMemberVO.UserTypeEnum.valueOf(contactInformation.getName().getUserType().toString().toUpperCase()));
		}
		if (contactInformation.getInformationOwner().isAddedByProvider() != null) {
			providerResponseVO.getContactInformation().getInformationOwner().setAddedByProvider(contactInformation.getInformationOwner().isAddedByProvider());
		}
		if (contactInformation.getName().isAddedByProvider() != null) {
			providerResponseVO.getContactInformation().getName().setAddedByProvider(contactInformation.getInformationOwner().isAddedByProvider());
		}
		if (existingDataProduct.getPersonalRelatedData().isPersonalRelatedData() != null) {
			providerResponseVO.getPersonalRelatedData().setPersonalRelatedData(existingDataProduct.getPersonalRelatedData().isPersonalRelatedData());
		}
		if (existingDataProduct.getPersonalRelatedData().isContactAwareTransfer() != null) {
			providerResponseVO.getPersonalRelatedData().setContactAwareTransfer(existingDataProduct.getPersonalRelatedData().isContactAwareTransfer());
		}
		if (existingDataProduct.getPersonalRelatedData().isObjectionsToTransfer() != null) {
			providerResponseVO.getPersonalRelatedData().setObjectionsToTransfer(existingDataProduct.getPersonalRelatedData().isObjectionsToTransfer());
		}
		if (existingDataProduct.getTransnationalDataTransfer().isDataTransferred() != null) {
			providerResponseVO.getTransnationalDataTransfer().setDataTransferred(existingDataProduct.getTransnationalDataTransfer().isDataTransferred());
		}
		if (existingDataProduct.getTransnationalDataTransfer().isNotWithinEU() != null) {
			providerResponseVO.getTransnationalDataTransfer().setNotWithinEU(existingDataProduct.getTransnationalDataTransfer().isNotWithinEU());
		}
		if (existingDataProduct.getTransnationalDataTransfer().isContactAwareTransfer() != null) {
			providerResponseVO.getTransnationalDataTransfer().setContactAwareTransfer(existingDataProduct.getTransnationalDataTransfer().isContactAwareTransfer());
		}
		if (existingDataProduct.getTransnationalDataTransfer().isObjectionsToTransfer() != null) {
			providerResponseVO.getTransnationalDataTransfer().setObjectionsToTransfer(existingDataProduct.getTransnationalDataTransfer().isObjectionsToTransfer());
		}
		if (existingDataProduct.getDeletionRequirement().isDeletionRequirements() != null) {
			providerResponseVO.getDeletionRequirement().setDeletionRequirements(existingDataProduct.getDeletionRequirement().isDeletionRequirements());
		}
		providerResponseVO.setProviderFormSubmitted(true);
		providerVO.setProviderInformation(providerResponseVO);

		List<ProviderResponseVO.OpenSegmentsEnum> openSegmentsEnumList = new ArrayList<>();
		openSegmentsEnumList.add(ProviderResponseVO.OpenSegmentsEnum.valueOf("CONTACTINFORMATION"));
		openSegmentsEnumList.add(ProviderResponseVO.OpenSegmentsEnum.valueOf("CLASSIFICATIONANDCONFIDENTIALITY"));
		openSegmentsEnumList.add(ProviderResponseVO.OpenSegmentsEnum.valueOf("IDENTIFYINGPERSONALRELATEDDATA"));
		openSegmentsEnumList.add(ProviderResponseVO.OpenSegmentsEnum.valueOf("IDENTIFIYINGTRANSNATIONALDATATRANSFER"));
		openSegmentsEnumList.add(ProviderResponseVO.OpenSegmentsEnum.valueOf("SPECIFYDELETIONREQUIREMENTS"));
		providerVO.getProviderInformation().setOpenSegments(List.copyOf(openSegmentsEnumList));

		return providerVO;
	}

	public DataTransferConsumerRequestVO convertDatatransferConsumerForm(DataTransferConsumerRequestInfoVO dataTransferConsumerRequestInfoVO) {
		DataTransferConsumerRequestVO dataTransferConsumerRequestVO = new DataTransferConsumerRequestVO();

		dataTransferConsumerRequestVO.setData(new ConsumerVO());
		dataTransferConsumerRequestVO.getData().setId(dataTransferConsumerRequestVO.getData().getId());
		dataTransferConsumerRequestVO.getData().setConsumerInformation(new ConsumerResponseVO());
		dataTransferConsumerRequestVO.getData().getConsumerInformation().setContactInformation(new ConsumerContactInformationVO());
		dataTransferConsumerRequestVO.getData().getConsumerInformation().getContactInformation().setDivision(new com.daimler.data.dto.datatransfer.DivisionVO());
		dataTransferConsumerRequestVO.getData().getConsumerInformation().getContactInformation().getDivision().setSubdivision(new com.daimler.data.dto.datatransfer.SubdivisionVO());
		dataTransferConsumerRequestVO.getData().getConsumerInformation().getContactInformation().setOwnerName(new DataTransferTeamMemberVO());
		dataTransferConsumerRequestVO.getData().getConsumerInformation().setPersonalRelatedData(new ConsumerPersonalRelatedDataVO());

		com.daimler.data.dto.dataproduct.ConsumerRequestVO dataproductConsumerData = dataTransferConsumerRequestInfoVO.getData();
		ConsumerVO datatransferConsumerData = dataTransferConsumerRequestVO.getData();

		BeanUtils.copyProperties(dataproductConsumerData, datatransferConsumerData);
		BeanUtils.copyProperties(dataproductConsumerData.getConsumerInformation(), datatransferConsumerData.getConsumerInformation());
		BeanUtils.copyProperties(dataproductConsumerData.getConsumerInformation().getContactInformation(), datatransferConsumerData.getConsumerInformation().getContactInformation());
		BeanUtils.copyProperties(dataproductConsumerData.getConsumerInformation().getContactInformation().getOwnerName(), datatransferConsumerData.getConsumerInformation().getContactInformation().getOwnerName());
		BeanUtils.copyProperties(dataproductConsumerData.getConsumerInformation().getContactInformation().getDivision(), datatransferConsumerData.getConsumerInformation().getContactInformation().getDivision());
		BeanUtils.copyProperties(dataproductConsumerData.getConsumerInformation().getContactInformation().getDivision().getSubdivision(), datatransferConsumerData.getConsumerInformation().getContactInformation().getDivision().getSubdivision());
		BeanUtils.copyProperties(dataproductConsumerData.getConsumerInformation().getPersonalRelatedData(), datatransferConsumerData.getConsumerInformation().getPersonalRelatedData());

		if (dataproductConsumerData.getConsumerInformation().getContactInformation().isLcoNeeded() != null) {
			datatransferConsumerData.getConsumerInformation().getContactInformation().setLcoNeeded(dataproductConsumerData.getConsumerInformation().getContactInformation().isLcoNeeded());
		}
		if (dataproductConsumerData.getConsumerInformation().getContactInformation().getOwnerName().getUserType() != null) {
			datatransferConsumerData.getConsumerInformation().getContactInformation().getOwnerName().setUserType(DataTransferTeamMemberVO.UserTypeEnum.valueOf(dataproductConsumerData.getConsumerInformation().getContactInformation().getOwnerName().getUserType().toString().toUpperCase()));
		}
		if (dataproductConsumerData.getConsumerInformation().getContactInformation().getOwnerName().isAddedByProvider() != null) {
			datatransferConsumerData.getConsumerInformation().getContactInformation().getOwnerName().setAddedByProvider(dataproductConsumerData.getConsumerInformation().getContactInformation().getOwnerName().isAddedByProvider());
		}
		if (dataproductConsumerData.getConsumerInformation().getPersonalRelatedData().isPersonalRelatedData() != null) {
			datatransferConsumerData.getConsumerInformation().getPersonalRelatedData().setPersonalRelatedData(dataproductConsumerData.getConsumerInformation().getPersonalRelatedData().isPersonalRelatedData());
		}
		if (dataproductConsumerData.getConsumerInformation().getPersonalRelatedData().isContactAwareTransfer() != null) {
			datatransferConsumerData.getConsumerInformation().getPersonalRelatedData().setContactAwareTransfer(dataproductConsumerData.getConsumerInformation().getPersonalRelatedData().isContactAwareTransfer());
		}
		if (dataproductConsumerData.getConsumerInformation().getPersonalRelatedData().isObjectionsToTransfer() != null) {
			datatransferConsumerData.getConsumerInformation().getPersonalRelatedData().setObjectionsToTransfer(dataproductConsumerData.getConsumerInformation().getPersonalRelatedData().isObjectionsToTransfer());
		}
		if (dataproductConsumerData.isNotifyUsers() != null) {
			datatransferConsumerData.setNotifyUsers(dataproductConsumerData.isNotifyUsers());
		}

		datatransferConsumerData.getConsumerInformation().setCreatedDate(new Date());

		List<ConsumerResponseVO.OpenSegmentsEnum> openSegmentsEnumList = new ArrayList<>();
		openSegmentsEnumList.add(ConsumerResponseVO.OpenSegmentsEnum.valueOf("CONTACTINFORMATION"));
		openSegmentsEnumList.add(ConsumerResponseVO.OpenSegmentsEnum.valueOf("IDENTIFYINGPERSONALRELATEDDATA"));
		datatransferConsumerData.getConsumerInformation().setOpenSegments(List.copyOf(openSegmentsEnumList));

		return dataTransferConsumerRequestVO;
	}
}
