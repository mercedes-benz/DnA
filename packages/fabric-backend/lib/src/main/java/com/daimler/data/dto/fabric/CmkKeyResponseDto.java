package com.daimler.data.dto.fabric;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CmkKeyResponseDto {
    
    @JsonProperty("key")
    private CmkKey key;
    
    private CmkKeyAttributes attributes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CmkKey {
        private String kid;
        private String kty;
        
        @JsonProperty("key_ops")
        private List<String> keyOps;
        
        private String n;
        private String e;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CmkKeyAttributes {
        private Boolean enabled;
        
        @JsonProperty("key_size")
        private Integer keySize;
        
        private Long created;
        private Long updated;
        
        @JsonProperty("recoveryLevel")
        private String recoveryLevel;
        
        @JsonProperty("recoverableDays")
        private Integer recoverableDays;
        
        private Boolean exportable;
    }
}
