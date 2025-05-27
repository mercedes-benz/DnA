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

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.filter.JWTAuthenticationFilter;
import com.daimler.data.application.intercepter.MatomoIntercepter;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.ThreadLocalTargetSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.http.HttpHost;

@Configuration
//@EnableWebMvc
@EnableScheduling
public class WebConfig implements WebMvcConfigurer {

	@Value("${drd.certificate-file}")
	private String certificateFile;

	@Value("${drd.certificate-pass}")
	private String certificatePass;

	// @Value("${environment}")
	// private String env;

	@Value("${allowedCorsOriginPatternUrl}")
	private String corsOriginUrl;

	@Value("${proxy.host}")
    private String proxyHost;
   
    @Value("${proxy.port}")
    private String proxyPort;

	@Autowired
	private JWTAuthenticationFilter filter;

	@Autowired
	private MatomoIntercepter tracker;

	@Override
	public void addCorsMappings(CorsRegistry registry) {

		registry.addMapping("/**").allowedMethods("GET", "PUT", "POST", "OPTIONS");
	}

	@Bean
	public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);

		RestTemplate restTemplate = new RestTemplate(requestFactory);
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(new ObjectMapper());
		restTemplate.getMessageConverters().add(converter);
		return restTemplate;
	}

@Bean
public RestTemplate proxyRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
	TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

	SSLContext sslContext = null;
		sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();
	SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
	int port = Integer.parseInt(proxyPort);
	HttpHost proxy = new HttpHost(proxyHost, port);
	CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).setProxy(proxy).build();

	HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
	requestFactory.setHttpClient(httpClient);
   
	RestTemplate restTemplate = new RestTemplate(requestFactory);
	MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
	converter.setObjectMapper(new ObjectMapper());
	restTemplate.getMessageConverters().add(converter);
	return restTemplate;
}

	@Bean
	public FilterRegistrationBean corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		// config.addAllowedOrigin("*");
		config.addAllowedOriginPattern(corsOriginUrl);
		config.addAllowedHeader("*");
		config.addAllowedMethod("OPTIONS");
		config.addAllowedMethod("HEAD");
		config.addAllowedMethod("GET");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("DELETE");
		config.addAllowedMethod("PATCH");
		source.registerCorsConfiguration("/**", config);
		final FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
		bean.setOrder(0);
		return bean;
	}

	@Bean
	public FilterRegistrationBean<JWTAuthenticationFilter> authtenticatonFilter() {
		FilterRegistrationBean<JWTAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();

		registrationBean.setFilter(filter);
		registrationBean.addUrlPatterns("/api/*");

		return registrationBean;
	}

	@Bean(destroyMethod = "destroy")
	public ThreadLocalTargetSource threadLocalTenantStore() {
		ThreadLocalTargetSource result = new ThreadLocalTargetSource();
		result.setTargetBeanName("userStore");
		return result;
	}

	@Primary
	@Bean(name = "proxiedThreadLocalTargetSource")
	public ProxyFactoryBean proxiedThreadLocalTargetSource(ThreadLocalTargetSource threadLocalTargetSource) {
		ProxyFactoryBean result = new ProxyFactoryBean();
		result.setTargetSource(threadLocalTargetSource);
		return result;
	}

	@Bean(name = "userStore")
	@Scope(scopeName = "prototype")
	public UserStore userStore() {
		return new UserStore();
	}

	@Bean
	public GraphQLScalarType dateTimeType() {
		return ExtendedScalars.DateTime;
	}

	@Bean
	public GraphQLScalarType dateType() {
		return ExtendedScalars.Date;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(tracker);
	}

}
