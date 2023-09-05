package com.daimler.data.application.auth;
import com.daimler.data.dto.matomo.CreatedByVO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStore {

    private UserInfo userInfo;

    private static Logger LOGGER = LoggerFactory.getLogger(UserStore.class);

    public void clear() {
        this.userInfo = null;
        LOGGER.debug("In UserStore.clear , clearing user");
    }

    public CreatedByVO getVO() {
        CreatedByVO vo = new CreatedByVO();
        vo.setId(this.userInfo.getId());
        vo.setFirstName(this.userInfo.getFirstName());
        vo.setLastName(this.userInfo.getLastName());
        vo.setDepartment(this.userInfo.getDepartment());
        vo.setEmail(this.userInfo.getEmail());
        vo.setMobileNumber(this.userInfo.getMobileNumber());
        return vo;
    }

    @Data
    @AllArgsConstructor
    @Builder
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserInfo {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String mobileNumber;
        private String department;

        private String sub;
        private boolean email_verified;
        private String name;
        private String given_name;
        private String family_name;
        private String personal_data_hint;
        private String updated_at;
        private List<UserRole> userRole;

        public UserInfo() {
            this.department = "";
        }

        public void setSub(String sub) {
            this.sub = this.id = sub;
        }

        public void setGiven_name(String given_name) {
            this.firstName = this.given_name = given_name;
        }

        public void setFamily_name(String family_name) {
            this.lastName = this.family_name = family_name;
        }

        public boolean hasAdminAccess() {
            return this.getUserRole().stream().anyMatch(
                    n -> "Admin".equalsIgnoreCase(n.getName()) || "ReportAdmin".equalsIgnoreCase(n.getName()));
        }

        public boolean hasSuperAdminAccess() {
            return this.getUserRole().stream().anyMatch(n -> "Admin".equalsIgnoreCase(n.getName()));
        }

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserRole implements Serializable {
        private static final long serialVersionUID = 1L;
        private String id;
        private String name;
    }

}


