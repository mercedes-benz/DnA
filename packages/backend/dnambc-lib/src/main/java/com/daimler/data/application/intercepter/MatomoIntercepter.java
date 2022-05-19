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

package com.daimler.data.application.intercepter;

import java.net.URL;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.jni.Local;
import org.piwik.java.tracking.CustomVariable;
import org.piwik.java.tracking.PiwikLocale;
import org.piwik.java.tracking.PiwikRequest;
import org.piwik.java.tracking.PiwikTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.LoginController.UserInfo;

@Component
public class MatomoIntercepter implements HandlerInterceptor {

	private Logger log = LoggerFactory.getLogger(MatomoIntercepter.class);

	@Value("${matomo.tracking}")
	private boolean enableTracking;

	@Value("${matomo.siteId}")
	private Integer siteId;

	@Value("${matomo.hostUrl}")
	private String hostUrl;

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception exception) throws Exception {
		if (enableTracking) {
			log.debug("Tracking is enabled!! Sending request to action tracker..");
			String action = request.getHeader("action");
			if (StringUtils.hasText(action)) {
				UserInfo userInfo = (UserInfo) request.getAttribute("userDetails");
				URL actionUrl = new URL(request.getRequestURL().toString());
				PiwikRequest pr = new PiwikRequest(siteId, actionUrl);
				pr.setActionName(action);
				pr.setEventAction(action);
				if (Objects.nonNull(userInfo)) {
					pr.setUserId(userInfo.getId());
				}
				PiwikTracker tracker = new PiwikTracker(hostUrl);
				tracker.sendRequestAsync(pr);
			}

		}
	}
}
