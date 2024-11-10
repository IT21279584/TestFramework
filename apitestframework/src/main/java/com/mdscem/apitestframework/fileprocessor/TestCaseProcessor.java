package com.mdscem.apitestframework.fileprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.context.TestCaseRepository;
import com.mdscem.apitestframework.fileprocessor.filereader.FlowBasedTestCaseReader;
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

    @Autowired
    private FlowBasedTestCaseReader flowBasedTestCaseReader;

    public void processTestCasesFromFlows(String flowsFilePath, String testCaseDirectory, List<JsonNode> includeNodes) throws IOException {
        // Load ordered test cases with merged parameters based on flows
        List<JsonNode> orderedTestCases = flowBasedTestCaseReader.loadTestCasesByFlow(flowsFilePath, testCaseDirectory);

        for (JsonNode testCaseNode : orderedTestCases) {
            // Combine include nodes into a values node
            JsonNode combinedValuesNode = combineIncludeNodes(includeNodes);

            // Replace placeholders in the current test case node
            TestCase[] finalResult = testCaseReplacer.replacePlaceholdersInNode(testCaseNode, combinedValuesNode);

            // Save the processed test cases
            saveTestCases(finalResult);
        }
    }

    private JsonNode combineIncludeNodes(List<JsonNode> includeNodes) {
        ObjectNode combinedValuesNode = new ObjectMapper().createObjectNode();
        for (JsonNode includeNode : includeNodes) {
            includeNode.fields().forEachRemaining(entry -> combinedValuesNode.set(entry.getKey(), entry.getValue()));
        }
        return combinedValuesNode;
    }




//    public void processTestCases(String testCaseFilePath, List<JsonNode> includeNodes) throws IOException {
//        // Combine all include nodes into a single values node
//        ObjectNode combinedValuesNode = objectMapper.createObjectNode();
//        for (JsonNode includeNode : includeNodes) {
//            includeNode.fields().forEachRemaining(entry -> combinedValuesNode.set(entry.getKey(), entry.getValue()));
//        }
//
//        // Load the test case and values for the current file
//        JsonNode testCaseNode = testCasesToJsonNodeReader.loadFileAsJsonNode(testCaseFilePath);
//
//        // Load flows files
//        JsonNode flowsNodes = testCasesToJsonNodeReader.loadFlowFilesAsJsonNodes(Constant.FLOWS_DIRECTORY);
//
//        // Replace placeholders in the current test case node using the combined values node
//        TestCase[] finalResult = testCaseReplacer.replacePlaceholdersInNode(testCaseNode, combinedValuesNode);
//
//        // Replace flows placeholders and convert to TestCase[]
//
//        System.out.println("Final Result as TestCase Array: " + finalResult.toString());
//
//        // Handle saving of test cases
//        saveTestCases(finalResult);
//    }


    private void saveTestCases(TestCase[] testCases) {
        for (TestCase testCase : testCases) {
            testCaseRepository.save(testCase);
        }
    }
}
