package com.daimler.data.service.scheduler;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.service.ArgoCdService;
import com.daimler.data.service.ServiceDowntimeService;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
@Slf4j
public class ServiceDowntimeDetectorJob {

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;
    private static final String ARGOCD_DOWN_MESSAGE =
            "ArgoCD is unavailable. Deployment operations may be affected.";

    @Autowired
    private ServiceDowntimeService serviceDowntimeService;

    @Autowired
    private ArgoCdService argoCdService;

    @Value("${alice.aliceBaseUrl:}")
    private String aliceBaseUrl;

    @Value("${vaultIntegration.service.baseUrl:}")
    private String vaultBaseUrl;

    @Value("${codeServer.git.enterprise.url:}")
    private String gheBaseUri;

    @Value("${downtime.check.artifactory.url:}")
    private String artifactoryUrl;

    @Value("${downtime.check.sso.url:}")
    private String ssoUrl;

    private final RestTemplate restTemplate;

    public ServiceDowntimeDetectorJob() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MS);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    @Scheduled(fixedDelayString = "#{${downtime.check.intervalSeconds:60} * 1000}", initialDelay = 15000)
    @SchedulerLock(name = "serviceDowntimeDetectorJob", lockAtMostFor = "2m", lockAtLeastFor = "30s")
    public void checkServices() {
        runCheck("GitHub Runner", () -> checkHttpService("GitHub Runner", gheBaseUri, "",
                "GitHub Runner / GitHub API is unreachable. Build and deploy operations may be delayed."));
        runCheck("MB Artifactory", () -> checkHttpService("MB Artifactory", artifactoryUrl, "/api/v1/system/liveness",
                "MB Artifactory is unavailable. Dependency resolution and builds may fail."));
        runCheck("ALICE Auth", () -> checkHttpService("ALICE Auth", aliceBaseUrl, "/healthz",
                "ALICE Auth service is unavailable. User entitlement checks may fail."));
        runCheck("SSO", () -> checkHttpService("SSO", ssoUrl, "/.well-known/openid-configuration",
                "SSO service is unavailable. Login and authentication may be affected."));
        runCheck("Vault", () -> checkHttpService("Vault", vaultBaseUrl, "/healthz",
                "Vault service is unavailable. Secret management operations may fail."));
        runCheck("ArgoCD / Cluster", () -> checkHttpService("ArgoCD / Cluster", argoCdService.getArgocdBaseUrl(),
                "/healthz?argocd-level=2", ARGOCD_DOWN_MESSAGE));
    }

    private void checkHttpService(String name, String baseUrl, String path, String downMessage) {
        if (isUnconfigured(baseUrl)) {
            log.debug("Skipping health check for {} because its base URL is not configured", name);
            return;
        }

        String url = baseUrl.replaceAll("/+$", "") + path;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCodeValue() >= 500) {
                serviceDowntimeService.markDown(name,
                        downMessage + " (HTTP " + response.getStatusCodeValue() + ")");
                return;
            }
        } catch (HttpClientErrorException e) {
            serviceDowntimeService.markUp(name);
            return;
        } catch (HttpServerErrorException e) {
            serviceDowntimeService.markDown(name,
                    downMessage + " (HTTP " + e.getStatusCode().value() + ")");
            return;
        } catch (Exception e) {
            log.warn("Health check failed for {}: {}", name, e.getMessage());
            serviceDowntimeService.markDown(name, downMessage);
            return;
        }

        serviceDowntimeService.markUp(name);
    }

    private void runCheck(String name, Runnable check) {
        try {
            check.run();
        } catch (Exception e) {
            log.warn("Service check failed for {}: {}", name, e.getMessage());
        }
    }

    private boolean isUnconfigured(String baseUrl) {
        return baseUrl == null || baseUrl.trim().isEmpty()
                || baseUrl.trim().toUpperCase(Locale.ROOT).startsWith("XXXX");
    }
}
