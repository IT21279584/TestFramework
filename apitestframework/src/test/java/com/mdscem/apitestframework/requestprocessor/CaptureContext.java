package com.mdscem.apitestframework.requestprocessor;

import java.util.HashMap;
import java.util.Map;

public class CaptureContext {

    private static CaptureContext instance; // Singleton instance
    private final Map<String, Map<String, Object>> captureMap = new HashMap<>();

    // Private constructor to prevent instantiation
    private CaptureContext() {}

    // Get the singleton instance of CaptureContext
    public static synchronized CaptureContext getInstance() {
        if (instance == null) {
            instance = new CaptureContext();
        }
        return instance;
    }

    public Map<String, Map<String, Object>> getCaptureMap() {
        return captureMap;
    }

    // Add captures for a specific test case
    public void addCapturesForTestCase(String testCaseName, Map<String, Object> captures) {
        captureMap.put(testCaseName, captures);
    }

    // Clear captures (optional for resetting between tests)
    public void clearCaptures() {
        captureMap.clear();
    }
}
