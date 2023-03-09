package com.daimler.data.client.teamsApi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamsApiResponseDto {
    private String mail;
    private String mobile;
    private String given_name;
    private String surname;
    private String id;
    private String department;

}
