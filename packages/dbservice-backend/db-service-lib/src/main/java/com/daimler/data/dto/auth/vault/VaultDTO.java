package com.daimler.data.dto.auth.vault;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaultDTO {
    private String appId;
    private String apiKey;
}
