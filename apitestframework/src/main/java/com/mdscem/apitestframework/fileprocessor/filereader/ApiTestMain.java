package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.mdscem.apitestframework.fileprocessor.validator.PlaceholderReplacer;
import org.apache.log4j.BasicConfigurator;

import java.util.List;

public class ApiTestMain {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        PlaceholderReplacer replacer = new PlaceholderReplacer();
        String propertyValues = "/home/kmedagoda/Documents/Kavinda Final/final TestFramework/TestFramework/apitestframework/src/main/resources/values.yaml";
        String yamlValues = "/home/kmedagoda/Documents/Kavinda Final/final TestFramework/TestFramework/apitestframework/src/main/resources/testcase1.yaml";
        replacer.replacePlaceholders(yamlValues,propertyValues);
        List<String> testCaseFiles= FileConfigLoader.loadTestCasesFiles();
        JsonNode jsonNode = FileConfigLoader.readFile(testCaseFiles);

    }
}
