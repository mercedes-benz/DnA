package com.daimler.data.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
public class RestConfig {

    @Value("${proxy.host}")
	private String proxyHost;
	
	@Value("${proxy.port}")
	private String proxyPort;


    @Bean
    public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = null;
        sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(csf)
                        .build())
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpclient);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        restTemplate.getMessageConverters().add(converter);
        return restTemplate;

    }

     @Bean
    @Lazy
    public RestTemplate proxyRestTemplate() throws Exception {
        // Trust strategy that accepts all certificates (no validation)
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        // Create an SSL context with the trust strategy
        SSLContext sslContext = null;
        sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        // Create an SSL connection socket factory with the SSL context
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        // Parse the proxy port to an integer
        int port = Integer.parseInt(proxyPort);

        // Create an HttpHost object representing the proxy
        HttpHost proxy = new HttpHost(proxyHost, port);

        // Create an HttpClient that uses the proxy and SSL connection socket factory
        // CloseableHttpClient httpclient = HttpClients.custom()
        // .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
        //         .setSSLSocketFactory(csf).
        //         .build())
        // .build();
        CloseableHttpClient httpClient = HttpClients.custom()
        .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(csf) // Configure SSL factory
                .build())
        .setProxy(proxy) // Set the proxy here
        .build();

        // Create a request factory using the custom HttpClient
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        
        // Create a RestTemplate with the custom request factory
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        // Add a JSON message converter with a custom ObjectMapper
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        restTemplate.getMessageConverters().add(converter);

        // Return the configured RestTemplate
        return restTemplate;
    }



}