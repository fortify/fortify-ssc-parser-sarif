/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
 * 
 * TODO: Move to fortify-ssc-parser-util-common after PoC validation
 ******************************************************************************/
package com.fortify.ssc.parser.sarif.cache;

import java.io.IOException;

import org.slf4j.Logger;

/**
 * Utility methods for working with CachedObject collections.
 * 
 * Reduces boilerplate in getter methods by centralizing:
 * - Null checks
 * - Bounds checks
 * - Exception handling
 * - Logging
 */
public final class CachedObjectUtil {

    private CachedObjectUtil() {
    } // Static utility class

    /**
     * Get object from cached list with full error handling.
     * 
     * **Usage in getters:**
     * ```java
     * public Artifact getArtifactByIndex(Integer index) {
     * return CachedObjectUtil.getOrNull(artifactsByIndex, index, LOG, "artifact");
     * }
     * ```
     * 
     * @param <T>        Object type
     * @param list       List of CachedObject
     * @param index      Index to retrieve
     * @param logger     Logger for warnings/errors
     * @param objectName Name of object type (for logging: "artifact", "rule", etc.)
     * @return Object if found and unwrapped, null otherwise
     */
    public static <T> T getOrNull(java.util.List<CachedObject<T>> list, Integer index,
            Logger logger, String objectName) {
        // Null or empty check
        if (index == null || list == null || list.isEmpty()) {
            return null;
        }

        // Bounds check
        if (index < 0 || index >= list.size()) {
            logger.warn("SARIF input error: Invalid {} index {}", objectName, index);
            return null;
        }

        // Unwrap CachedObject
        try {
            CachedObject<T> cached = list.get(index);
            return cached.getOrReload();
        } catch (IOException e) {
            logger.error("Failed to reload {} at index {}", objectName, index, e);
            return null;
        }
    }
}