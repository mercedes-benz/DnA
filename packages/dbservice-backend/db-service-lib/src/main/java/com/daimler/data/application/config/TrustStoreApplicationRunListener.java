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