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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.DataTransferNsql;
import com.daimler.data.db.jsonb.CreatedBy;
import com.daimler.data.db.jsonb.datatransfer.Consumer;
import com.daimler.data.db.jsonb.datatransfer.ConsumerContactInformation;
import com.daimler.data.db.jsonb.datatransfer.ConsumerPersonalRelatedData;
import com.daimler.data.db.jsonb.datatransfer.DataTranfer;
import com.daimler.data.db.jsonb.datatransfer.Division;
import com.daimler.data.db.jsonb.datatransfer.Provider;
import com.daimler.data.db.jsonb.datatransfer.ProviderClassificationConfidentiality;
import com.daimler.data.db.jsonb.datatransfer.ProviderContactInformation;
import com.daimler.data.db.jsonb.datatransfer.ProviderDeletionRequirement;
import com.daimler.data.db.jsonb.datatransfer.ProviderPersonalRelatedData;
import com.daimler.data.db.jsonb.datatransfer.ProviderTransnationalDataTransfer;
import com.daimler.data.db.jsonb.datatransfer.Subdivision;
import com.daimler.data.db.jsonb.datatransfer.TeamMember;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.datatransfer.ChangeLogVO;
import com.daimler.data.dto.datatransfer.ConsumerContactInformationVO;
import com.daimler.data.dto.datatransfer.ConsumerPersonalRelatedDataVO;
import com.daimler.data.dto.datatransfer.ConsumerResponseVO;
import com.daimler.data.dto.datatransfer.DataTransferTeamMemberVO;
import com.daimler.data.dto.datatransfer.DataTransferTeamMemberVO.UserTypeEnum;
import com.daimler.data.dto.datatransfer.DataTransferVO;
import com.daimler.data.dto.datatransfer.DivisionVO;
import com.daimler.data.dto.datatransfer.ProviderClassificationConfidentialityVO;
import com.daimler.data.dto.datatransfer.ProviderContactInformationVO;
import com.daimler.data.dto.datatransfer.ProviderDeletionRequirementVO;
import com.daimler.data.dto.datatransfer.ProviderPersonalRelatedDataVO;
import com.daimler.data.dto.datatransfer.ProviderResponseVO;
import com.daimler.data.dto.datatransfer.ProviderTransnationalDataTransferVO;
import com.daimler.data.dto.datatransfer.SubdivisionVO;
import com.daimler.data.dto.datatransfer.TeamMemberVO;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class DataTransferAssembler implements GenericAssembler<DataTransferVO, DataTransferNsql> {

	@Override
	public DataTransferVO toVo(DataTransferNsql entity) {
		DataTransferVO vo = null;
		if (entity != null && entity.getData() != null) {
			vo = new DataTransferVO();
			vo.setId(entity.getId());
			DataTranfer dataTransfer = entity.getData();
			BeanUtils.copyProperties(dataTransfer, vo);
			Provider provider = dataTransfer.getProviderInformation();
			if (provider != null) {
				ProviderResponseVO providerVO = new ProviderResponseVO();
				BeanUtils.copyProperties(provider, providerVO);
				if (Objects.nonNull(provider.getCreatedBy())) {
					CreatedByVO createdByVO = new CreatedByVO();
					BeanUtils.copyProperties(provider.getCreatedBy(), createdByVO);
					providerVO.setCreatedBy(createdByVO);
				}
				if (Objects.nonNull(provider.getModifiedBy())) {
					CreatedByVO updatedByVO = new CreatedByVO();
					BeanUtils.copyProperties(provider.getModifiedBy(), updatedByVO);
					providerVO.setModifiedBy(updatedByVO);
				}
				if (!ObjectUtils.isEmpty(provider.getUsers())) {
					List<DataTransferTeamMemberVO> users = provider.getUsers().stream().map(n -> toTeamMemberVO(n))
							.collect(Collectors.toList());
					providerVO.setUsers(users);
				}

				ProviderContactInformation providerContactInformation = provider.getContactInformation();
				if (providerContactInformation != null) {
					ProviderContactInformationVO contactInformationVO = new ProviderContactInformationVO();
					BeanUtils.copyProperties(providerContactInformation, contactInformationVO);
					DivisionVO divisionvo = new DivisionVO();
					Division division = providerContactInformation.getDivision();
					if (division != null) {
						BeanUtils.copyProperties(division, divisionvo);
						SubdivisionVO subdivisionVO = new SubdivisionVO();
						if (division.getSubdivision() != null)
							BeanUtils.copyProperties(division.getSubdivision(), subdivisionVO);
						divisionvo.setSubdivision(subdivisionVO);
						contactInformationVO.setDivision(divisionvo);
					}

					contactInformationVO.setName(toTeamMemberVO(providerContactInformation.getName()));
					contactInformationVO.setInformationOwner(toTeamMemberVO(providerContactInformation.getInformationOwner()));
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
					transnationalDataTransferVO.setContactAwareTransfer(provider.getTransnationalDataTransfer().isContactAwareTransfer());
					transnationalDataTransferVO.setObjectionsToTransfer(provider.getTransnationalDataTransfer().isObjectionsToTransfer());
					providerVO.setTransnationalDataTransfer(transnationalDataTransferVO);
				}

				if (provider.getDeletionRequirement() != null) {
					ProviderDeletionRequirementVO deletionRequirementVO = new ProviderDeletionRequirementVO();
					BeanUtils.copyProperties(provider.getDeletionRequirement(), deletionRequirementVO);
					String insiderInfo = "";
					if(provider.getTransnationalDataTransfer() != null) {
						insiderInfo = provider.getTransnationalDataTransfer().getInsiderInformation() != null ? provider.getTransnationalDataTransfer().getInsiderInformation() : "";
					}					
					deletionRequirementVO.setInsiderInformation(insiderInfo);
					providerVO.setDeletionRequirement(deletionRequirementVO);
				}
				if (!ObjectUtils.isEmpty(provider.getOpenSegments())) {
					List<ProviderResponseVO.OpenSegmentsEnum> openSegmentsEnumList = new ArrayList<>();
					provider.getOpenSegments().forEach(openSegment -> openSegmentsEnumList
							.add(ProviderResponseVO.OpenSegmentsEnum.valueOf(openSegment)));
					providerVO.setOpenSegments(openSegmentsEnumList);
				}
				vo.setProviderInformation(providerVO);
			}

			Consumer consumer = dataTransfer.getConsumerInformation();
			if (consumer != null) {
				ConsumerResponseVO consumerVO = new ConsumerResponseVO();
				BeanUtils.copyProperties(consumer, consumerVO);
				if (Objects.nonNull(consumer.getCreatedBy())) {
					CreatedByVO createdByVO = new CreatedByVO();
					BeanUtils.copyProperties(consumer.getCreatedBy(), createdByVO);
					consumerVO.setCreatedBy(createdByVO);
				}
				if (Objects.nonNull(consumer.getModifiedBy())) {
					CreatedByVO updatedByVO = new CreatedByVO();
					BeanUtils.copyProperties(consumer.getModifiedBy(), updatedByVO);
					consumerVO.setModifiedBy(updatedByVO);
				}
				ConsumerContactInformation consumerContactInformation = consumer.getContactInformation();
				if (consumerContactInformation != null) {
					ConsumerContactInformationVO contactInformationVO = new ConsumerContactInformationVO();
					BeanUtils.copyProperties(consumerContactInformation, contactInformationVO);
					DivisionVO divisionvo = new DivisionVO();
					Division division = consumerContactInformation.getDivision();
					if (division != null) {
						BeanUtils.copyProperties(division, divisionvo);
						SubdivisionVO subdivisionVO = new SubdivisionVO();
						if (division.getSubdivision() != null)
							BeanUtils.copyProperties(division.getSubdivision(), subdivisionVO);
						divisionvo.setSubdivision(subdivisionVO);
						contactInformationVO.setDivision(divisionvo);
					}
					contactInformationVO.setOwnerName(toTeamMemberVO(consumerContactInformation.getOwnerName()));
					consumerVO.setContactInformation(contactInformationVO);
				}

				if (consumer.getPersonalRelatedData() != null) {
					ConsumerPersonalRelatedDataVO personalRelatedDataVO = new ConsumerPersonalRelatedDataVO();
					BeanUtils.copyProperties(consumer.getPersonalRelatedData(), personalRelatedDataVO);
					consumerVO.setPersonalRelatedData(personalRelatedDataVO);
				}

				if (!ObjectUtils.isEmpty(consumer.getOpenSegments())) {
					List<ConsumerResponseVO.OpenSegmentsEnum> openSegmentsEnumList = new ArrayList<>();
					consumer.getOpenSegments().forEach(openSegment -> openSegmentsEnumList
							.add(ConsumerResponseVO.OpenSegmentsEnum.valueOf(openSegment)));
					consumerVO.setOpenSegments(openSegmentsEnumList);
				}
				vo.setConsumerInformation(consumerVO);
			}

		}

		return vo;
	}

	@Override
	public DataTransferNsql toEntity(DataTransferVO vo) {
		DataTransferNsql entity = null;
		if (vo != null) {
			entity = new DataTransferNsql();
			String id = vo.getId();
			if (StringUtils.hasText(id)) {
				entity.setId(id);
			}
			DataTranfer dataTransfer = new DataTranfer();
			BeanUtils.copyProperties(vo, dataTransfer);
			dataTransfer.setNotifyUsers(vo.isNotifyUsers());
			dataTransfer.setPublish(vo.isPublish());
			ProviderResponseVO providerVO = vo.getProviderInformation();
			if (providerVO != null) {
				Provider provider = new Provider();
				BeanUtils.copyProperties(providerVO, provider);
				provider.setProviderFormSubmitted(providerVO.isProviderFormSubmitted());
				if (Objects.nonNull(providerVO.getCreatedBy())) {
					CreatedBy userDetails = new CreatedBy();
					BeanUtils.copyProperties(providerVO.getCreatedBy(), userDetails);
					provider.setCreatedBy(userDetails);
				}
				if (Objects.nonNull(providerVO.getModifiedBy())) {
					CreatedBy userDetails = new CreatedBy();
					BeanUtils.copyProperties(providerVO.getModifiedBy(), userDetails);
					provider.setModifiedBy(userDetails);
				}
				if (!ObjectUtils.isEmpty(providerVO.getUsers())) {
					List<TeamMember> users = providerVO.getUsers().stream().map(n -> toTeamMemberJson(n))
							.collect(Collectors.toList());
					provider.setUsers(users);
				}
				ProviderContactInformationVO providerContactInformationVO = providerVO.getContactInformation();
				if (providerContactInformationVO != null) {
					ProviderContactInformation contactInformation = new ProviderContactInformation();
					BeanUtils.copyProperties(providerContactInformationVO, contactInformation);
					DivisionVO divisionVO = providerContactInformationVO.getDivision();
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
					contactInformation.setName(toTeamMemberJson(providerContactInformationVO.getName()));
					contactInformation.setInformationOwner(toTeamMemberJson(providerContactInformationVO.getInformationOwner()));
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
					personalRelatedData.setContactAwareTransfer(personalRelatedDataVO.isContactAwareTransfer());
					personalRelatedData.setObjectionsToTransfer(personalRelatedDataVO.isObjectionsToTransfer());
					provider.setPersonalRelatedData(personalRelatedData);
				}

				ProviderTransnationalDataTransferVO transnationalDataTransferVO = providerVO
						.getTransnationalDataTransfer();
				if (transnationalDataTransferVO != null) {
					ProviderTransnationalDataTransfer transnationalDataTransfer = new ProviderTransnationalDataTransfer();
					BeanUtils.copyProperties(transnationalDataTransferVO, transnationalDataTransfer);
					transnationalDataTransfer.setDataTransferred(transnationalDataTransferVO.isDataTransferred());
					transnationalDataTransfer.setNotWithinEU(transnationalDataTransferVO.isNotWithinEU());
					String insiderInfo = "";
					if(providerVO.getDeletionRequirement() != null) {
						insiderInfo = providerVO.getDeletionRequirement().getInsiderInformation() != null ? providerVO.getDeletionRequirement().getInsiderInformation() : "";
					}
					transnationalDataTransfer.setInsiderInformation(insiderInfo);
					transnationalDataTransfer.setContactAwareTransfer(transnationalDataTransferVO.isContactAwareTransfer());
					transnationalDataTransfer.setObjectionsToTransfer(transnationalDataTransferVO.isObjectionsToTransfer());
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
				dataTransfer.setProviderInformation(provider);
			}
			ConsumerResponseVO consumerVO = vo.getConsumerInformation();
			if (consumerVO != null) {
				Consumer consumer = new Consumer();
				BeanUtils.copyProperties(consumerVO, consumer);

				if (Objects.nonNull(consumerVO.getCreatedBy())) {
					CreatedBy userDetails = new CreatedBy();
					BeanUtils.copyProperties(consumerVO.getCreatedBy(), userDetails);
					consumer.setCreatedBy(userDetails);
				}
				if (Objects.nonNull(consumerVO.getModifiedBy())) {
					CreatedBy userDetails = new CreatedBy();
					BeanUtils.copyProperties(consumerVO.getModifiedBy(), userDetails);
					consumer.setModifiedBy(userDetails);
				}
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
					contactInformation.setOwnerName(toTeamMemberJson(consumerContactInformationVO.getOwnerName()));
					consumer.setContactInformation(contactInformation);
				}

				ConsumerPersonalRelatedDataVO personalRelatedDataVO = consumerVO.getPersonalRelatedData();
				if (personalRelatedDataVO != null) {
					ConsumerPersonalRelatedData personalRelatedData = new ConsumerPersonalRelatedData();
					BeanUtils.copyProperties(personalRelatedDataVO, personalRelatedData);
					personalRelatedData.setPersonalRelatedData(personalRelatedDataVO.isPersonalRelatedData());
					personalRelatedData.setContactAwareTransfer(personalRelatedDataVO.isContactAwareTransfer());
					personalRelatedData.setObjectionsToTransfer(personalRelatedDataVO.isObjectionsToTransfer());
					consumer.setPersonalRelatedData(personalRelatedData);
				}


				if (!ObjectUtils.isEmpty(consumerVO.getOpenSegments())) {
					List<String> openSegmentList = new ArrayList<>();
					consumerVO.getOpenSegments().forEach(openSegmentsEnum -> {
						openSegmentList.add(openSegmentsEnum.name());
					});
					consumer.setOpenSegments(openSegmentList);
				}
				dataTransfer.setConsumerInformation(consumer);
			}
			entity.setData(dataTransfer);
		}

		return entity;
	}

	private DataTransferTeamMemberVO toTeamMemberVO(TeamMember teamMember) {
		DataTransferTeamMemberVO vo = null;
		if (teamMember != null) {
			vo = new DataTransferTeamMemberVO();
			BeanUtils.copyProperties(teamMember, vo);
			if (StringUtils.hasText(teamMember.getUserType())) {
				vo.setUserType(UserTypeEnum.valueOf(teamMember.getUserType()));
			}
		}
		return vo;
	}

	private TeamMember toTeamMemberJson(DataTransferTeamMemberVO vo) {
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

		Map<String, Object> leftFlatMap = DataTransferAssembler.flatten(leftMap);
		Map<String, Object> rightFlatMap = DataTransferAssembler.flatten(rightMap);

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
			return map.entrySet().stream().flatMap(DataTransferAssembler::flatten).collect(LinkedHashMap::new,
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
					.flatMap(DataTransferAssembler::flatten);
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

}
