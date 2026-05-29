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
public class MirroredCatalogDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String mirroredCatalogId;
    private String mirroredCatalogUrl;
    private String mirrorCatalogName;
    private String catalogStatus;
    private String message;
}
