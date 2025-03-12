package com.daimler.data.assembler;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
import com.daimler.data.db.json.CodeServerBuildDeploy;
import com.daimler.data.db.json.DeploymentAudit;
import com.daimler.data.dto.workspace.buildDeploy.DeploymentAuditVO;
import com.daimler.data.dto.workspace.buildDeploy.BuildAuditVO;
import com.daimler.data.dto.workspace.buildDeploy.CodeServerBuildDeployVO;
import com.daimler.data.db.json.BuildAudit;

@Slf4j
@Component
public class BuildDeployAssembler implements GenericAssembler<CodeServerBuildDeployVO, CodeServerBuildDeployNsql> {

    private List<BuildAuditVO> toBuildAuditVO(List<BuildAudit> buildAuditLogs) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
        List<BuildAuditVO> auditDetailsVO = new ArrayList<>();
        try {
            if (buildAuditLogs != null && !buildAuditLogs.isEmpty()) {
                for (BuildAudit audit : buildAuditLogs) {
                    BuildAuditVO auditDetails = new BuildAuditVO();
                    auditDetails.setBuildStatus(audit.getBuildStatus());
                    if (Objects.nonNull(audit.getBuildOn())) {
                        auditDetails.setBuildOn(isoFormat.parse(isoFormat.format(audit.getBuildOn())));
                    }
                    auditDetails.setTriggeredBy(audit.getTriggeredBy());
                    if (Objects.nonNull(audit.getTriggeredOn())) {
                        auditDetails.setTriggeredOn(isoFormat.parse(isoFormat.format(audit.getTriggeredBy())));
                    }
                    auditDetails.setBranch(audit.getBranch());
                    auditDetails.setVersion(audit.getVersion());
                    auditDetails.setComments(audit.getComments());
                    auditDetailsVO.add(auditDetails);
                }
            }
        } catch (Exception e) {
            log.error("Failed in assembler while parsing date into iso format with exception {}", e);
        }

        return auditDetailsVO;
    }

    private List<BuildAudit> toBuildAudit(List<BuildAuditVO> auditdetails) {
        List<BuildAudit> buildAuditLogDetails = new ArrayList<>();
        try {
            if (auditdetails != null && !auditdetails.isEmpty()) {
                for (BuildAuditVO audit : auditdetails) {
                    BuildAudit auditDetails = new BuildAudit();
                    auditDetails.setBuildStatus(audit.getBuildStatus());
					 if(Objects.nonNull(audit.getBuildOn())){
						 auditDetails.setBuildOn(audit.getBuildOn());
					 }
					 auditDetails.setTriggeredBy(audit.getTriggeredBy());
					 if(Objects.nonNull(audit.getTriggeredOn())){
						 auditDetails.setTriggeredOn(audit.getTriggeredOn());
					 }
					 auditDetails.setBranch(audit.getBranch());
					 auditDetails.setVersion(audit.getVersion());
                     auditDetails.setComments(audit.getComments());
					 buildAuditLogDetails.add(auditDetails);
                }
            }

        } catch (Exception e) {
            log.error("Failed while parsing in assembler");
        }
        return buildAuditLogDetails;
    }

    private List<DeploymentAuditVO> toDeploymentAuditVO(List<DeploymentAudit> deploymentAuditLogs) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
        List<DeploymentAuditVO> auditDetailsVO = new ArrayList<>();
        try {
            if (deploymentAuditLogs != null && !deploymentAuditLogs.isEmpty()) {
                for (DeploymentAudit audit : deploymentAuditLogs) {
                    DeploymentAuditVO auditDetails = new DeploymentAuditVO();
                    auditDetails.setDeploymentStatus(audit.getDeploymentStatus());
                    if (Objects.nonNull(audit.getDeployedOn()))
                        auditDetails.setDeployedOn(isoFormat.parse(isoFormat.format(audit.getDeployedOn())));
                    auditDetails.setTriggeredBy(audit.getTriggeredBy());
                    if (Objects.nonNull(audit.getTriggeredOn()))
                        auditDetails.setTriggeredOn(isoFormat.parse(isoFormat.format(audit.getTriggeredOn())));
                    auditDetails.setBranch(audit.getBranch());
                    auditDetails.setCommitId(audit.getCommitId());
                    auditDetailsVO.add(auditDetails);
                }
            }
        } catch (ParseException e) {
            log.error("Failed in assembler  while parsing date into iso format with exception {} in auditDeatils");
        }
        return auditDetailsVO;
    }

    private List<DeploymentAudit> toDeploymentAudit(List<DeploymentAuditVO> auditdetails)
	 {
		 List<DeploymentAudit> deployedAuditLogDetails = new ArrayList<>();
		 try
		 {
			 if(auditdetails != null && !auditdetails.isEmpty())
			 {
				 for(DeploymentAuditVO audit: auditdetails)
				 { 
					 DeploymentAudit auditDetails = new DeploymentAudit();
					 auditDetails.setDeploymentStatus(audit.getDeploymentStatus());
					 if(Objects.nonNull(audit.getDeployedOn())){
						 auditDetails.setDeployedOn(audit.getDeployedOn());
					 }
					 auditDetails.setTriggeredBy(audit.getTriggeredBy());
					 if(Objects.nonNull(audit.getTriggeredOn())){
						 auditDetails.setTriggeredOn(audit.getTriggeredOn());
					 }
					 auditDetails.setBranch(audit.getBranch());
					 auditDetails.setCommitId(audit.getCommitId());
					 deployedAuditLogDetails.add(auditDetails);
				 }
			 }
		 }
		 catch(Exception e)
		 {
			 log.error("Failed while parsing in assembler");
		 }
		 return deployedAuditLogDetails;
	 }

    @Override
    public CodeServerBuildDeployVO toVo(CodeServerBuildDeployNsql entity) {
        CodeServerBuildDeployVO vo = new CodeServerBuildDeployVO();

        try {
            if (entity != null) {
                CodeServerBuildDeploy data = entity.getData();
                if (data != null) {
                    BeanUtils.copyProperties(data, vo);
                    if (data.getIntBuildAuditLogs() != null && !data.getIntBuildAuditLogs().isEmpty()) {
                        List<BuildAuditVO> auditDetails = this.toBuildAuditVO(data.getIntBuildAuditLogs());
                        vo.setIntBuildAuditLogs(auditDetails);
                    }
                    if (data.getProdBuildAuditLogs() != null && !data.getProdBuildAuditLogs().isEmpty()) {
                        List<BuildAuditVO> auditDetails = this.toBuildAuditVO(data.getProdBuildAuditLogs());
                        vo.setProdBuildAuditLogs(auditDetails);
                    }
                    if (data.getIntDeploymentAuditLogs() != null && !data.getIntDeploymentAuditLogs().isEmpty()) {
                        List<DeploymentAuditVO> auditDetails = this
                                .toDeploymentAuditVO(data.getIntDeploymentAuditLogs());
                        vo.setIntDeploymentAuditLogs(auditDetails);
                    }
                    if (data.getProdDeploymentAuditLogs() != null && !data.getProdDeploymentAuditLogs().isEmpty()) {
                        List<DeploymentAuditVO> auditDetails = this
                                .toDeploymentAuditVO(data.getProdDeploymentAuditLogs());
                        vo.setProdDeploymentAuditLogs(auditDetails);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed in assembler while parsing date into iso format with exception {}", e);
        }

        return vo;
    }

    @Override
    public CodeServerBuildDeployNsql toEntity(CodeServerBuildDeployVO vo) {
        CodeServerBuildDeployNsql entity = null;
        if (vo != null) {
            entity = new CodeServerBuildDeployNsql();
            entity.setId(vo.getId());
            CodeServerBuildDeploy data = new CodeServerBuildDeploy();
            BeanUtils.copyProperties(vo, data);
            if(vo.getProdBuildAuditLogs() != null){
                List<BuildAudit> auditDetails = this.toBuildAudit(vo.getProdBuildAuditLogs());
                data.setProdBuildAuditLogs(auditDetails);
            }
            if(vo.getIntBuildAuditLogs() != null){
                List<BuildAudit> auditDetails = this.toBuildAudit(vo.getIntBuildAuditLogs());
                data.setIntBuildAuditLogs(auditDetails);
            }
            if(vo.getIntDeploymentAuditLogs() != null){
                List<DeploymentAudit> auditDetails = this.toDeploymentAudit(vo.getIntDeploymentAuditLogs());
                data.setIntDeploymentAuditLogs(auditDetails);
            }
            if(vo.getProdDeploymentAuditLogs() != null){
                List<DeploymentAudit> auditDetails = this.toDeploymentAudit(vo.getProdDeploymentAuditLogs());
                data.setProdDeploymentAuditLogs(auditDetails);
            }
            entity.setData(data);

        }
        return entity;
    }

}
