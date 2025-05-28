package com.daimler.data.application.config;
 
 import org.springframework.boot.ConfigurableBootstrapContext;
 import org.springframework.boot.SpringApplication;
 import org.springframework.boot.SpringApplicationRunListener;
 import org.springframework.core.env.ConfigurableEnvironment;

import lombok.extern.slf4j.Slf4j;
 
 @Slf4j
 public class TrustStoreApplicationRunListener implements SpringApplicationRunListener {
 
     public TrustStoreApplicationRunListener(SpringApplication application, String[] args) {
         // Constructor required by SpringApplicationRunListener
     }
 
     @Override
     public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,ConfigurableEnvironment environment) {
 
         log.info("Adding CA Trust Store....");
         String trustStorePath = environment.getProperty("spring.cloud.vault.ssl.trust.store.path");
         String trustStorePassword = environment.getProperty("spring.cloud.vault.ssl.trust.store.password");
         
         if (trustStorePath != null && trustStorePassword != null) {
             System.setProperty("javax.net.ssl.trustStore", trustStorePath);
             System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            //  System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
         }
         log.info("Added CA Trust Store value {}....",System.getProperty("javax.net.ssl.trustStore"));
     }
 
 } 
 
