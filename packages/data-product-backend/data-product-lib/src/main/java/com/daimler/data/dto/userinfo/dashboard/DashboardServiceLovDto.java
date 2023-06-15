package com.daimler.data.dto.userinfo.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardServiceLovDto implements Serializable  {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
}
