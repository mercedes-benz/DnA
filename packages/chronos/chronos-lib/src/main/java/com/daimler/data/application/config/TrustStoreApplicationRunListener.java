package com.daimler.data.application.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.env.ConfigurableEnvironment;

public class TrustStoreApplicationRunListener implements SpringApplicationRunListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrustStoreApplicationRunListener.class);

    public TrustStoreApplicationRunListener(SpringApplication application, String[] args) {
        // Required constructor
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        LOGGER.info("Adding CA Trust Store....");

        String trustStorePath = environment.getProperty("spring.cloud.vault.ssl.trust.store.path");
        String trustStorePassword = environment.getProperty("spring.cloud.vault.ssl.trust.store.password");

        if (trustStorePath != null && trustStorePassword != null) {
            // Set the trust store properties
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            LOGGER.info("Trust store configured: {}", trustStorePath);
        } else {
            LOGGER.warn("Trust store path or password is not configured!");
        }
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        // This method is intentionally left empty or for additional early setup
    }
}
