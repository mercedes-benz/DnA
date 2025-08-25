package com.daimler.data.util;

import java.util.List;

public class ExclusionUtils {

    public static boolean isExcluded(String requestUri, List<String> exclusionPatterns) {
        return exclusionPatterns.stream().anyMatch(requestUri::startsWith);
    }
}
