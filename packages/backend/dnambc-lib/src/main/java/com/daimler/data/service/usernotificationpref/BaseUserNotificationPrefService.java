/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.service.usernotificationpref;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.assembler.UserNotificationPrefAssembler;
import com.daimler.data.db.entities.UserNotificationPrefNsql;
import com.daimler.data.db.repo.usernotificationpref.UserNotificationPrefCustomRepository;
import com.daimler.data.db.repo.usernotificationpref.UserNotificationPrefRepository;
import com.daimler.data.dto.usernotificationpref.NotificationPreferenceVO;
import com.daimler.data.dto.usernotificationpref.UserNotificationPrefVO;
import com.daimler.data.service.common.BaseCommonService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseUserNotificationPrefService extends BaseCommonService<UserNotificationPrefVO, UserNotificationPrefNsql, String>
		implements UserNotificationPrefService {
	
	@Autowired
	private UserNotificationPrefCustomRepository customRepo;
	@Autowired
	private UserNotificationPrefRepository jpaRepo;
	@Autowired
	private UserNotificationPrefAssembler userNotificationPrefAssembler;
	
	@Value("${notification.email.solutionNotificationPref}")
	private boolean defaultSolutionEmailNotificationPref;
	
	@Value("${notification.email.notebookNotificationPref}")
	private boolean defaultNotebookEmailNotificationPref;
	
	@Value("${notification.email.persistenceNotificationPref}")
	private boolean defaultPersistenceEmailNotificationPref;
	
	@Value("${notification.email.dashboardNotificationPref}")
	private boolean defaultDashboardEmailNotificationPref;
	
	@Value("${notification.email.dataComplianceNotificationPref}")
	private boolean defaultDataComplianceEmailNotificationPref;
	
	@Value("${notification.email.dataProductNotificationPref}")
	private boolean defaultDataProductEmailNotificationPref;
	
	@Value("${notification.email.chronosNotificationPref}")
	private boolean defaultChronosEmailNotificationPref;

	@Value("${notification.app.solutionNotificationPref}")
	private boolean defaultSolutionAppNotificationPref;
	
	@Value("${notification.app.notebookNotificationPref}")
	private boolean defaultNotebookAppNotificationPref;
	
	@Value("${notification.app.persistenceNotificationPref}")
	private boolean defaultPersistenceAppNotificationPref;
	
	@Value("${notification.app.dashboardNotificationPref}")
	private boolean defaultDashboardAppNotificationPref;
	
	@Value("${notification.app.dataComplianceNotificationPref}")
	private boolean defaultDataComplianceAppNotificationPref;
	
	@Value("${notification.app.dataProductNotificationPref}")
	private boolean defaultDataProductAppNotificationPref;
	
	@Value("${notification.app.chronosNotificationPref}")
	private boolean defaultChronosAppNotificationPref;

	public BaseUserNotificationPrefService() {
		super();
	}

	@Override
	public UserNotificationPrefVO getById(String id) {
		UserNotificationPrefVO preferenceVO = super.getById(id);
		
		return super.getById(id);
	}

	@Override
	@Transactional
	public UserNotificationPrefVO getByUniqueliteral(String uniqueLiteral, String value) {
		UserNotificationPrefVO preferencesVO = super.getByUniqueliteral(uniqueLiteral, value);
		if("userId".equalsIgnoreCase(uniqueLiteral)) {
			log.debug("Searching user preferences based on user shortId {} ", value);
			if(preferencesVO!=null && preferencesVO.getId()!=null && value.equalsIgnoreCase(preferencesVO.getUserId())) {
				log.debug("Returning successfully after finding user preferences for user shortId {}  ", value);
				return preferencesVO;
			}else {
				log.debug("Couldnt find user preferences for user {} , sending default preference", value);
				preferencesVO = new UserNotificationPrefVO();
				preferencesVO.setUserId(value);
				preferencesVO.setTermsOfUse(false);
				NotificationPreferenceVO notebookNotificationPref = new NotificationPreferenceVO();
				notebookNotificationPref.setEnableAppNotifications(defaultNotebookAppNotificationPref);
				notebookNotificationPref.setEnableEmailNotifications(defaultNotebookEmailNotificationPref);
				preferencesVO.setNotebookNotificationPref(notebookNotificationPref);
				NotificationPreferenceVO solutionNotificationPref = new NotificationPreferenceVO();
				solutionNotificationPref.setEnableAppNotifications(defaultSolutionAppNotificationPref);
				solutionNotificationPref.setEnableEmailNotifications(defaultSolutionEmailNotificationPref);
				preferencesVO.setSolutionNotificationPref(solutionNotificationPref);
				NotificationPreferenceVO persistenceNotificationPref = new NotificationPreferenceVO();
				persistenceNotificationPref.setEnableAppNotifications(defaultPersistenceAppNotificationPref);
				persistenceNotificationPref.setEnableEmailNotifications(defaultPersistenceEmailNotificationPref);
				preferencesVO.setPersistenceNotificationPref(persistenceNotificationPref);
				NotificationPreferenceVO dashboardNotificationPref = new NotificationPreferenceVO();
				dashboardNotificationPref.setEnableAppNotifications(defaultDashboardAppNotificationPref);
				dashboardNotificationPref.setEnableEmailNotifications(defaultDashboardEmailNotificationPref);
				preferencesVO.setDashboardNotificationPref(dashboardNotificationPref);
				NotificationPreferenceVO dataComplianceNotificationPref = new NotificationPreferenceVO();
				dataComplianceNotificationPref.setEnableAppNotifications(defaultDataComplianceAppNotificationPref);
				dataComplianceNotificationPref.setEnableEmailNotifications(defaultDataComplianceEmailNotificationPref);
				preferencesVO.setDataComplianceNotificationPref(dataComplianceNotificationPref);
				NotificationPreferenceVO dataProductNotificationPref = new NotificationPreferenceVO();
				dataProductNotificationPref.setEnableAppNotifications(defaultDataProductAppNotificationPref);
				dataProductNotificationPref.setEnableEmailNotifications(defaultDataProductEmailNotificationPref);
				preferencesVO.setDataProductNotificationPref(dataProductNotificationPref);
				NotificationPreferenceVO chronosNotificationPref = new NotificationPreferenceVO();
				chronosNotificationPref.setEnableAppNotifications(defaultChronosAppNotificationPref);
				chronosNotificationPref.setEnableEmailNotifications(defaultChronosEmailNotificationPref);
				preferencesVO.setChronosNotificationPref(chronosNotificationPref);				
				try {
					UserNotificationPrefVO savedPreferencesVO = this.create(preferencesVO);
					log.info("Notification preferences created for user {} ", value);
					return savedPreferencesVO;
				}catch(Exception e) {
					log.error("Error creating notification preferences for user {} . Exception is {} ", value,e.getMessage());
				}
			}
		}
		return preferencesVO;
	}
	
	
	
}
