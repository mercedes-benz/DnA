package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.daimler.data.db.entities.DdxDataProductsDetailsNsql;
import com.daimler.data.db.json.DdxDataProductsDetail;
import com.daimler.data.db.json.DdxProduct;
import com.daimler.data.db.json.DdxUnityDetails;
import com.daimler.data.db.json.Fabric2FabricDetail;
import com.daimler.data.db.json.GroupNameDetail;
import com.daimler.data.db.json.GroupNameList;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.DdxPublishedLakeHouseDetailsVO;
import com.daimler.data.dto.fabricWorkspace.DdxUnityDetailsVO;
import com.daimler.data.dto.fabricWorkspace.Fabric2FabricDetailVO;
import com.daimler.data.dto.fabricWorkspace.GroupNameDetailVO;
import com.daimler.data.dto.fabricWorkspace.GroupNameListVO;

@Component
public class DdxDataProductsDetailsAssembler {

    public DdxPublishedLakeHouseDetailsVO toVo(DdxProduct product) {
        if (product == null) return null;
        DdxPublishedLakeHouseDetailsVO vo = new DdxPublishedLakeHouseDetailsVO();

        vo.setWorkspaceName(product.getWorkspaceName());
        vo.setWorkspaceId(product.getWorkspaceId());
        vo.setLakehouseName(product.getLakehouseName());
        vo.setLakeHouseId(product.getLakeHouseId());
        vo.setIsLakeHousesPublishedToDdx(product.getIsLakeHousesPublishedToDdx());
        vo.setProductName(product.getProductName());
        vo.setProductId(product.getProductId());
        vo.setCreatedOn(product.getCreatedOn());
        vo.setModifiedOn(product.getModifiedOn());

        if (!ObjectUtils.isEmpty(product.getCreatedBy())) {
            CreatedByVO createdByVO = new CreatedByVO();
            BeanUtils.copyProperties(product.getCreatedBy(), createdByVO);
            vo.setCreatedBy(createdByVO);
        }

        if (!ObjectUtils.isEmpty(product.getUnityDetails())) {
            DdxUnityDetailsVO unityVO = new DdxUnityDetailsVO();
            BeanUtils.copyProperties(product.getUnityDetails(), unityVO);
            vo.setUnityDetails(unityVO);
        }

        if (!ObjectUtils.isEmpty(product.getFabric2fabricDetails())) {
            List<Fabric2FabricDetailVO> f2fVOs = product.getFabric2fabricDetails().stream()
                .map(this::toFabric2FabricDetailVO)
                .collect(Collectors.toList());
            vo.setFabric2fabricDetails(f2fVOs);
        }

        return vo;
    }

    public DdxProduct toProduct(DdxPublishedLakeHouseDetailsVO vo) {
        if (vo == null) return null;
        DdxProduct product = new DdxProduct();

        product.setWorkspaceName(vo.getWorkspaceName());
        product.setWorkspaceId(vo.getWorkspaceId());
        product.setLakehouseName(vo.getLakehouseName());
        product.setLakeHouseId(vo.getLakeHouseId());
        product.setIsLakeHousesPublishedToDdx(vo.isIsLakeHousesPublishedToDdx());
        product.setProductName(vo.getProductName());
        product.setProductId(vo.getProductId());
        product.setCreatedOn(vo.getCreatedOn());
        product.setModifiedOn(vo.getModifiedOn());

        if (!ObjectUtils.isEmpty(vo.getCreatedBy())) {
            UserDetails userDetails = new UserDetails();
            BeanUtils.copyProperties(vo.getCreatedBy(), userDetails);
            product.setCreatedBy(userDetails);
        }

        if (!ObjectUtils.isEmpty(vo.getUnityDetails())) {
            DdxUnityDetails unityDetails = new DdxUnityDetails();
            BeanUtils.copyProperties(vo.getUnityDetails(), unityDetails);
            product.setUnityDetails(unityDetails);
        }

        if (!ObjectUtils.isEmpty(vo.getFabric2fabricDetails())) {
            List<Fabric2FabricDetail> f2fDetails = vo.getFabric2fabricDetails().stream()
                .map(this::toFabric2FabricDetail)
                .collect(Collectors.toList());
            product.setFabric2fabricDetails(f2fDetails);
        }

        return product;
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
