package com.daimler.data.application.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Configuration;
 
@Configuration

public class ProxyConfig {
 
    @Value("${proxy.host}")

    private String host;
 
    @Value("${proxy.port}")

    private String port;
 
    // @Value("${proxy.username:}")

    private String username="";
 
    // @Value("${proxy.password:}")

    private String password="";
 
    @PostConstruct

    public void setProxy() {

        System.setProperty("https.proxyHost", host);

        System.setProperty("https.proxyPort", port);
 
        System.setProperty("http.proxyHost", host);

        System.setProperty("http.proxyPort", port);
 
        if (!username.isEmpty()) {

            System.setProperty("https.proxyUser", username);

            System.setProperty("https.proxyPassword", password);

        }

    }

}
 