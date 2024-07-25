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
                    EnvVO envo =  new EnvVO();
                    for(EnvironmentVariable env: environmentVariables){
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
                    PortVO portVO = new PortVO();
                    for(Port port:ports) {
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
                    VolumeMountsVO volMountVo = new VolumeMountsVO();
                    for(VolumeMount volmounts: volumeMounts){
                        volMountVo.setName(volmounts.getName());
                        volMountVo.setMountPath(volmounts.getMountPath());
                        volumeMountsVO.add(volMountVo);
                    }
                    additionalPropertiesVO.setVolumeMounts(volumeMountsVO);
                }if(entity.getData().getAdditionalProperties().getImage()!=null) {
                    additionalPropertiesVO.setImage(entity.getData().getAdditionalProperties().getImage());
                }
                if(entity.getData().getAdditionalProperties().getImagePullPolicy()!=null) {
                    additionalPropertiesVO.setImagePullPolicy(entity.getData().getAdditionalProperties().getImagePullPolicy());
                }
                additionalServiceLovVo.setAdditionalProperties(additionalPropertiesVO);
            }
        
        }
        catch(Exception e){
            e.printStackTrace();
        }
		return additionalServiceLovVo;
    }

    @Override
    public CodeServerAdditionalServiceNsql toEntity(AdditionalServiceLovVo vo) {
        CodeServerAdditionalServiceNsql entity = new CodeServerAdditionalServiceNsql();
        AdditionalPropertiesDto additionalPropertiesDto =  new AdditionalPropertiesDto();
        CodeServerAdditionalService data =  new CodeServerAdditionalService();
        if(Objects.nonNull(vo)) {
            //To implement for entity creation
            data.setAdditionalProperties(additionalPropertiesDto);
            entity.setData(data);
        }
        return entity;
    }

}
