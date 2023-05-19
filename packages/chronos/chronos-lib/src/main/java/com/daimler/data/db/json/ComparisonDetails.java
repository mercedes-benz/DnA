package com.daimler.data.db.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComparisonDetails
{
    private String comparisonId;
    private String comparisonName;
    private String triggeredBy;
    private Date triggeredOn;
    private String actualsFile;
    private String targetFolder;
    private String[] runsList;
    private Boolean isDelete;
    private ComparisonState comparisonState;






}
