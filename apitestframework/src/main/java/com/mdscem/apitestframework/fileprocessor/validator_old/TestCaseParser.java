package com.mdscem.apitestframework.fileprocessor.validator_old;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Map;

public class TestCaseParser {

    private static final Yaml yaml = new Yaml();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static TestCase parseTestCase(String yamlContent) throws IOException {
        // Parse the YAML string into a Map
        Map<String, Object> caseMap = yaml.load(yamlContent);

        // Convert the Map into a TestCase object
        return objectMapper.convertValue(caseMap, TestCase.class);
    }
}
