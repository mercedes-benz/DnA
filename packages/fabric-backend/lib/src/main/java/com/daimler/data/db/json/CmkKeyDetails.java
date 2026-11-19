package com.daimler.data.db.json;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmkKeyDetails implements Serializable {
    private static final long serialVersionUID = 1L;
    private String cmkKey;
    private Boolean cmkKeyCreated;
    private Boolean cmkKeyAssign;
}