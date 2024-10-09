package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class ApiTestMain {

    public static void main(String[] args) throws Exception {

        List<String> testCaseFiles= FileConfigLoader.loadTestCasesFiles();
        JsonNode jsonNode = FileConfigLoader.readFile(testCaseFiles);
        System.out.println(jsonNode.toPrettyString());

    }
}
