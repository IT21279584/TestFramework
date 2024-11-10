package com.mdscem.apitestframework;

import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.fileprocessor.TestCaseProcessor;
import com.mdscem.apitestframework.fileprocessor.filereader.TestCasesToJsonNodeReader;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.IOException;
import java.util.List;

import static com.mdscem.apitestframework.constants.Constant.FLOWS_DIRECTORY;
import static com.mdscem.apitestframework.constants.Constant.TEST_CASES_DIRECTORY;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApiTestMain implements CommandLineRunner {

    @Autowired
    private TestCaseProcessor testCaseProcessor;

    @Autowired
    private TestCasesToJsonNodeReader testCasesToJsonNodeReader;


    @Override
    public void run(String... args) {
        try {
            // Load include files only once
            List<JsonNode> includeNodes = testCasesToJsonNodeReader.loadIncludeFilesAsJsonNodes(Constant.INCLUDES_DIRECTORY);
            testCaseProcessor.processTestCasesFromFlows(FLOWS_DIRECTORY, TEST_CASES_DIRECTORY, includeNodes);

        } catch (IOException e) {
            System.err.println("Error occurred while loading files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
