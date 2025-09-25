package com.daimler.data.application.interceptor;

import com.daimler.data.application.annotation.RequiresApiKeyAuthorization;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Slf4j
@Component
public class ApiKeyAuthorizationInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${fabricWorkspaces.ada.apiKey}")
    private String adaApiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip if not a handler method
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // Check if the method has our annotation
        RequiresApiKeyAuthorization annotation = method.getAnnotation(RequiresApiKeyAuthorization.class);
        
        // If no annotation or annotation is not required, skip validation
        if (annotation == null || !annotation.required()) {
            return true;
        }

        // Extract and validate token
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader == null || authHeader.isBlank()) {
            log.error("Missing Authorization header for request: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header is required");
            return false;
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            log.error("Invalid token format for request: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format. Expected: Bearer <token>");
            return false;
        }

        String accessToken = authHeader.substring(BEARER_PREFIX.length()).trim();
        
        if (accessToken.isBlank()) {
            log.error("Empty token provided for request: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token cannot be empty");
            return false;
        }

        if (!isValidToken(accessToken)) {
            log.error("Invalid token provided for request: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return false;
        }

        log.debug("Token validation successful for: {}", request.getRequestURI());
        
        // Store token in request for later use if needed
        request.setAttribute("accessToken", accessToken);
        
        return true;
    }

    /**
     * Implement your token validation logic here
     */
    private boolean isValidToken(String accessToken) {
        try {
            return accessToken.length() >= 20 && accessToken.equals(adaApiKey);
            
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}