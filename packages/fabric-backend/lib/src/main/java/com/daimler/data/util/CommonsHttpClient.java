package com.daimler.data.util;

import com.databricks.sdk.core.http.Request;
import com.databricks.sdk.core.http.Response;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.net.URL;

public class CommonsHttpClient implements com.databricks.sdk.core.http.HttpClient {

    private final org.apache.http.client.HttpClient httpClient;

    private CommonsHttpClient(org.apache.http.client.HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Response execute(Request request) {

        try {

            HttpRequestBase apacheRequest = (HttpRequestBase) RequestBuilder
                    .create(request.getMethod())
                    .setUri(request.getUri())
                    .build();

            CloseableHttpResponse apacheResponse =
                    (CloseableHttpResponse) httpClient.execute(apacheRequest);

            int statusCode = apacheResponse.getStatusLine().getStatusCode();

            return new Response(
                    String.valueOf(statusCode),
                    new URL(request.getUri().toString())
            );

        } catch (Exception e) {
            throw new RuntimeException("HTTP request failed", e);
        }
    }

    public static class Builder {

        private ProxyConfig proxyConfig;

        public Builder withProxyConfig(ProxyConfig proxyConfig) {
            this.proxyConfig = proxyConfig;
            return this;
        }

        public CommonsHttpClient build() {

            org.apache.http.client.HttpClient client;

            if (proxyConfig != null) {

                HttpHost proxy = new HttpHost(
                        proxyConfig.getHost(),
                        proxyConfig.getPort()
                );

                client = HttpClients.custom()
                        .setProxy(proxy)
                        .build();

            } else {
                client = HttpClients.createDefault();
            }

            return new CommonsHttpClient(client);
        }
    }
}