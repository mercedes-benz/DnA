package com.daimler.data.dto.vault;

import com.daimler.data.dto.dbService.SubscriptionkeysVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaultDTO {
    private String projectName;
	private SubscriptionkeysVO subscriptionKeys;
}
