package com.mdscem.apitestframework.fileprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.context.TestCaseRepository;
import com.mdscem.apitestframework.fileprocessor.filereader.TestCasesToJsonNodeReader;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.io.IOException;
import java.util.List;

@Component
public class TestCaseProcessor {

    @Autowired
    private TestCaseReplacer testCaseReplacer;

    @Autowired
    private TestCasesToJsonNodeReader testCasesToJsonNodeReader;

    @Autowired
    private TestCaseRepository testCaseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void processTestCases(String testCaseFilePath, List<JsonNode> includeNodes) throws IOException {
        // Combine all include nodes into a single values node
        ObjectNode combinedValuesNode = objectMapper.createObjectNode();
        for (JsonNode includeNode : includeNodes) {
            includeNode.fields().forEachRemaining(entry -> combinedValuesNode.set(entry.getKey(), entry.getValue()));
        }

        System.out.println("My Includes ObjectNode " + combinedValuesNode.toPrettyString());

        // Load the test case and values for the current file
        JsonNode testCaseNode = testCasesToJsonNodeReader.loadFileAsJsonNode(testCaseFilePath);

        // Replace placeholders in the current test case node using the combined values node
        TestCase[] finalResult = testCaseReplacer.replacePlaceholdersInNode(testCaseNode, combinedValuesNode);
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
