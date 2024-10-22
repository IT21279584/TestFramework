package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class TestCaseContext {

    private Map<String, TestCase> testCaseMap = new HashMap<>();

    public Map<String, TestCase> getTestCaseMap() {
        return testCaseMap;
    }
}
