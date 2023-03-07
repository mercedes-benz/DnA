package com.daimler.data.client.teamsApi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TeamsApiResponseDto {
    private float score;
    private String mail;
    private  String company_id;
    private String telephone_namber;
    private String mobile;
    private String given_name;
    private String kim;
    private String department_description;
    private String surname;
    private String company_name;
    private String plant;
    private String id;
    private String department;

}
