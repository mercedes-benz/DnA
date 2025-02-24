package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.CodeServerAdditionalServiceNsql;
import com.daimler.data.db.json.CodeServerAdditionalService;
import com.daimler.data.dto.AdditionalPropertiesDto;
import com.daimler.data.dto.EnvironmentVariable;
import com.daimler.data.dto.Port;
import com.daimler.data.dto.SecurityContext;
import com.daimler.data.dto.VolumeMount;
import com.daimler.data.dto.workspace.recipe.AdditionalPropertiesVO;
import com.daimler.data.dto.workspace.recipe.AdditionalServiceLovVo;
import com.daimler.data.dto.workspace.recipe.EnvVO;
import com.daimler.data.dto.workspace.recipe.InitializeAdditionalServiceLovVo;
import com.daimler.data.dto.workspace.recipe.PortVO;
import com.daimler.data.dto.workspace.recipe.SecurityContextVO;
import com.daimler.data.dto.workspace.recipe.VolumeMountsVO;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AdditionalServiceAssembler implements GenericAssembler<AdditionalServiceLovVo, CodeServerAdditionalServiceNsql> {
    
    @Override
    public AdditionalServiceLovVo toVo(CodeServerAdditionalServiceNsql entity) {
        AdditionalServiceLovVo additionalServiceLovVo =  new AdditionalServiceLovVo();
        try{
            List<EnvVO> envVoS = new ArrayList<>();
            List<PortVO> portVOs = new ArrayList<>();
            List<VolumeMountsVO> volumeMountsVO = new ArrayList<>();
            AdditionalPropertiesVO additionalPropertiesVO =  new AdditionalPropertiesVO();
            if(Objects.nonNull(entity.getData())) {
                additionalServiceLovVo.setServiceName(entity.getData().getServiceName());
                additionalServiceLovVo.setVersion(entity.getData().getVersion());
                if(entity.getData().getAdditionalProperties().getEnv() !=null ) { 
                    List<EnvironmentVariable> environmentVariables = entity.getData().getAdditionalProperties().getEnv(); 
                    for(EnvironmentVariable env: environmentVariables){
                        EnvVO envo =  new EnvVO();
                        envo.setName(env.getName());
                        envo.setValue(env.getValue());
                        envVoS.add(envo);
                    }
                    additionalPropertiesVO.setEnv(envVoS);
                }
                if(entity.getData().getAdditionalProperties().getArgs()!=null) {
                    List<String> args = entity.getData().getAdditionalProperties().getArgs();
                    additionalPropertiesVO.setArgs(args);
                 }
                if(entity.getData().getAdditionalProperties().getPorts()!=null) {
                    List<Port> ports = entity.getData().getAdditionalProperties().getPorts();
                    for(Port port:ports) {
                        PortVO portVO = new PortVO();
                        portVO.setContainerPort(port.getContainerPort());
                        portVO.setProtocol(port.getProtocol());
                        portVOs.add(portVO);
                    }
                    additionalPropertiesVO.setPorts(portVOs);
                }
                if(entity.getData().getAdditionalProperties().getSecurityContext()!=null) {
                    SecurityContext securityContexts = entity.getData().getAdditionalProperties().getSecurityContext();
                    SecurityContextVO securityVo = new SecurityContextVO();
                    securityVo.setRunAsUser(securityContexts.getRunAsUser());
                    additionalPropertiesVO.setSecurityContext(securityVo);

                }
                if(entity.getData().getAdditionalProperties().getVolumeMounts()!=null){
                    List<VolumeMount> volumeMounts = entity.getData().getAdditionalProperties().getVolumeMounts();
                    for(VolumeMount volmounts: volumeMounts){
                        VolumeMountsVO volMountVo = new VolumeMountsVO();
                        volMountVo.setName(volmounts.getName());
                        volMountVo.setMountPath(volmounts.getMountPath());
                        volumeMountsVO.add(volMountVo);
                    }
                    additionalPropertiesVO.setVolumeMounts(volumeMountsVO);
                }if(entity.getData().getAdditionalProperties().getImage()!=null) {
                    additionalPropertiesVO.setImage(entity.getData().getAdditionalProperties().getImage());
                }
                if(entity.getData().getAdditionalProperties().getName()!=null) {
                    additionalPropertiesVO.setName(entity.getData().getAdditionalProperties().getName());

                }
                if(entity.getData().getAdditionalProperties().getImagePullPolicy()!=null) {
                    additionalPropertiesVO.setImagePullPolicy(entity.getData().getAdditionalProperties().getImagePullPolicy());
                }
                additionalServiceLovVo.setAdditionalProperties(additionalPropertiesVO);
            }
            additionalServiceLovVo.setCreatedOn(entity.getData().getCreatedOn());
            additionalServiceLovVo.setCreatedBy(entity.getData().getCreatedBy());
            additionalServiceLovVo.setUpdatedOn(entity.getData().getUpdatedOn());
            additionalServiceLovVo.setUpdatedBy(entity.getData().getUpdatedBy());
        
        }
        catch(Exception e){
            log.error("Exception in Assembler ",e);
        }
		return additionalServiceLovVo;
    }

    @Override
    public CodeServerAdditionalServiceNsql toEntity(AdditionalServiceLovVo vo) {
        CodeServerAdditionalServiceNsql entity = new CodeServerAdditionalServiceNsql();
        if (Objects.nonNull(vo)) {
            CodeServerAdditionalService data = new CodeServerAdditionalService();
            AdditionalPropertiesDto additionalProperties = new AdditionalPropertiesDto();

            data.setServiceName(vo.getServiceName());
            data.setVersion(vo.getVersion());

            if (Objects.nonNull(vo.getAdditionalProperties())) {
                AdditionalPropertiesVO propertiesVO = (AdditionalPropertiesVO) vo.getAdditionalProperties();

                if (Objects.nonNull(propertiesVO.getEnv())) {
                    List<EnvironmentVariable> envList = new ArrayList<>();
                    for (EnvVO envVo : propertiesVO.getEnv()) {
                        EnvironmentVariable env = new EnvironmentVariable();
                        env.setName(envVo.getName());
                        env.setValue(envVo.getValue());
                        envList.add(env);
                    }
                    additionalProperties.setEnv(envList);
                }

                additionalProperties.setArgs(propertiesVO.getArgs());

                if (Objects.nonNull(propertiesVO.getPorts())) {
                    List<Port> portList = new ArrayList<>();
                    for (PortVO portVo : propertiesVO.getPorts()) {
                        Port port = new Port();
                        port.setContainerPort(portVo.getContainerPort());
                        port.setProtocol(portVo.getProtocol());
                        portList.add(port);
                    }
                    additionalProperties.setPorts(portList);
                }

                if (Objects.nonNull(propertiesVO.getVolumeMounts())) {
                    List<VolumeMount> volumeMountList = new ArrayList<>();
                    for (VolumeMountsVO volMountVo : propertiesVO.getVolumeMounts()) {
                        VolumeMount volumeMount = new VolumeMount();
                        volumeMount.setName(volMountVo.getName());
                        volumeMount.setMountPath(volMountVo.getMountPath());
                        volumeMountList.add(volumeMount);
                    }
                    additionalProperties.setVolumeMounts(volumeMountList);
                }

                if (Objects.nonNull(propertiesVO.getSecurityContext())) {
                    SecurityContext securityContext = new SecurityContext();
                    securityContext.setRunAsUser(propertiesVO.getSecurityContext().getRunAsUser());
                    additionalProperties.setSecurityContext(securityContext);
                }

                additionalProperties.setImage(propertiesVO.getImage());
                additionalProperties.setName(propertiesVO.getName());
                additionalProperties.setImagePullPolicy(propertiesVO.getImagePullPolicy());

                data.setAdditionalProperties(additionalProperties);
            }

            data.setCreatedOn(vo.getCreatedOn());
            data.setCreatedBy(vo.getCreatedBy());
            data.setUpdatedOn(vo.getUpdatedOn());
            data.setUpdatedBy(vo.getUpdatedBy());

            entity.setId(vo.getId()); 
            entity.setData(data);
        }
        return entity;
    }

}
