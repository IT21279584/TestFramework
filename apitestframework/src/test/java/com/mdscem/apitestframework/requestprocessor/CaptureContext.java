package com.mdscem.apitestframework.requestprocessor;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CaptureContext {

    // Outer map key is the test case name, inner map stores captures for the test case
    private final Map<String, Map<String, Object>> captureMap = new HashMap<>();

    public Map<String, Map<String, Object>> getCaptureMap() {
        return captureMap;
    }

    // Add  captures for a specific test case
    public void addCapturesForTestCase(String testCaseName, Map<String, Object> captures) {
        captureMap.put(testCaseName, captures);
    }
}
