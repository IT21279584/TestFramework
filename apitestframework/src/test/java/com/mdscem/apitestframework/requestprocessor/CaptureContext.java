package com.mdscem.apitestframework.requestprocessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CaptureContext {
    private static final Logger logger = LogManager.getLogger(CaptureReplacer.class);

    private  static Map<String, Map<String, Object>> captureMap;

    public CaptureContext() {
      this.captureMap = new HashMap<>();
    }

    public Map<String, Map<String, Object>> getCaptureMap() {
        return captureMap;
    }

    // Add captures for a specific test case
    public void addCapturesForTestCase(String testCaseName, Map<String, Object> captures) {
        captureMap.put(testCaseName, captures);
    }

    public void setCaptureMap(Map<String, Map<String, Object>> captureMap) {
        this.captureMap = captureMap;
    }
}
