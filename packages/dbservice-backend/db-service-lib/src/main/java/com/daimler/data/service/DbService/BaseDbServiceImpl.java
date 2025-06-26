package com.daimler.data.service.DbService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.application.config.VaultConfig;
import com.daimler.data.assembler.DbServiceAssembler;
import com.daimler.data.db.entities.DbServiceNsql;
import com.daimler.data.db.json.DbService;
import com.daimler.data.db.json.UserInfo;
import com.daimler.data.db.repo.DbService.DbServiceCustomRepository;
import com.daimler.data.db.repo.DbService.DbServiceRepository;
import com.daimler.data.dto.dbService.CredentialsVO;
import com.daimler.data.dto.dbService.DbServiceVO;
import com.daimler.data.dto.dbService.GenericMessage;
import com.daimler.data.dto.dbService.InitializeResponseVo;
import com.daimler.data.dto.dbService.MessageDescription;
import com.daimler.data.dto.dbService.UserInfoVO;
import com.daimler.data.service.ArgoCdService;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.common.PasswordService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
 @Slf4j
 @SuppressWarnings(value = "unused")
public class BaseDbServiceImpl extends BaseCommonService<DbServiceVO, DbServiceNsql, String> implements DbServicies {

    @Autowired
    private DbServiceRepository repository;

    @Autowired
    private DbServiceAssembler assembler;

    @Autowired
    private VaultConfig vault;

    @Autowired
    private PasswordService passwordGenerator;

    @Autowired
    private DbServiceCustomRepository dbServiceCustomRepo;

    @Autowired
    private ArgoCdService argoCdService;

    @Value("${dbService.accesskey}")
    private String accesskey;

    @Value("${dbService.backupPassword}")
    private String backupPassword;

    @Value("${dbService.bucketName}")
    private String bucketName;

    @Value("${dbService.dataCatalogPassword}")
    private String dataCatalogPassword;

    @Value("${dbService.debeziumPassword}")
    private String debeziumPassword;

    @Value("${dbService.patroniPassword}")
    private String patroniPassword;

    @Value("${dbService.pgmonPassword}")
    private String pgmonPassword;

    @Value("${dbService.retention}")
    private String retention;

    @Value("${dbService.s3Host}")
    private String s3Host;

    @Value("${dbService.s3secretkey}")
    private String s3secretkey;

    @Value("${dbService.size}")
    private String size;

    @Value("${dbService.initDbTemplate}")
    private String initDbTemplate;


    
    @Override
    public InitializeResponseVo createDb(DbServiceVO serviceVo) {
        InitializeResponseVo response = new InitializeResponseVo();
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			Date now = isoFormat.parse(isoFormat.format(new Date()));
            serviceVo.setCreatedOn(now);
            serviceVo.setModifiedOn(now);            
            String id = UUID.randomUUID().toString();
            serviceVo.setId(id);
            serviceVo.setStatus("CREATED");
            response.setData(null);
            response.setSuccess("failed");
            DbServiceNsql entity =  assembler.toEntity(serviceVo); 
            String adminSecret = passwordGenerator.generatePassword();
            String readSecret = passwordGenerator.generatePassword();
            String writeSecret = passwordGenerator.generatePassword();

            Map<String, String> vaultData = new HashMap<String, String>();
            vaultData.put("db_admin", adminSecret);
            vaultData.put("db_read", readSecret);
            vaultData.put("db_write", writeSecret);
            vaultData.put("accesskey",accesskey);
            vaultData.put("backup.password",backupPassword);
            vaultData.put("bucketName", bucketName);
            vaultData.put("dataCatalog.password",dataCatalogPassword);
            vaultData.put("dbadm.password",adminSecret);
            vaultData.put("debezium.password", debeziumPassword);
            vaultData.put("patroni.password", patroniPassword);
            vaultData.put("pgmon.password", pgmonPassword);
            vaultData.put("retention", retention);
            vaultData.put("s3.host",s3Host);
            vaultData.put("secretkey", s3secretkey);
            vaultData.put("size", size);
                StringBuilder template = new StringBuilder(initDbTemplate);
                Map<String,String> replacements = Map.of(
                    "<superusername>","db_admin",
                    "<readonlyusername>","db_read",
                    "<readwrtiteusername>","db_write",
                    "<dbname>",serviceVo.getDbName(),
                    "<readonlypassword>",readSecret,
                    "<readwrtitepassword>",writeSecret
                );
                StringBuilder templateResult = replacePlaceholders(template, replacements);
                String initScript = templateResult.toString();
                initScript = initScript.replace("\\n", "\n").replace("\\\"", "\"");
                vaultData.put("init-db",initScript);

            
            String token = argoCdService.getArgoToken();
            if(token != null){
                String vaultResponsee = vault.addToVault(serviceVo.getServiceName().toLowerCase(), vaultData);                                
                if(vaultResponsee.equalsIgnoreCase("success")){
                    log.info("vault created");
                    String argoResponse = argoCdService.createArgoApp(token, serviceVo.getServiceName().toLowerCase(),serviceVo.getDbName().toLowerCase(),serviceVo.getDbType().toLowerCase());
                    if(argoResponse.equals("success")){
                        log.info("argocd application created");
                        DbServiceNsql responseEntiy = repository.save(entity); 
                        DbServiceVO responseVo = assembler.toVo(responseEntiy);
                        List<CredentialsVO> credentialsList = getCredentials(responseVo.getProjectOwner(), responseVo.getServiceName());
                        responseVo.setCredentials(credentialsList);

                        response.setData(responseVo);
                        response.setSuccess("success");
                    }else{
                        //delete vault
                        String vaultRes = vault.deleteFromVault(serviceVo.getServiceName().toLowerCase());
                        if(vaultRes.equalsIgnoreCase("success")){
                            log.info("vault deleted successfully");
                        }
                    }
                }  
            }
                return response;

        } catch (Exception e) {
            log.error("exception in create db {}",e.getMessage());            
			return response;
        }
    }

    public static StringBuilder replacePlaceholders(StringBuilder template, Map<String, String> replacements) {
        
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = entry.getKey();
            String replacement = entry.getValue();

            int index = template.indexOf(placeholder);
            while (index != -1) {
                template.replace(index, index + placeholder.length(), replacement);
                index = template.indexOf(placeholder, index + replacement.length());
            }
        }

        return template;
    }

    

    @Override
    public List<CredentialsVO> getCredentials(UserInfoVO user,String serviceName){
        List<CredentialsVO> credentials = new ArrayList<>();
        VaultResponse response = vault.getFromVault(serviceName.toLowerCase());
        if(response != null ){
           
           CredentialsVO admin = new CredentialsVO();
           CredentialsVO read = new CredentialsVO();
           CredentialsVO write = new CredentialsVO();

           admin.setUserName("db_admin");
           read.setUserName("db_read");
           write.setUserName("db_write");
           admin.setPassword(response.getData().get("db_admin").toString());
           read.setPassword(response.getData().get("db_read").toString());
           write.setPassword(response.getData().get("db_write").toString());
           if(user.getIsAdmin()){
                credentials.add(admin);
                credentials.add(read);
                credentials.add(write);
           }else if(user.getIsWrite()){
                credentials.add(read);
                credentials.add(write);
           }else{
                credentials.add(read);
           }
        }
        return credentials;
    }


   
    @Override
	@Transactional
	public List<DbServiceVO> getAllDbService(int offset, int limit,String id) {
		List<DbServiceNsql> entities = dbServiceCustomRepo.findAllDbService(offset, limit,id);        
        List<DbServiceVO> voList = entities.stream().map(n ->{
            DbServiceVO vo = assembler.toVo(n);
            List<CredentialsVO> credentialsList = new ArrayList<>();
            if(vo.getProjectOwner() != null && vo.getProjectOwner().getId().equalsIgnoreCase(id)){ 
                credentialsList = getCredentials(vo.getProjectOwner(), vo.getServiceName());
            }else if(vo.getProjectCollaborators() != null &&  vo.getProjectCollaborators().stream().anyMatch(i -> i.getId().equalsIgnoreCase(id))){
                UserInfoVO user = vo.getProjectCollaborators().stream().filter(i -> i.getId().equalsIgnoreCase(id)).findFirst().get();
                credentialsList = getCredentials(user, vo.getServiceName());
            }
            vo.setCredentials(credentialsList);
            return vo;
            }).collect(Collectors.toList());
		return voList ;
	}


    @SuppressWarnings("deprecation")
    @Override
    public InitializeResponseVo editDb(DbServiceVO serviceVo,UserInfoVO user) {
        InitializeResponseVo response = new InitializeResponseVo();
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			Date now = isoFormat.parse(isoFormat.format(new Date()));
            serviceVo.setModifiedOn(now);
            response.setData(null);
            response.setSuccess("failed");
            DbServiceNsql entity =  repository.getById(serviceVo.getId()); 
            DbService data = entity.getData();
                data.setModifiedOn(now);
                data.setModifiedBy(assembler.toUserInfo(serviceVo.getModifiedBy()));
                data.setDataGovernance(assembler.toGovernanceEntity(serviceVo.getDataGovernance()));
                data.setDescription(serviceVo.getDescription());
                List<UserInfoVO> projectCollabsVO = serviceVo.getProjectCollaborators();
				 if (projectCollabsVO != null && !projectCollabsVO.isEmpty()) {
					 List<UserInfo> projectCollabs = projectCollabsVO.stream().map(n -> assembler.toUserInfo(n))
							 .collect(Collectors.toList());
					 data.setProjectCollaborators(projectCollabs);
				 }
                 data.setProjectType(serviceVo.getProjectType());
                 entity.setData(data);
                    DbServiceNsql responseEntiy = repository.save(entity); 
                    DbServiceVO responseVo = assembler.toVo(responseEntiy);
                    List<CredentialsVO> credentialsList = getCredentials(user, responseVo.getServiceName());
                    responseVo.setCredentials(credentialsList);

                    response.setData(responseVo);
                    response.setSuccess("success");
                return response;
        } catch (Exception e) {
            log.error("exception in edit db {}",e.getMessage());            
			return response;
        }
    }

    @Deprecated
    @Override
    public GenericMessage deleteDb(DbServiceVO serviceVo){
        GenericMessage response = new GenericMessage();
        response.setSuccess("Failed");
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
			Date now = isoFormat.parse(isoFormat.format(new Date()));
            String token = argoCdService.getArgoToken();
            String vaultRes = vault.deleteFromVault(serviceVo.getServiceName().toLowerCase());
            if(vaultRes.equalsIgnoreCase("success")){
                log.info("vault with name {} deleted successfully",serviceVo.getServiceName());
                String argoResponse = argoCdService.deleteArgoApp(token, serviceVo.getServiceName());
                        if(argoResponse.equalsIgnoreCase("success")){
                            log.info("application "+serviceVo.getServiceName()+" deleted successfully");
                            DbServiceNsql entity =  repository.getById(serviceVo.getId()); 
                            DbService data = entity.getData();
                            data.setModifiedOn(now);
                            data.setModifiedBy(assembler.toUserInfo(serviceVo.getModifiedBy()));
                            data.setStatus("DELETED");
                            repository.save(entity);
                            response.setSuccess("success");
                        }else{
                            log.info("application "+serviceVo.getServiceName()+" is not deleted");
                            MessageDescription exceptionMsg = new MessageDescription();
                            exceptionMsg.setMessage("Failed to delete application "+serviceVo.getServiceName()+" due to internal error.");
                            response.addErrorsItem(exceptionMsg); 
                        }
            }else{
                log.info("vault with name "+serviceVo.getServiceName()+" is not deleted");
                MessageDescription exceptionMsg = new MessageDescription();
                exceptionMsg.setMessage("Failed to delete application"+serviceVo.getServiceName()+" due to internal error.");
                response.addErrorsItem(exceptionMsg); 
            }
            return response;
            
        } catch (Exception e) {
            log.error("exception in delete db {}",e.getMessage());  
            MessageDescription exceptionMsg = new MessageDescription();
            exceptionMsg.setMessage("Failed to delete dbService "+serviceVo.getServiceName()+" due to internal error.");
			response.addErrorsItem(exceptionMsg);          
			return response;
        }
    }


    
    

}
