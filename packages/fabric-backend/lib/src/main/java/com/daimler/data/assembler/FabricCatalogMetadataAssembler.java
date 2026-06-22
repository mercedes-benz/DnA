package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.beans.BeanUtils;

import com.daimler.data.db.entities.FabricCatalogMetadataNsql;
import com.daimler.data.db.json.catalogManangement.CdcTableDetail;
import com.daimler.data.db.json.catalogManangement.FabricCatalogMetadataDetails;
import com.daimler.data.db.json.catalogManangement.MandatoryFields;
import com.daimler.data.db.json.catalogManangement.LakehouseTableDetail;
import com.daimler.data.db.json.catalogManangement.LakehouseColumnDetail;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.dto.fabricCatalogManagement.CdcTableDetailVO;
import com.daimler.data.dto.fabricCatalogManagement.FabricCatalogMetadataDetailsVO;
import com.daimler.data.dto.fabricCatalogManagement.LakehouseTableDetailVO;
import com.daimler.data.dto.fabricCatalogManagement.LakehouseColumnDetailVO;
import com.daimler.data.dto.fabricCatalogManagement.MandatoryFieldsVO;
import com.daimler.data.dto.fabricCatalogManagement.MandatoryFieldsVO.DivisionsEnum;
// import com.daimler.data.dto.fabricCatalogManagement.MandatoryFieldsVO.DataOriginEnum;
import com.daimler.data.dto.fabricCatalogManagement.MandatoryFieldsVO.IsDocumentationUpdatedEnum;
// import com.daimler.data.dto.fabricCatalogManagement.MandatoryFieldsVO.DataLakeNameEnum;
import com.daimler.data.dto.fabricCatalogManagement.MandatoryFieldsVO.DataConfidentialityEnum;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;


@Component
public class FabricCatalogMetadataAssembler implements GenericAssembler<FabricCatalogMetadataDetailsVO, FabricCatalogMetadataNsql> {

    public FabricCatalogMetadataNsql toEntity(FabricCatalogMetadataDetailsVO vo) {
        FabricCatalogMetadataNsql entity = new FabricCatalogMetadataNsql();
        FabricCatalogMetadataDetails data = new FabricCatalogMetadataDetails();
        if (vo != null) {
            if (vo.getId() != null) {
                entity.setId(vo.getId());
            }

            if (vo.getPublishedCDCCatalogs() != null) {
                List<CdcTableDetail> cdcTableDetails = new ArrayList<>();
                for (CdcTableDetailVO cdcVo : vo.getPublishedCDCCatalogs()) {
                    cdcTableDetails.add(toCdcTableDetail(cdcVo));
                }
                data.setPublishedCDCCatalogs(cdcTableDetails);
            }
        }
        entity.setData(data);
        return entity;
    }

    public FabricCatalogMetadataDetailsVO toVo(FabricCatalogMetadataNsql entity) {
        FabricCatalogMetadataDetailsVO vo = new FabricCatalogMetadataDetailsVO();

        if (entity != null) {
            if (entity.getId() != null) {
                vo.setId(entity.getId());
            }
            FabricCatalogMetadataDetails metadataDetails = entity.getData();
            if (metadataDetails != null) {
                if (metadataDetails.getPublishedCDCCatalogs() != null) {
                    List<CdcTableDetailVO> cdcTableVos = new ArrayList<>();
                    for (CdcTableDetail cdcDetail : metadataDetails.getPublishedCDCCatalogs()) {
                        cdcTableVos.add(toCdcTableDetailVO(
                            cdcDetail,
                            metadataDetails.getMandatoryFields(),
                            metadataDetails.getPublishedLakehouseTables(),
                            metadataDetails.getPublishedLakehouseTableDetails()));
                    }
                    vo.setPublishedCDCCatalogs(cdcTableVos);
                }
            }
        }
        return vo;
    }

    public CdcTableDetailVO toCdcTableDetailVO(CdcTableDetail detail) {
        return toCdcTableDetailVO(detail, null, null, null);
    }

    public CdcTableDetailVO toCdcTableDetailVO(CdcTableDetail detail,
            MandatoryFields legacyMandatoryFields,
            List<String> legacyPublishedLakehouseTables,
            List<LakehouseTableDetail> legacyPublishedLakehouseTableDetails) {
        CdcTableDetailVO vo = new CdcTableDetailVO();
        if (detail != null) {
            vo.setWorkspaceName(detail.getWorkspaceName());
            vo.setWorkspaceId(detail.getWorkspaceId());
            vo.setLakehouseName(detail.getLakehouseName());
            vo.setLakeHouseId(detail.getLakeHouseId());
            vo.setIsLakeHousesPublishedToCdc(detail.getIsLakeHousesPublishedToCdc());
            vo.setMandatoryFields(toMandatoryFieldsVO(
                detail.getMandatoryFields() != null ? detail.getMandatoryFields() : legacyMandatoryFields));
            vo.setPublishedLakehouseTables(detail.getPublishedLakehouseTables() != null
                ? new ArrayList<>(detail.getPublishedLakehouseTables())
                : copyPublishedLakehouseTables(legacyPublishedLakehouseTables));
            vo.setPublishedLakehouseTableDetails(toLakehouseTableDetailVOs(
                detail.getPublishedLakehouseTableDetails() != null
                    ? detail.getPublishedLakehouseTableDetails()
                    : legacyPublishedLakehouseTableDetails));
            vo.setCreatedOn(detail.getCreatedOn());
            vo.setModifiedOn(detail.getModifiedOn());
            if (detail.getCreatedBy() != null) {
                vo.setCreatedBy(toCreatedByVO(detail.getCreatedBy()));
            }
        }
        return vo;
    }

    public CdcTableDetail toCdcTableDetail(CdcTableDetailVO vo) {
        CdcTableDetail detail = new CdcTableDetail();
        if (vo != null) {
            detail.setWorkspaceName(vo.getWorkspaceName());
            detail.setWorkspaceId(vo.getWorkspaceId());
            detail.setLakehouseName(vo.getLakehouseName());
            detail.setLakeHouseId(vo.getLakeHouseId());
            detail.setIsLakeHousesPublishedToCdc(vo.isIsLakeHousesPublishedToCdc());
            detail.setMandatoryFields(toMandatoryFields(vo.getMandatoryFields()));
            detail.setPublishedLakehouseTables(copyPublishedLakehouseTables(vo.getPublishedLakehouseTables()));
            detail.setPublishedLakehouseTableDetails(toLakehouseTableDetails(vo.getPublishedLakehouseTableDetails()));
            detail.setCreatedOn(vo.getCreatedOn());
            detail.setModifiedOn(vo.getModifiedOn());
            if (vo.getCreatedBy() != null) {
                detail.setCreatedBy(toUserDetails(vo.getCreatedBy()));
            }
        }
        return detail;
    }

    private MandatoryFields toMandatoryFields(MandatoryFieldsVO voMandatoryFields) {
        if (voMandatoryFields == null) {
            return null;
        }

        MandatoryFields mandatoryFields = new MandatoryFields();
        mandatoryFields.setDivisions(voMandatoryFields.getDivisions() != null
            ? voMandatoryFields.getDivisions().stream().map(Enum::name).collect(Collectors.toList())
            : null);
        mandatoryFields.setDepartment(voMandatoryFields.getDepartment());
        mandatoryFields.setLeanIXId(voMandatoryFields.getLeanIXId());
        mandatoryFields.setIsDocumentationUpdated(voMandatoryFields.getIsDocumentationUpdated() != null
            ? voMandatoryFields.getIsDocumentationUpdated().name()
            : null);
        mandatoryFields.setDataConfidentiality(voMandatoryFields.getDataConfidentiality() != null
            ? voMandatoryFields.getDataConfidentiality().name()
            : null);
        mandatoryFields.setTier(voMandatoryFields.getTier());
        return mandatoryFields;
    }

    private MandatoryFieldsVO toMandatoryFieldsVO(MandatoryFields mandatoryFields) {
        if (mandatoryFields == null) {
            return null;
        }

        MandatoryFieldsVO mandatoryFieldsVO = new MandatoryFieldsVO();
        mandatoryFieldsVO.setDivisions(mandatoryFields.getDivisions() != null
            ? mandatoryFields.getDivisions().stream().map(DivisionsEnum::valueOf).collect(Collectors.toList())
            : null);
        mandatoryFieldsVO.setDepartment(mandatoryFields.getDepartment());
        mandatoryFieldsVO.setLeanIXId(mandatoryFields.getLeanIXId());
        mandatoryFieldsVO.setIsDocumentationUpdated(mandatoryFields.getIsDocumentationUpdated() != null
            ? IsDocumentationUpdatedEnum.valueOf(mandatoryFields.getIsDocumentationUpdated())
            : null);
        mandatoryFieldsVO.setDataConfidentiality(mandatoryFields.getDataConfidentiality() != null
            ? DataConfidentialityEnum.valueOf(mandatoryFields.getDataConfidentiality())
            : null);
        mandatoryFieldsVO.setTier(mandatoryFields.getTier());
        return mandatoryFieldsVO;
    }

    private List<String> copyPublishedLakehouseTables(List<String> publishedLakehouseTables) {
        return publishedLakehouseTables == null ? null : new ArrayList<>(publishedLakehouseTables);
    }

    private List<LakehouseTableDetail> toLakehouseTableDetails(List<LakehouseTableDetailVO> tableVos) {
        if (tableVos == null) {
            return null;
        }

        List<LakehouseTableDetail> tableDetails = new ArrayList<>();
        for (LakehouseTableDetailVO tableVo : tableVos) {
            if (tableVo == null) {
                continue;
            }

            LakehouseTableDetail tableDetail = new LakehouseTableDetail();
            tableDetail.setTableName(tableVo.getTableName());
            tableDetail.setEnabled(tableVo.isEnabled());

            if (tableVo.getColumns() != null) {
                List<LakehouseColumnDetail> columnDetails = new ArrayList<>();
                for (LakehouseColumnDetailVO colVo : tableVo.getColumns()) {
                    if (colVo == null) {
                        continue;
                    }

                    LakehouseColumnDetail colDetail = new LakehouseColumnDetail();
                    colDetail.setColumnName(colVo.getColumnName());
                    colDetail.setColType(colVo.getColType());
                    colDetail.setEnabled(colVo.isEnabled());
                    columnDetails.add(colDetail);
                }
                tableDetail.setColumns(columnDetails);
            }
            tableDetails.add(tableDetail);
        }
        return tableDetails;
    }

    private List<LakehouseTableDetailVO> toLakehouseTableDetailVOs(List<LakehouseTableDetail> tableDetails) {
        if (tableDetails == null) {
            return null;
        }

        List<LakehouseTableDetailVO> tableDetailVos = new ArrayList<>();
        for (LakehouseTableDetail tableDetail : tableDetails) {
            if (tableDetail == null) {
                continue;
            }

            LakehouseTableDetailVO tableVo = new LakehouseTableDetailVO();
            tableVo.setTableName(tableDetail.getTableName());
            tableVo.setEnabled(Boolean.TRUE.equals(tableDetail.getEnabled()));

            if (tableDetail.getColumns() != null) {
                List<LakehouseColumnDetailVO> columnVos = new ArrayList<>();
                for (LakehouseColumnDetail colDetail : tableDetail.getColumns()) {
                    if (colDetail == null) {
                        continue;
                    }

                    LakehouseColumnDetailVO colVo = new LakehouseColumnDetailVO();
                    colVo.setColumnName(colDetail.getColumnName());
                    colVo.setColType(colDetail.getColType());
                    colVo.setEnabled(Boolean.TRUE.equals(colDetail.getEnabled()));
                    columnVos.add(colVo);
                }
                tableVo.setColumns(columnVos);
            }
            tableDetailVos.add(tableVo);
        }
        return tableDetailVos;
    }

    public UserDetails toUserDetails(CreatedByVO createdBy) {
		UserDetails userDetails = new UserDetails();
		if (createdBy != null) {
			BeanUtils.copyProperties(createdBy, userDetails);
		}
		return userDetails;
	}

	public CreatedByVO toCreatedByVO(UserDetails userDetails) {
		CreatedByVO createdByVO = new CreatedByVO();
		if (userDetails != null) {
			BeanUtils.copyProperties(userDetails, createdByVO);
		}
		return createdByVO;
	}
}
