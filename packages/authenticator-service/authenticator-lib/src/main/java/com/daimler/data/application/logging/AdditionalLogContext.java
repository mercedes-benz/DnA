package com.daimler.data.application.logging;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.stereotype.Component;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.dto.solution.CreatedByVO;

public class AdditionalLogContext extends FilterRegistrationBean {

	public AdditionalLogContext() {
		super(new MDCContextFilter());
		addUrlPatterns("/api/*");
		setOrder(Integer.MAX_VALUE);
	}

	@Component
	public static class MDCContextFilter implements Filter {

		@Autowired
		private ApplicationLoggingProperties loggingProperties;

		@Autowired
		private UserStore userStore;

		@Override
		public void init(FilterConfig filterconfig) throws ServletException {
		}

		@Override
		public void destroy() {
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			String userId = "";
			try {
				CreatedByVO currentUser = this.userStore.getVO();
				userId = currentUser != null ? currentUser.getId() : null;
			} catch (Exception e) {
				userId = null;
			}
			MDC.put("env", loggingProperties.getEnvironment());
			MDC.put("user", userId);
			chain.doFilter(request, response);
		}

	}
}
