package com.daimler.data.application.auth;

import java.util.Set;

public final class PatPermissions {
	public static final String READ = "workspace:read";
	public static final String DEPLOY_STAGING = "workspace:deploy:staging";
	public static final String DEPLOY_PRODUCTION = "workspace:deploy:production";
	public static final Set<String> ALL = Set.of(READ, DEPLOY_STAGING, DEPLOY_PRODUCTION);

	private PatPermissions() {
	}
}
