package com.daimler.data.assembler;

import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.UserNotificationPrefNsql;
import com.daimler.data.db.jsonb.NotificationPreference;
import com.daimler.data.db.jsonb.UserNotificationPreference;
import com.daimler.data.dto.usernotificationpref.NotificationPreferenceVO;
import com.daimler.data.dto.usernotificationpref.UserNotificationPrefVO;

@Component
public class UserNotificationPrefAssembler  implements GenericAssembler<UserNotificationPrefVO, UserNotificationPrefNsql> {

	@Override
	public UserNotificationPrefVO toVo(UserNotificationPrefNsql entity) {
		UserNotificationPrefVO vo = new UserNotificationPrefVO();
		if(entity != null) {
			vo.setId(entity.getId());
			UserNotificationPreference data = entity.getData();
			if(data!=null) {
				vo.setTermsOfUse(data.getTermsOfUse());
				NotificationPreference notebookNotificationPrefJson = data.getNotebookNotificationPref();
				NotificationPreferenceVO notebookNotificationPrefVO = this.toNotificationPrefVO(notebookNotificationPrefJson);
				vo.setNotebookNotificationPref(notebookNotificationPrefVO);
				
				NotificationPreference solutionNotificationPrefJson = data.getSolutionNotificationPref();
				NotificationPreferenceVO solutionNotificationPrefVO = this.toNotificationPrefVO(solutionNotificationPrefJson);
				vo.setSolutionNotificationPref(solutionNotificationPrefVO);
				
				NotificationPreference persistenceNotificationPrefJson = data.getPersistenceNotificationPref();
				NotificationPreferenceVO persistenceNotificationPrefVO = this.toNotificationPrefVO(persistenceNotificationPrefJson);
				vo.setPersistenceNotificationPref(persistenceNotificationPrefVO);
				
				NotificationPreference dashboardNotificationPrefJson = data.getDashboardNotificationPref();
				NotificationPreferenceVO dashboarNotificationPrefVO = this.toNotificationPrefVO(dashboardNotificationPrefJson);
				vo.setDashboardNotificationPref(dashboarNotificationPrefVO);
				
				NotificationPreference dataComplianceNotificationPrefJson = data.getDataComplianceNotificationPref();
				NotificationPreferenceVO dataComplianceNotificationPrefVO = new NotificationPreferenceVO();
				if(dataComplianceNotificationPrefJson != null) {
					dataComplianceNotificationPrefVO.setEnableAppNotifications(dataComplianceNotificationPrefJson.isEnableAppNotifications());
					dataComplianceNotificationPrefVO.setEnableEmailNotifications(dataComplianceNotificationPrefJson.isEnableEmailNotifications());
				}
				else {
					dataComplianceNotificationPrefVO.setEnableAppNotifications(true);
					dataComplianceNotificationPrefVO.setEnableEmailNotifications(true);
				}
				vo.setDataComplianceNotificationPref(dataComplianceNotificationPrefVO);
				
				NotificationPreference dataProductNotificationPrefJson = data.getDataProductNotificationPref();
				NotificationPreferenceVO dataProductNotificationPrefVO = new NotificationPreferenceVO();
				if(dataProductNotificationPrefJson != null) {
					dataProductNotificationPrefVO.setEnableAppNotifications(dataProductNotificationPrefJson.isEnableAppNotifications());
					dataProductNotificationPrefVO.setEnableEmailNotifications(dataProductNotificationPrefJson.isEnableEmailNotifications());
				}
				else {
					dataProductNotificationPrefVO.setEnableAppNotifications(true);
					dataProductNotificationPrefVO.setEnableEmailNotifications(true);
				}
				vo.setDataProductNotificationPref(dataProductNotificationPrefVO);
				
				NotificationPreference chronosNotificationPrefJson = data.getChronosNotificationPref();
				NotificationPreferenceVO chronosNotificationPrefVO = new NotificationPreferenceVO();
				if(chronosNotificationPrefJson != null) {
					chronosNotificationPrefVO.setEnableAppNotifications(chronosNotificationPrefJson.isEnableAppNotifications());
					chronosNotificationPrefVO.setEnableEmailNotifications(chronosNotificationPrefJson.isEnableEmailNotifications());
				}
				else {
					chronosNotificationPrefVO.setEnableAppNotifications(true);
					chronosNotificationPrefVO.setEnableEmailNotifications(true);
				}
				vo.setChronosNotificationPref(chronosNotificationPrefVO);
				
				NotificationPreference codespaceNotificationPrefJson = data.getCodespaceNotificationPref();
				NotificationPreferenceVO codespaceNotificationPrefVO = this.toNotificationPrefVO(codespaceNotificationPrefJson);
				vo.setCodespaceNotificationPref(codespaceNotificationPrefVO);
				
				NotificationPreference airflowNotificationPrefJson = data.getAirflowNotificationPref();
				NotificationPreferenceVO airflowNotificationPrefVO = this.toNotificationPrefVO(airflowNotificationPrefJson);
				vo.setAirflowNotificationPref(airflowNotificationPrefVO);
				
				
				NotificationPreference dataLakeNotificationPrefJson = data.getDataLakeNotificationPref();
				NotificationPreferenceVO dataLakeNotificationPrefVO = this.toNotificationPrefVO(dataLakeNotificationPrefJson);
				vo.setDataLakeNotificationPref(dataLakeNotificationPrefVO);
				
				NotificationPreference dataEntryNotificationPrefJson = data.getDataEntryNotificationPref();
				NotificationPreferenceVO dataEntryNotificationPrefVO = this.toNotificationPrefVO(dataEntryNotificationPrefJson);
				vo.setDataEntryNotificationPref(dataEntryNotificationPrefVO);
				
				NotificationPreference powerPlatformNotificationPrefJson = data.getPowerPlatformNotificationPref();
				NotificationPreferenceVO powerPlatformNotificationPrefVO = this.toNotificationPrefVO(powerPlatformNotificationPrefJson);
				vo.setPowerPlatformNotificationPref(powerPlatformNotificationPrefVO);

				NotificationPreference useCaseOwnerNotificationPrefJson = data.getUseCaseOwnerNotificationPref();
				NotificationPreferenceVO useCaseOwnerNotificationPrefVO = this.toNotificationPrefVO(useCaseOwnerNotificationPrefJson);
				if(useCaseOwnerNotificationPrefJson != null) {
					useCaseOwnerNotificationPrefVO.setEnableAppNotifications(useCaseOwnerNotificationPrefJson.isEnableAppNotifications());
					useCaseOwnerNotificationPrefVO.setEnableEmailNotifications(useCaseOwnerNotificationPrefJson.isEnableEmailNotifications());
				}
				else {
					useCaseOwnerNotificationPrefVO.setEnableAppNotifications(true);
					useCaseOwnerNotificationPrefVO.setEnableEmailNotifications(true);
				}
				vo.setUseCaseOwnerNotificationPref(useCaseOwnerNotificationPrefVO);
				
				vo.setUserId(data.getUserId());
			}
		}
		return vo;
	}

	private NotificationPreferenceVO toNotificationPrefVO(NotificationPreference notificationPrefJson){
		
		NotificationPreferenceVO  vo = new NotificationPreferenceVO();
		if(notificationPrefJson!=null) {
			vo.setEnableAppNotifications(notificationPrefJson.isEnableAppNotifications());
			vo.setEnableEmailNotifications(notificationPrefJson.isEnableEmailNotifications());
		}else {
			vo.setEnableAppNotifications(true);
			vo.setEnableEmailNotifications(false);
		}
		return vo;
	}
	
	
	@Override
	public UserNotificationPrefNsql toEntity(UserNotificationPrefVO vo) {
		UserNotificationPrefNsql entity = new UserNotificationPrefNsql();
		UserNotificationPreference userNotificationPreferenceJsonb = new UserNotificationPreference();
		if(vo!=null) {
			userNotificationPreferenceJsonb.setUserId(vo.getUserId());
			NotificationPreference notebookPreference = this.toNotificationPrefJson(vo.getNotebookNotificationPref());
			userNotificationPreferenceJsonb.setNotebookNotificationPref(notebookPreference);
			NotificationPreference solutionPreference = this.toNotificationPrefJson(vo.getSolutionNotificationPref());
			userNotificationPreferenceJsonb.setSolutionNotificationPref(solutionPreference);
			NotificationPreference persistencePreference = this.toNotificationPrefJson(vo.getPersistenceNotificationPref());
			userNotificationPreferenceJsonb.setPersistenceNotificationPref(persistencePreference);
			NotificationPreference dashboardPreference = this.toNotificationPrefJson(vo.getDashboardNotificationPref());
			userNotificationPreferenceJsonb.setDashboardNotificationPref(dashboardPreference);
			NotificationPreference dataCompliancePreference = this.toNotificationPrefJson(vo.getDataComplianceNotificationPref());
			userNotificationPreferenceJsonb.setDataComplianceNotificationPref(dataCompliancePreference);
			NotificationPreference dataProductPreference = this.toNotificationPrefJson(vo.getDataProductNotificationPref());
			userNotificationPreferenceJsonb.setDataProductNotificationPref(dataProductPreference);
			userNotificationPreferenceJsonb.setTermsOfUse(vo.isTermsOfUse());
			NotificationPreference chronosNotificationPreference = this.toNotificationPrefJson(vo.getChronosNotificationPref());
			userNotificationPreferenceJsonb.setChronosNotificationPref(chronosNotificationPreference);
			NotificationPreference codespaceNotificationPreference = this.toNotificationPrefJson(vo.getCodespaceNotificationPref());
			userNotificationPreferenceJsonb.setCodespaceNotificationPref(codespaceNotificationPreference);
			NotificationPreference airflowNotificationPreference = this.toNotificationPrefJson(vo.getAirflowNotificationPref());
			userNotificationPreferenceJsonb.setAirflowNotificationPref(airflowNotificationPreference);
			NotificationPreference dataLakeNotificationPreference = this.toNotificationPrefJson(vo.getDataLakeNotificationPref());
			userNotificationPreferenceJsonb.setDataLakeNotificationPref(dataLakeNotificationPreference);
			NotificationPreference dataEntryNotificationPreference = this.toNotificationPrefJson(vo.getDataEntryNotificationPref());
			userNotificationPreferenceJsonb.setDataEntryNotificationPref(dataEntryNotificationPreference);
			NotificationPreference useCaseOwnerPreference = this.toNotificationPrefJson(vo.getUseCaseOwnerNotificationPref());
			userNotificationPreferenceJsonb.setUseCaseOwnerNotificationPref(useCaseOwnerPreference);
			NotificationPreference powerPlatformNotificationPref = this.toNotificationPrefJson(vo.getPowerPlatformNotificationPref());
			userNotificationPreferenceJsonb.setPowerPlatformNotificationPref(powerPlatformNotificationPref);
			entity.setId(vo.getId());
		}
		entity.setData(userNotificationPreferenceJsonb);
		return entity;
	}
	
	private NotificationPreference toNotificationPrefJson(NotificationPreferenceVO notificationPrefVO){
		
		NotificationPreference notificationPrefJson = new NotificationPreference();
		if(notificationPrefVO!=null) {
			notificationPrefJson.setEnableAppNotifications(notificationPrefVO.isEnableAppNotifications());
			notificationPrefJson.setEnableEmailNotifications(notificationPrefVO.isEnableEmailNotifications());
		}else {
			notificationPrefJson.setEnableAppNotifications(true);
			notificationPrefJson.setEnableEmailNotifications(false);
		}
		return notificationPrefJson;
	}

}
