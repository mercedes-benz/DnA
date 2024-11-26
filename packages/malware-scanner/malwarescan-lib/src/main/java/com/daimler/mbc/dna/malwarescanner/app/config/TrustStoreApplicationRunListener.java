package com.daimler.data.application.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class TrustStoreApplicationRunListener implements SpringApplicationRunListener {

    private static Logger LOGGER = LoggerFactory.getLogger(TrustStoreApplicationRunListener.class);

    public TrustStoreApplicationRunListener(SpringApplication application, String[] args) {
        // Constructor required by SpringApplicationRunListener
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,ConfigurableEnvironment environment) {

        LOGGER.info("Adding CA Trust Store....");
        String trustStorePath = environment.getProperty("spring.cloud.vault.ssl.trust.store.path");
        String trustStorePassword = environment.getProperty("spring.cloud.vault.ssl.trust.store.password");

        if (trustStorePath != null && trustStorePassword != null) {
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        }
        LOGGER.info("Added CA Trust Store value {}....",System.getProperty("javax.net.ssl.trustStore"));
    }

} 