package com.daimler.data.util;

import java.util.List;
import java.util.regex.Pattern;

public class ExclusionUtils {

       public static boolean isExcluded(String requestUri, List<String> exclusionPatterns) {
        return exclusionPatterns.stream().anyMatch(pattern -> {
            String regex = pattern.replace("**", ".*"); // Convert wildcard to regex
            return Pattern.matches(regex, requestUri);
        });
    }
}
