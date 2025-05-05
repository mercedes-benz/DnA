package com.daimler.data.dto.auth.vault;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaultGenericResponse {

    private String status;
    private String message;
    private VaultDTO data;
}

