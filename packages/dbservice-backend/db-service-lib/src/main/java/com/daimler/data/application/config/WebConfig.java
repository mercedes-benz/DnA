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

 import java.util.Arrays;
 
 import org.springframework.aop.framework.ProxyFactoryBean;
 import org.springframework.aop.target.ThreadLocalTargetSource;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.boot.web.servlet.FilterRegistrationBean;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Primary;
 import org.springframework.context.annotation.Scope;
 import org.springframework.scheduling.annotation.EnableScheduling;
 import org.springframework.web.cors.CorsConfiguration;
 import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
 import org.springframework.web.filter.CorsFilter;
 import org.springframework.web.servlet.config.annotation.CorsRegistry;
 import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
 import com.daimler.data.application.auth.UserStore;
 import com.daimler.data.application.filter.JWTAuthenticationFilter;
 import org.springframework.web.util.UrlPathHelper;
 
 @Configuration
 @EnableScheduling
 public class WebConfig implements WebMvcConfigurer {
 
     @Value("${allowedCorsOriginPatternUrl}")
     private String corsOriginUrl;
 
     @Autowired
     private JWTAuthenticationFilter filter;
 
     @Override
     public void addCorsMappings(CorsRegistry registry) {
 
         registry.addMapping("/**").allowedMethods("GET", "PUT", "POST", "OPTIONS");
     }
 
     @Bean
     public FilterRegistrationBean corsFilter() {
         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
         CorsConfiguration config = new CorsConfiguration();
         config.setAllowCredentials(true);
         if (corsOriginUrl != null) {
             if (corsOriginUrl.contains(",")) {
                 String[] allowedUrls = corsOriginUrl.split(",");
                 Arrays.asList(allowedUrls).forEach(x -> config.addAllowedOriginPattern(x));
             } else {
                 config.addAllowedOriginPattern(corsOriginUrl);
             }
         }
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
 
 }
 