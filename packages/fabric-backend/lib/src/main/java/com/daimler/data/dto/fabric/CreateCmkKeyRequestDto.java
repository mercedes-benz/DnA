package com.daimler.data.dto.fabric;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCmkKeyRequestDto {

    private String kty;

    @JsonProperty("key_size")
    private Integer keySize;

    @JsonProperty("key_ops")
    private List<String> keyOps;
}
