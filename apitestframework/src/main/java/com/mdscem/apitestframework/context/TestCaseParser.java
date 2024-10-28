package com.mdscem.apitestframework.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TestCaseParser {

    public List<TestCase> parseTestCases(String content) {
        List<TestCase> testCases = new ArrayList<>();

        if (content.trim().startsWith("{")) {
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                TestCase[] parsedCases = objectMapper.readValue(content, TestCase[].class);
                for (TestCase testCase : parsedCases) {
                    testCases.add(testCase);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Yaml yaml = new Yaml();
            List<Map<String, Object>> parsedData = yaml.load(content);
            for (Map<String, Object> entry : parsedData) {
                TestCase testCase = new TestCase();
                testCase.setTestCaseId(entry.get("testCaseId").toString());
                testCase.setBaseUri(entry.get("baseUri").toString());
                testCases.add(testCase);
            }
        }
        return testCases;
    }
}
