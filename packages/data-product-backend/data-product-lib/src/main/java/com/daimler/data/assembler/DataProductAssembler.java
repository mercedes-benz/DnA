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

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.daimler.data.db.entities.DataProductNsql;
import com.daimler.data.db.jsonb.CreatedBy;
import com.daimler.data.db.jsonb.dataproduct.ClassificationConfidentiality;
import com.daimler.data.db.jsonb.dataproduct.ContactInformation;
import com.daimler.data.db.jsonb.dataproduct.DataProduct;
import com.daimler.data.db.jsonb.dataproduct.DeletionRequirement;
import com.daimler.data.db.jsonb.dataproduct.Division;
import com.daimler.data.db.jsonb.dataproduct.PersonalRelatedData;
import com.daimler.data.db.jsonb.dataproduct.Subdivision;
import com.daimler.data.db.jsonb.dataproduct.TransnationalDataTransfer;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.dataproduct.ClassificationConfidentialityVO;
import com.daimler.data.dto.dataproduct.ContactInformationVO;
import com.daimler.data.dto.dataproduct.DataProductVO;
import com.daimler.data.dto.dataproduct.DeletionRequirementVO;
import com.daimler.data.dto.dataproduct.DivisionVO;
import com.daimler.data.dto.dataproduct.PersonalRelatedDataVO;
import com.daimler.data.dto.dataproduct.SubdivisionVO;
import com.daimler.data.dto.dataproduct.TransnationalDataTransferVO;

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
			if (dataProduct.getContactInformation() != null) {
				ContactInformationVO contactInformationVO = new ContactInformationVO();
				BeanUtils.copyProperties(dataProduct.getContactInformation(), contactInformationVO);
				DivisionVO divisionvo = new DivisionVO();
				Division division = dataProduct.getContactInformation().getDivision();
				if (division != null) {
					BeanUtils.copyProperties(division, divisionvo);
					SubdivisionVO subdivisionVO = new SubdivisionVO();
					if (division.getSubdivision() != null)
						BeanUtils.copyProperties(division.getSubdivision(), subdivisionVO);
					divisionvo.setSubdivision(subdivisionVO);
					contactInformationVO.setDivision(divisionvo);
				}
				vo.setContactInformation(contactInformationVO);
			}

			if (dataProduct.getClassificationConfidentiality() != null) {
				ClassificationConfidentialityVO classificationConfidentialityVO = new ClassificationConfidentialityVO();
				BeanUtils.copyProperties(dataProduct.getClassificationConfidentiality(),
						classificationConfidentialityVO);
				vo.setClassificationConfidentiality(classificationConfidentialityVO);
			}

			if (dataProduct.getPersonalRelatedData() != null) {
				PersonalRelatedDataVO personalRelatedDataVO = new PersonalRelatedDataVO();
				BeanUtils.copyProperties(dataProduct.getPersonalRelatedData(), personalRelatedDataVO);
				vo.setPersonalRelatedData(personalRelatedDataVO);
			}

			if (dataProduct.getTransnationalDataTransfer() != null) {
				TransnationalDataTransferVO transnationalDataTransferVO = new TransnationalDataTransferVO();
				BeanUtils.copyProperties(dataProduct.getTransnationalDataTransfer(), transnationalDataTransferVO);
				vo.setTransnationalDataTransfer(transnationalDataTransferVO);
			}

			if (dataProduct.getDeletionRequirement() != null) {
				DeletionRequirementVO deletionRequirementVO = new DeletionRequirementVO();
				BeanUtils.copyProperties(dataProduct.getDeletionRequirement(), deletionRequirementVO);
				vo.setDeletionRequirement(deletionRequirementVO);
			}
			if (!ObjectUtils.isEmpty(dataProduct.getOpenSegments())) {
				List<DataProductVO.OpenSegmentsEnum> openSegmentsEnumList = new ArrayList<>();
				dataProduct.getOpenSegments().forEach(
						openSegment -> openSegmentsEnumList.add(DataProductVO.OpenSegmentsEnum.valueOf(openSegment)));
				vo.setOpenSegments(openSegmentsEnumList);
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
			dataProduct.setDataFromChina(vo.isDataFromChina());
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

			if (vo.getContactInformation() != null) {
				ContactInformation contactInformation = new ContactInformation();
				BeanUtils.copyProperties(vo.getContactInformation(), contactInformation);
				DivisionVO divisionvo = vo.getContactInformation().getDivision();
				Division division = new Division();
				if (divisionvo != null) {
					BeanUtils.copyProperties(divisionvo, division);
					Subdivision subdivision = new Subdivision();
					if (divisionvo.getSubdivision() != null)
						BeanUtils.copyProperties(divisionvo.getSubdivision(), subdivision);
					division.setSubdivision(subdivision);
					contactInformation.setDivision(division);
				}
				dataProduct.setContactInformation(contactInformation);
			}

			if (vo.getClassificationConfidentiality() != null) {
				ClassificationConfidentiality classificationConfidentiality = new ClassificationConfidentiality();
				BeanUtils.copyProperties(vo.getClassificationConfidentiality(), classificationConfidentiality);
				dataProduct.setClassificationConfidentiality(classificationConfidentiality);
			}

			PersonalRelatedDataVO personalRelatedDataVO = vo.getPersonalRelatedData();
			if (personalRelatedDataVO != null) {
				PersonalRelatedData personalRelatedData = new PersonalRelatedData();
				BeanUtils.copyProperties(personalRelatedDataVO, personalRelatedData);
				personalRelatedData.setPersonalRelatedData(personalRelatedDataVO.isPersonalRelatedData());
				dataProduct.setPersonalRelatedData(personalRelatedData);
			}

			TransnationalDataTransferVO transnationalDataTransferVO = vo.getTransnationalDataTransfer();
			if (transnationalDataTransferVO != null) {
				TransnationalDataTransfer transnationalDataTransfer = new TransnationalDataTransfer();
				transnationalDataTransfer.setDataTransferred(transnationalDataTransferVO.isDataTransferred());
				transnationalDataTransfer.setNotWithinEU(transnationalDataTransferVO.isNotWithinEU());
				transnationalDataTransfer.setApproved(transnationalDataTransferVO.getApproved());
				dataProduct.setTransnationalDataTransfer(transnationalDataTransfer);
			}

			DeletionRequirementVO deletionRequirementVO = vo.getDeletionRequirement();
			if (deletionRequirementVO != null) {
				DeletionRequirement deletionRequirement = new DeletionRequirement();
				deletionRequirement.setDeletionRequirements(deletionRequirementVO.isDeletionRequirements());
				deletionRequirement.setDescription(deletionRequirementVO.getDescription());
				dataProduct.setDeletionRequirement(deletionRequirement);
			}

			if (!ObjectUtils.isEmpty(vo.getOpenSegments())) {
				List<String> openSegmentList = new ArrayList<>();
				vo.getOpenSegments().forEach(openSegmentsEnum -> {
					openSegmentList.add(openSegmentsEnum.name());
				});
				dataProduct.setOpenSegments(openSegmentList);
			}
			entity.setData(dataProduct);
		}

		return entity;
	}

}
