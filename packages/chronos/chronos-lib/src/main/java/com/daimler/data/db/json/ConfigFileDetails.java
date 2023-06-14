package com.daimler.data.db.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigFileDetails {

    private String configFileId;
    private String configFilePath;
    private String configFileName;
    private String triggeredBy;
    private Date triggeredOn;
    private Boolean isDeleted;

}
