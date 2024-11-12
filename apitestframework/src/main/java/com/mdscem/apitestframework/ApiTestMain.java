package com.mdscem.apitestframework;

import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.fileprocessor.TestCaseProcessor;
import com.mdscem.apitestframework.fileprocessor.filereader.TestCasesToJsonNodeReader;
import com.mdscem.apitestframework.fileprocessor.filereader.FlowBasedTestCaseReader;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.IOException;
import java.util.List;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApiTestMain implements CommandLineRunner {

    @Autowired
    private FlowBasedTestCaseReader flowBasedTestCaseReader;

    @Autowired
    private TestCasesToJsonNodeReader testCasesToJsonNodeReader;

    @Override
    public void run(String... args) {
        try {
            // Load include files and combine them into one node
            List<JsonNode> includeNodes = testCasesToJsonNodeReader.loadIncludeFilesAsJsonNodes(Constant.INCLUDES_DIRECTORY);
            JsonNode combinedValuesNode = TestCaseProcessor.combineNodes(includeNodes);

            // Load test cases by flow, passing the combinedValuesNode
            List<JsonNode> orderedTestCases = flowBasedTestCaseReader.loadTestCasesByFlow(Constant.FLOWS_DIRECTORY, Constant.TEST_CASES_DIRECTORY, combinedValuesNode);

        } catch (IOException e) {
            System.err.println("Error occurred while loading files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
