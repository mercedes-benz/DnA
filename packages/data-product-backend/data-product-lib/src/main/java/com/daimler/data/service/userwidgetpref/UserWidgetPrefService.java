package com.daimler.data.service.userwidgetpref;

import org.springframework.http.ResponseEntity;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.UserWidgetPreferenceNsql;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceVO;
import com.daimler.data.service.common.CommonService;


public interface UserWidgetPrefService extends CommonService<UserWidgetPreferenceVO, UserWidgetPreferenceNsql, String> {
    ResponseEntity<UserWidgetPreferenceVO> createUserWidgetPreference(UserWidgetPreferenceVO requestUserWidgetPrefVO);

    ResponseEntity<GenericMessage> deleteUserWidgetPreference(String id);
}