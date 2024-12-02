package com.mdscem.apitestframework.fileprocessor.flowprocessor;

import com.mdscem.apitestframework.context.Flow;

import java.util.HashMap;
import java.util.Map;

public class CaptureContext {
    private static final Map<String, Object> captureMap = new HashMap<>();

    public Map<String, Object> getCaptureMap() {
        return captureMap;
    }


}
