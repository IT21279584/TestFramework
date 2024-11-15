package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TestCaseContext {

    public Map<String, FlowObject> flowObjectMap = new HashMap<>();

    public Map<String,TestCase> testCaseMap = new HashMap<>();

    public Map<String, FlowObject> getFlowObjectMap() {
        return flowObjectMap;
    }

    public void setFlowObjectMap(Map<String, FlowObject> flowObjectMap) {
        this.flowObjectMap = flowObjectMap;
    }

    public Map<String, TestCase> getTestCaseMap() {
        return testCaseMap;
    }

    public void setTestCaseMap(Map<String, TestCase> testCaseMap) {
        this.testCaseMap = testCaseMap;
    }
}
