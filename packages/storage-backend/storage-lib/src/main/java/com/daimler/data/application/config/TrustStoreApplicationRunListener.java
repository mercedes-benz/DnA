package com.daimler.data.application.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class TrustStoreApplicationRunListener implements SpringApplicationRunListener {

    public TrustStoreApplicationRunListener(SpringApplication application, String[] args) {
        // Constructor required by SpringApplicationRunListener
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        String trustStorePath = environment.getProperty("trust.store.path");
        String trustStorePassword = environment.getProperty("trust.store.password");

        if (trustStorePath != null && trustStorePassword != null) {
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        }
    }

} 
