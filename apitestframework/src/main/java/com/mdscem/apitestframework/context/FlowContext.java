package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FlowContext {

    private Map<String, Flow> flowMap = new HashMap<>();

    private Map<String,TestCase> testCaseMap = new HashMap<>();

    public Map<String, Flow> getFlowMap() {
        return flowMap;
    }

    public Map<String, TestCase> getTestCaseMap() {
        return testCaseMap;
    }

}
