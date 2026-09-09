package com.daimler.data.service.azureKeyVault;

import org.springframework.http.ResponseEntity;
import java.util.List;

import com.daimler.data.db.entities.AzureKeyVaultNsql;
import com.daimler.data.dto.fabricWorkspace.KeyVaultCollectionVO;
import com.daimler.data.dto.fabricWorkspace.KeyVaultResponseVO;
import com.daimler.data.dto.fabricWorkspace.KeyVaultVO;
import com.daimler.data.dto.azureKeyVault.AzurePrincipalDto;
import com.daimler.data.service.common.CommonService;

public interface AzureKeyVaultService extends CommonService<KeyVaultVO, AzureKeyVaultNsql, String> {

	ResponseEntity<KeyVaultResponseVO> createKeyVault(KeyVaultVO vo);

	ResponseEntity<KeyVaultResponseVO> updateKeyVault(KeyVaultVO vo); 
	
	KeyVaultCollectionVO getAllKeyVaults(int limit, int offset, String createdBy);

	List<AzurePrincipalDto> searchPrincipals(String search);
}
