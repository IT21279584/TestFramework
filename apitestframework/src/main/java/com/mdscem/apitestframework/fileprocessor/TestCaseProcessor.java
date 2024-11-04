package com.mdscem.apitestframework.fileprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.mdscem.apitestframework.context.TestCaseRepository;
import com.mdscem.apitestframework.fileprocessor.filereader.TestCasesToJsonNodeReader;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.io.IOException;

@Component
public class TestCaseProcessor {

    @Autowired
    private TestCaseReplacer testCaseReplacer;

    @Autowired
    private TestCasesToJsonNodeReader testCasesToJsonNodeReader;

    @Autowired
    private TestCaseRepository testCaseRepository;



    public void processTestCases(String testCaseFilePath, String valueFilePath) throws IOException {
        // Load the test case and values for the current file
        JsonNode testCaseNode = testCasesToJsonNodeReader.loadFileAsJsonNode(testCaseFilePath);
        JsonNode valuesNode = testCasesToJsonNodeReader.loadFileAsJsonNode(valueFilePath);

        // Replace placeholders in the current test case node using values node
        TestCase[] finalResult = testCaseReplacer.replacePlaceholdersInNode(testCaseNode, valuesNode);
        System.out.println("Final Result as TestCase Array: " + finalResult[0].toString());
        // Handle saving of test cases
        saveTestCases(finalResult);
    }


    private void saveTestCases(TestCase[] testCases) {
        for (TestCase testCase : testCases) {
            testCaseRepository.save(testCase);
        }
    }
}
