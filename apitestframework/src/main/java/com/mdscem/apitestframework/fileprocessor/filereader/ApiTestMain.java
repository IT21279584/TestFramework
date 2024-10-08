package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.mdscem.apitestframework.fileprocessor.fileinterpreter.FileInterpreter;
import com.mdscem.apitestframework.fileprocessor.yamlprocessor.YamlPlaceholderReplacer;

import java.util.List;

public class ApiTestMain {

    public static void main(String[] args) throws Exception {
        YamlPlaceholderReplacer replacer = new YamlPlaceholderReplacer();
        String yamlFilePath = "/home/kmedagoda/Documents/Kavinda Final/final TestFramework/TestFramework/apitestframework/src/main/resources/testcase1.yaml";
        String propertiesFilePath = "/home/kmedagoda/Documents/Kavinda Final/final TestFramework/TestFramework/apitestframework/src/main/resources/values.yaml";
        replacer.replaceYamlPlaceholders(yamlFilePath, propertiesFilePath);


        List<String> testCaseFiles= FileConfigLoader.loadTestCasesFiles();
        JsonNode jsonNode = FileConfigLoader.readFile(testCaseFiles);
//        System.out.println(jsonNode.toPrettyString());
//        System.out.println(FileInterpreter.interpret(jsonNode).get(0));

    }
}
