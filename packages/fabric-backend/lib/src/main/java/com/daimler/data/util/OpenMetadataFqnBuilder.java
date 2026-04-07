package com.daimler.data.util;

import java.util.regex.Pattern;
import java.util.Objects;

/**
 * Utility class for building Fully Qualified Names (FQNs) for OpenMetadata entities.
 * FQNs follow the format: "component1.component2.component3"
 */
public class OpenMetadataFqnBuilder {
    private static final String SEPARATOR = ".";
    private static final Pattern VALID_COMPONENT = Pattern.compile("^[a-zA-Z0-9_-]+$");
    
    /**
     * Builds an FQN from the given components.
     * 
     * @param components the parts of the FQN (e.g., service, database, schema)
     * @return the constructed FQN string
     * @throws FqnConstructionException if components are invalid
     */
    public static String build(String... components) {
        Objects.requireNonNull(components, "FQN components cannot be null");
        
        if (components.length == 0) {
            throw new FqnConstructionException("At least one FQN component required");
        }
        
        StringBuilder fqn = new StringBuilder();
        for (int i = 0; i < components.length; i++) {
            String component = components[i];
            validateComponent(component);
            
            fqn.append(component);
            if (i < components.length - 1) {
                fqn.append(SEPARATOR);
            }
        }
        
        return fqn.toString();
    }
    
    /**
     * Validates a single FQN component.
     * 
     * @param component the component to validate
     * @throws FqnConstructionException if component is invalid
     */
    private static void validateComponent(String component) {
        if (component == null || component.isEmpty()) {
            throw new FqnConstructionException("FQN component cannot be null or empty");
        }
        if (!VALID_COMPONENT.matcher(component).matches()) {
            throw new FqnConstructionException(
                String.format("Invalid FQN component '%s'. Only alphanumerics, '-', and '_' are allowed", component));
        }
    }

    /**
     * Exception thrown when FQN construction fails due to invalid input.
     */
    public static class FqnConstructionException extends RuntimeException {
        public FqnConstructionException(String message) {
            super(message);
        }
        
        public FqnConstructionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}