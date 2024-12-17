package com.mdscem.apitestframework.requestprocessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class CaptureContext {

    private  static Map<String, Map<String, Object>> captureMap;

    public CaptureContext() {
      this.captureMap = new HashMap<>();
    }

    public static Map<String, Map<String, Object>> getCaptureMap() {
        return captureMap;
    }

    // Add captures for a specific test case
    public static void addCapturesForTestCase(String testCaseName, Map<String, Object> captures) {
        captureMap.put(testCaseName, captures);
    }
}
