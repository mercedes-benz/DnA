package com.daimler.data.dto.fabric;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.daimler.data.dto.fabric.DataProductConnectionStringDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataProductConnectionsDto {

    private String dataHubName;
    private List<String> storingCountries;
    private String cloudRegion;
    private String formatType;
    private String technology;
    private String frequency;
    private String cloudProvider;
    private DataProductConnectionStringDto dataProductConnectionString;
    private List<Object> dataSources;
}