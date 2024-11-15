package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FlowContext {

    public Map<String, Flow> flowMap = new HashMap<>();

    public Map<String,TestCase> testCaseMap = new HashMap<>();

    public Map<String, TestCase> getTestCaseMap() {
        return testCaseMap;
    }

}
