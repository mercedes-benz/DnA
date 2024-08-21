package com.daimler.data.dto;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyTeamMemberVO {
    private String id;
    private String shortId;
    private String userType;
    private String firstName;
    private String lastName;
    private String department;
    private String email;
    private String mobileNumber;
    private String company;
    private String teamMemberPosition;
    private Boolean isUseCaseOwner;
}
