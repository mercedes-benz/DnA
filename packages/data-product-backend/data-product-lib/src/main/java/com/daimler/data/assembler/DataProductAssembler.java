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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.DataProductNsql;
import com.daimler.data.db.jsonb.CreatedBy;
import com.daimler.data.db.jsonb.dataproduct.Consumer;
import com.daimler.data.db.jsonb.dataproduct.ConsumerContactInformation;
import com.daimler.data.db.jsonb.dataproduct.ConsumerPersonalRelatedData;
import com.daimler.data.db.jsonb.dataproduct.DataProduct;
import com.daimler.data.db.jsonb.dataproduct.Division;
import com.daimler.data.db.jsonb.dataproduct.Provider;
import com.daimler.data.db.jsonb.dataproduct.ProviderClassificationConfidentiality;
import com.daimler.data.db.jsonb.dataproduct.ProviderContactInformation;
import com.daimler.data.db.jsonb.dataproduct.ProviderDeletionRequirement;
import com.daimler.data.db.jsonb.dataproduct.ProviderPersonalRelatedData;
import com.daimler.data.db.jsonb.dataproduct.ProviderTransnationalDataTransfer;
import com.daimler.data.db.jsonb.dataproduct.Subdivision;
import com.daimler.data.db.jsonb.dataproduct.TeamMember;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.dataproduct.ConsumerContactInformationVO;
import com.daimler.data.dto.dataproduct.ConsumerPersonalRelatedDataVO;
import com.daimler.data.dto.dataproduct.ConsumerVO;
import com.daimler.data.dto.dataproduct.DataProductVO;
import com.daimler.data.dto.dataproduct.DivisionVO;
import com.daimler.data.dto.dataproduct.ProviderClassificationConfidentialityVO;
import com.daimler.data.dto.dataproduct.ProviderContactInformationVO;
import com.daimler.data.dto.dataproduct.ProviderDeletionRequirementVO;
import com.daimler.data.dto.dataproduct.ProviderPersonalRelatedDataVO;
import com.daimler.data.dto.dataproduct.ProviderTransnationalDataTransferVO;
import com.daimler.data.dto.dataproduct.ProviderVO;
import com.daimler.data.dto.dataproduct.SubdivisionVO;
import com.daimler.data.dto.dataproduct.TeamMemberVO;
import com.daimler.data.dto.dataproduct.TeamMemberVO.UserTypeEnum;

@Component
public class DataProductAssembler implements GenericAssembler<DataProductVO, DataProductNsql> {

	@Override
	public DataProductVO toVo(DataProductNsql entity) {
		DataProductVO vo = null;
		if (entity != null && entity.getData() != null) {
			vo = new DataProductVO();
			DataProduct dataProduct = entity.getData();
			BeanUtils.copyProperties(dataProduct, vo);

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
			if (!ObjectUtils.isEmpty(dataProduct.getUsers())) {
				List<TeamMemberVO> users = dataProduct.getUsers().stream().map(n -> toTeamMemberVO(n))
						.collect(Collectors.toList());
				vo.setUsers(users);
			}

			Provider provider = dataProduct.getProviderInformation();
			if (provider != null) {
				ProviderVO providerVO = new ProviderVO();
				if (provider.getContactInformation() != null) {
					ProviderContactInformationVO contactInformationVO = new ProviderContactInformationVO();
					BeanUtils.copyProperties(provider.getContactInformation(), contactInformationVO);
					DivisionVO divisionvo = new DivisionVO();
					Division division = provider.getContactInformation().getDivision();
					if (division != null) {
						BeanUtils.copyProperties(division, divisionvo);
						SubdivisionVO subdivisionVO = new SubdivisionVO();
						if (division.getSubdivision() != null)
							BeanUtils.copyProperties(division.getSubdivision(), subdivisionVO);
						divisionvo.setSubdivision(subdivisionVO);
						contactInformationVO.setDivision(divisionvo);
					}
					providerVO.setContactInformation(contactInformationVO);
				}

				if (provider.getClassificationConfidentiality() != null) {
					ProviderClassificationConfidentialityVO classificationConfidentialityVO = new ProviderClassificationConfidentialityVO();
					BeanUtils.copyProperties(provider.getClassificationConfidentiality(),
							classificationConfidentialityVO);
					providerVO.setClassificationConfidentiality(classificationConfidentialityVO);
				}

				if (provider.getPersonalRelatedData() != null) {
					ProviderPersonalRelatedDataVO personalRelatedDataVO = new ProviderPersonalRelatedDataVO();
					BeanUtils.copyProperties(provider.getPersonalRelatedData(), personalRelatedDataVO);
					providerVO.setPersonalRelatedData(personalRelatedDataVO);
				}

				if (provider.getTransnationalDataTransfer() != null) {
					ProviderTransnationalDataTransferVO transnationalDataTransferVO = new ProviderTransnationalDataTransferVO();
					BeanUtils.copyProperties(provider.getTransnationalDataTransfer(), transnationalDataTransferVO);
					providerVO.setTransnationalDataTransfer(transnationalDataTransferVO);
				}

				if (provider.getDeletionRequirement() != null) {
					ProviderDeletionRequirementVO deletionRequirementVO = new ProviderDeletionRequirementVO();
					BeanUtils.copyProperties(provider.getDeletionRequirement(), deletionRequirementVO);
					providerVO.setDeletionRequirement(deletionRequirementVO);
				}
				if (!ObjectUtils.isEmpty(provider.getOpenSegments())) {
					List<ProviderVO.OpenSegmentsEnum> openSegmentsEnumList = new ArrayList<>();
					provider.getOpenSegments().forEach(
							openSegment -> openSegmentsEnumList.add(ProviderVO.OpenSegmentsEnum.valueOf(openSegment)));
					providerVO.setOpenSegments(openSegmentsEnumList);
				}
				vo.setProviderInformation(providerVO);
			}

			Consumer consumer = dataProduct.getConsumerInformation();
			if (consumer != null) {
				ConsumerVO consumerVO = new ConsumerVO();
				if (consumer.getContactInformation() != null) {
					ConsumerContactInformationVO contactInformationVO = new ConsumerContactInformationVO();
					BeanUtils.copyProperties(consumer.getContactInformation(), contactInformationVO);
					DivisionVO divisionvo = new DivisionVO();
					Division division = consumer.getContactInformation().getDivision();
					if (division != null) {
						BeanUtils.copyProperties(division, divisionvo);
						SubdivisionVO subdivisionVO = new SubdivisionVO();
						if (division.getSubdivision() != null)
							BeanUtils.copyProperties(division.getSubdivision(), subdivisionVO);
						divisionvo.setSubdivision(subdivisionVO);
						contactInformationVO.setDivision(divisionvo);
					}
					consumerVO.setContactInformation(contactInformationVO);
				}

				if (consumer.getPersonalRelatedData() != null) {
					ConsumerPersonalRelatedDataVO personalRelatedDataVO = new ConsumerPersonalRelatedDataVO();
					BeanUtils.copyProperties(consumer.getPersonalRelatedData(), personalRelatedDataVO);
					consumerVO.setPersonalRelatedData(personalRelatedDataVO);
				}

				if (!ObjectUtils.isEmpty(consumer.getOpenSegments())) {
					List<ConsumerVO.OpenSegmentsEnum> openSegmentsEnumList = new ArrayList<>();
					consumer.getOpenSegments().forEach(
							openSegment -> openSegmentsEnumList.add(ConsumerVO.OpenSegmentsEnum.valueOf(openSegment)));
					consumerVO.setOpenSegments(openSegmentsEnumList);
				}
				vo.setConsumerInformation(consumerVO);
			}

			vo.setId(entity.getId());

		}

		return vo;
	}

	@Override
	public DataProductNsql toEntity(DataProductVO vo) {
		DataProductNsql entity = null;
		if (vo != null) {
			entity = new DataProductNsql();
			String id = vo.getId();
			if (id != null && !id.isEmpty() && !id.trim().isEmpty()) {
				entity.setId(id);
			}
			DataProduct dataProduct = new DataProduct();
			BeanUtils.copyProperties(vo, dataProduct);
			dataProduct.setPublish(vo.isPublish());
			dataProduct.setNotifyUsers(vo.isNotifyUsers());
			dataProduct.setProviderFormSubmitted(vo.isProviderFormSubmitted());
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
			if (!ObjectUtils.isEmpty(vo.getUsers())) {
				List<TeamMember> users = vo.getUsers().stream().map(n -> toTeamMemberJson(n))
						.collect(Collectors.toList());
				dataProduct.setUsers(users);
			}

			ProviderVO providerVO = vo.getProviderInformation();
			if (providerVO != null) {
				Provider provider = new Provider();
				if (providerVO.getContactInformation() != null) {
					ProviderContactInformation contactInformation = new ProviderContactInformation();
					BeanUtils.copyProperties(providerVO.getContactInformation(), contactInformation);
					DivisionVO divisionVO = providerVO.getContactInformation().getDivision();
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
					provider.setContactInformation(contactInformation);
				}

				if (providerVO.getClassificationConfidentiality() != null) {
					ProviderClassificationConfidentiality classificationConfidentiality = new ProviderClassificationConfidentiality();
					BeanUtils.copyProperties(providerVO.getClassificationConfidentiality(),
							classificationConfidentiality);
					provider.setClassificationConfidentiality(classificationConfidentiality);
				}

				ProviderPersonalRelatedDataVO personalRelatedDataVO = providerVO.getPersonalRelatedData();
				if (personalRelatedDataVO != null) {
					ProviderPersonalRelatedData personalRelatedData = new ProviderPersonalRelatedData();
					BeanUtils.copyProperties(personalRelatedDataVO, personalRelatedData);
					personalRelatedData.setPersonalRelatedData(personalRelatedDataVO.isPersonalRelatedData());
					provider.setPersonalRelatedData(personalRelatedData);
				}

				ProviderTransnationalDataTransferVO transnationalDataTransferVO = providerVO
						.getTransnationalDataTransfer();
				if (transnationalDataTransferVO != null) {
					ProviderTransnationalDataTransfer transnationalDataTransfer = new ProviderTransnationalDataTransfer();
					transnationalDataTransfer.setDataTransferred(transnationalDataTransferVO.isDataTransferred());
					transnationalDataTransfer.setNotWithinEU(transnationalDataTransferVO.isNotWithinEU());
					transnationalDataTransfer.setApproved(transnationalDataTransferVO.getApproved());
					transnationalDataTransfer.setDataFromChina(transnationalDataTransferVO.isDataFromChina());
					provider.setTransnationalDataTransfer(transnationalDataTransfer);
				}

				ProviderDeletionRequirementVO deletionRequirementVO = providerVO.getDeletionRequirement();
				if (deletionRequirementVO != null) {
					ProviderDeletionRequirement deletionRequirement = new ProviderDeletionRequirement();
					BeanUtils.copyProperties(deletionRequirementVO, deletionRequirement);
					deletionRequirement.setDeletionRequirements(deletionRequirementVO.isDeletionRequirements());
					provider.setDeletionRequirement(deletionRequirement);
				}

				if (!ObjectUtils.isEmpty(providerVO.getOpenSegments())) {
					List<String> openSegmentList = new ArrayList<>();
					providerVO.getOpenSegments().forEach(openSegmentsEnum -> {
						openSegmentList.add(openSegmentsEnum.name());
					});
					provider.setOpenSegments(openSegmentList);
				}
				dataProduct.setProviderInformation(provider);
			}
			ConsumerVO consumerVO = vo.getConsumerInformation();
			if (consumerVO != null) {
				Consumer consumer = new Consumer();
				ConsumerContactInformationVO consumerContactInformationVO = consumerVO.getContactInformation();
				if (consumerContactInformationVO != null) {
					ConsumerContactInformation contactInformation = new ConsumerContactInformation();
					BeanUtils.copyProperties(consumerContactInformationVO, contactInformation);
					contactInformation.setLcoNeeded(consumerContactInformationVO.isLcoNeeded());
					DivisionVO divisionVO = consumerContactInformationVO.getDivision();
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
					consumer.setContactInformation(contactInformation);
				}

				ConsumerPersonalRelatedDataVO personalRelatedDataVO = consumerVO.getPersonalRelatedData();
				if (personalRelatedDataVO != null) {
					ConsumerPersonalRelatedData personalRelatedData = new ConsumerPersonalRelatedData();
					BeanUtils.copyProperties(personalRelatedDataVO, personalRelatedData);
					personalRelatedData.setPersonalRelatedData(personalRelatedDataVO.isPersonalRelatedData());
					consumer.setPersonalRelatedData(personalRelatedData);
				}

				if (!ObjectUtils.isEmpty(consumerVO.getOpenSegments())) {
					List<String> openSegmentList = new ArrayList<>();
					consumerVO.getOpenSegments().forEach(openSegmentsEnum -> {
						openSegmentList.add(openSegmentsEnum.name());
					});
					consumer.setOpenSegments(openSegmentList);
				}
				dataProduct.setConsumerInformation(consumer);
			}
			entity.setData(dataProduct);
		}

		return entity;
	}

	private TeamMemberVO toTeamMemberVO(TeamMember teamMember) {
		TeamMemberVO vo = null;
		if (teamMember != null) {
			vo = new TeamMemberVO();
			BeanUtils.copyProperties(teamMember, vo);
			if (StringUtils.hasText(teamMember.getUserType())) {
				vo.setUserType(UserTypeEnum.valueOf(teamMember.getUserType()));
			}
		}
		return vo;
	}

	private TeamMember toTeamMemberJson(TeamMemberVO vo) {
		TeamMember teamMember = null;
		if (vo != null) {
			teamMember = new TeamMember();
			BeanUtils.copyProperties(vo, teamMember);
			if (vo.getUserType() != null) {
				teamMember.setUserType(vo.getUserType().name());
			}
		}
		return teamMember;
	}

}
