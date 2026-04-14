package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.daimler.data.db.entities.LakehouseDetailNsql;
import com.daimler.data.db.json.DdxUnityDetails;
import com.daimler.data.db.json.Fabric2FabricDetail;
import com.daimler.data.db.json.GroupNameDetail;
import com.daimler.data.db.json.GroupNameList;
import com.daimler.data.db.json.LakehouseDetail;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.DdxPublishedLakeHouseDetailsVO;
import com.daimler.data.dto.fabricWorkspace.DdxUnityDetailsVO;
import com.daimler.data.dto.fabricWorkspace.Fabric2FabricDetailVO;
import com.daimler.data.dto.fabricWorkspace.GroupNameDetailVO;
import com.daimler.data.dto.fabricWorkspace.GroupNameListVO;

@Component
public class LakehouseDetailAssembler implements GenericAssembler<DdxPublishedLakeHouseDetailsVO, LakehouseDetailNsql> {

    @Override
    public DdxPublishedLakeHouseDetailsVO toVo(LakehouseDetailNsql entity) {
        if (entity == null) return null;
        DdxPublishedLakeHouseDetailsVO vo = new DdxPublishedLakeHouseDetailsVO();
        LakehouseDetail data = entity.getData();
        if (data != null) {
            vo.setWorkspaceName(data.getWorkspaceName());
            vo.setWorkspaceId(data.getWorkspaceId());
            vo.setLakehouseName(data.getLakehouseName());
            vo.setLakeHouseId(data.getLakeHouseId());
            vo.setIsLakeHousesPublishedToDdx(data.getIsLakeHousesPublishedToDdx());
            vo.setProductName(data.getProductName());
            vo.setProductId(data.getProductId());
            vo.setCreatedOn(data.getCreatedOn());
            vo.setModifiedOn(data.getModifiedOn());

            if (!ObjectUtils.isEmpty(data.getCreatedBy())) {
                CreatedByVO createdByVO = new CreatedByVO();
                BeanUtils.copyProperties(data.getCreatedBy(), createdByVO);
                vo.setCreatedBy(createdByVO);
            }

            if (!ObjectUtils.isEmpty(data.getUnityDetails())) {
                DdxUnityDetailsVO unityVO = new DdxUnityDetailsVO();
                BeanUtils.copyProperties(data.getUnityDetails(), unityVO);
                vo.setUnityDetails(unityVO);
            }

            if (!ObjectUtils.isEmpty(data.getFabric2fabricDetails())) {
                List<Fabric2FabricDetailVO> f2fVOs = data.getFabric2fabricDetails().stream()
                    .map(this::toFabric2FabricDetailVO)
                    .collect(Collectors.toList());
                vo.setFabric2fabricDetails(f2fVOs);
            }
        }
        return vo;
    }

    @Override
    public LakehouseDetailNsql toEntity(DdxPublishedLakeHouseDetailsVO vo) {
        if (vo == null) return null;
        LakehouseDetailNsql entity = new LakehouseDetailNsql();
        LakehouseDetail data = new LakehouseDetail();

        data.setWorkspaceName(vo.getWorkspaceName());
        data.setWorkspaceId(vo.getWorkspaceId());
        data.setLakehouseName(vo.getLakehouseName());
        data.setLakeHouseId(vo.getLakeHouseId());
        data.setIsLakeHousesPublishedToDdx(vo.isIsLakeHousesPublishedToDdx());
        data.setProductName(vo.getProductName());
        data.setProductId(vo.getProductId());
        data.setCreatedOn(vo.getCreatedOn());
        data.setModifiedOn(vo.getModifiedOn());

        if (!ObjectUtils.isEmpty(vo.getCreatedBy())) {
            UserDetails userDetails = new UserDetails();
            BeanUtils.copyProperties(vo.getCreatedBy(), userDetails);
            data.setCreatedBy(userDetails);
        }

        if (!ObjectUtils.isEmpty(vo.getUnityDetails())) {
            DdxUnityDetails unityDetails = new DdxUnityDetails();
            BeanUtils.copyProperties(vo.getUnityDetails(), unityDetails);
            data.setUnityDetails(unityDetails);
        }

        if (!ObjectUtils.isEmpty(vo.getFabric2fabricDetails())) {
            List<Fabric2FabricDetail> f2fDetails = vo.getFabric2fabricDetails().stream()
                .map(this::toFabric2FabricDetail)
                .collect(Collectors.toList());
            data.setFabric2fabricDetails(f2fDetails);
        }

        entity.setData(data);
        return entity;
    }

    private Fabric2FabricDetailVO toFabric2FabricDetailVO(Fabric2FabricDetail detail) {
        Fabric2FabricDetailVO vo = new Fabric2FabricDetailVO();
        vo.setIsFabric2Fabric(detail.getIsFabric2Fabric());
        vo.setInitiatedOn(detail.getInitiatedOn());
        if (!ObjectUtils.isEmpty(detail.getGroupsNames())) {
            List<GroupNameDetailVO> groupVOs = detail.getGroupsNames().stream()
                .map(this::toGroupNameDetailVO)
                .collect(Collectors.toList());
            vo.setGroupsNames(groupVOs);
        }
        return vo;
    }

    private Fabric2FabricDetail toFabric2FabricDetail(Fabric2FabricDetailVO vo) {
        Fabric2FabricDetail detail = new Fabric2FabricDetail();
        detail.setIsFabric2Fabric(vo.isIsFabric2Fabric());
        detail.setInitiatedOn(vo.getInitiatedOn());
        if (!ObjectUtils.isEmpty(vo.getGroupsNames())) {
            List<GroupNameDetail> groups = vo.getGroupsNames().stream()
                .map(this::toGroupNameDetail)
                .collect(Collectors.toList());
            detail.setGroupsNames(groups);
        }
        return detail;
    }

    private GroupNameDetailVO toGroupNameDetailVO(GroupNameDetail detail) {
        GroupNameDetailVO vo = new GroupNameDetailVO();
        vo.setTestRunId(detail.getTestRunId());
        vo.setRunStatus(detail.getRunStatus());
        if (!ObjectUtils.isEmpty(detail.getGroupNameList())) {
            List<GroupNameListVO> listVOs = detail.getGroupNameList().stream()
                .map(this::toGroupNameListVO)
                .collect(Collectors.toList());
            vo.setGroupNameList(listVOs);
        }
        return vo;
    }

    private GroupNameDetail toGroupNameDetail(GroupNameDetailVO vo) {
        GroupNameDetail detail = new GroupNameDetail();
        detail.setTestRunId(vo.getTestRunId());
        detail.setRunStatus(vo.getRunStatus());
        if (!ObjectUtils.isEmpty(vo.getGroupNameList())) {
            List<GroupNameList> list = vo.getGroupNameList().stream()
                .map(this::toGroupNameList)
                .collect(Collectors.toList());
            detail.setGroupNameList(list);
        }
        return detail;
    }

    private GroupNameListVO toGroupNameListVO(GroupNameList item) {
        GroupNameListVO vo = new GroupNameListVO();
        vo.setGroupName(item.getGroupName());
        vo.setStatus(item.getStatus());
        vo.setMessage(item.getMessage());
        return vo;
    }

    private GroupNameList toGroupNameList(GroupNameListVO vo) {
        GroupNameList item = new GroupNameList();
        item.setGroupName(vo.getGroupName());
        item.setStatus(vo.getStatus());
        item.setMessage(vo.getMessage());
        return item;
    }
}
