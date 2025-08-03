package com.daimler.data.db.json.catalogManangement;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MandatoryFields implements Serializable{
    private static final long serialVersionUID = 1L;

    private String division;
    private String dataOrigin;
    private String leanIXId;
    private String isDocumentationUpdated;
    private String isDataLakeAvailability;
    private String isDataAsset;
    private String dataConfidentiality;
    
}
