package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TestContext {

    //map that passes to req processor
    private Map<String, Flow> flowMap = new HashMap<>();

    //map that contains test case names with validated testcases without flow values
    private Map<String,TestCase> testCaseMap = new HashMap<>();


    //getters
    public Map<String, Flow> getFlowMap() {
        return flowMap;
    }

    public Map<String, TestCase> getTestCaseMap() {
        return testCaseMap;
    }

}
