package com.daimler.data.db.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Matomo {
    private String name;
    private UserDetails createdBy;
    //private String resultFolderPath;
    private Date createdOn;
    private List<UserDetails> collaborators;

}
