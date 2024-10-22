package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Component
public class TestCaseContext {

    public Map<String, TestCase> testCaseMap = new HashMap<>();

}
