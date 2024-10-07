package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.mdscem.apitestframework.fileprocessor.fileinterpreter.FileInterpreter;

import java.util.List;

public class ApiTestMain {

    public static void main(String[] args) throws Exception {

        List<String> testCaseFiles= FileConfigLoader.loadTestCasesFiles();
        JsonNode jsonNode = FileConfigLoader.readFile(testCaseFiles);
//        System.out.println(jsonNode.toPrettyString());
        System.out.println(FileInterpreter.interpret(jsonNode));

    }
}
