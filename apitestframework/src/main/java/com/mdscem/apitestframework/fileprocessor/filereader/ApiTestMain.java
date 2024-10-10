package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.mdscem.apitestframework.fileprocessor.yamlprocessor.YamlPlaceholderReplacer;
import org.apache.log4j.BasicConfigurator;

import java.util.List;

public class ApiTestMain {

    public static void main(String[] args) throws Exception {
        List<String> testCaseFiles= FileConfigLoader.loadTestCasesFiles();
        String content = FileConfigLoader.readFile(testCaseFiles);
        System.out.println(content);
    }
}
