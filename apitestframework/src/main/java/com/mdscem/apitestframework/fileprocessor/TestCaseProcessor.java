package com.mdscem.apitestframework.fileprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.context.TestCaseRepository;
import com.mdscem.apitestframework.fileprocessor.filereader.FlowBasedTestCaseReader;
import com.mdscem.apitestframework.fileprocessor.filereader.TestCasesToJsonNodeReader;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    private FlowBasedTestCaseReader flowBasedTestCaseReader;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Process Test Cases from Flow Data and Merge Placeholders
     */
    public void processTestCasesFromFlows(String flowsDirectoryPath, String testCaseDirectory, List<JsonNode> includeNodes) throws IOException {
        // Combine include nodes into a single JsonNode to apply to all test cases
        JsonNode combinedValuesNode = combineNodes(includeNodes);

        // Load flow data only once
        List<JsonNode> flowData = flowBasedTestCaseReader.getFlowData(flowsDirectoryPath);

        for (JsonNode flow : flowData) {
            // Load the test cases specific to this flow
            List<JsonNode> orderedTestCases = flowBasedTestCaseReader.loadTestCasesByFlow(flowsDirectoryPath, testCaseDirectory);

            // For each test case in this flow, replace placeholders and process
            for (JsonNode testCaseNode : orderedTestCases) {
                TestCase[] finalResults = testCaseReplacer.replacePlaceholdersInNode(testCaseNode, combinedValuesNode);

                for (TestCase testCase : finalResults) {
                    // Process the test case with flow-specific data (pathParam, queryParam, delay, etc.)
                    JsonNode processedTestCase = processTestCaseWithFlowData(testCase, testCaseNode, combinedValuesNode, flow);

                    // Save the processed test case for this flow
                    saveTestCases(processedTestCase);
                }
            }

            // Optional: If you want to log that a particular flow has been processed, you can add a log here.
//            System.out.println("Processed test cases for flow: " + flow.get("testCase").get("name").asText());
        }
    }


    /**
     * Combine multiple nodes into a single node.
     */
    private JsonNode combineNodes(List<JsonNode> node) {
        ObjectNode combinedValuesNode = objectMapper.createObjectNode();
        node.forEach(includeNode ->
                includeNode.fields().forEachRemaining(entry ->
                        combinedValuesNode.set(entry.getKey(), entry.getValue())
                )
        );
        return combinedValuesNode;
    }

    /**
     * Process each TestCase with flow-specific data (pathParam, queryParam, delay, etc.).
     */
    public JsonNode processTestCaseWithFlowData(TestCase testCase, JsonNode testCaseNode, JsonNode combinedValuesNode, JsonNode flowsData) {
        ObjectNode updatedTestCase = objectMapper.createObjectNode();

        updatedTestCase.set("testCaseName", testCaseNode.get("testCaseName"));
        updatedTestCase.set("baseUri", testCaseNode.get("baseUri"));
        updatedTestCase.set("auth", testCaseNode.get("auth"));

        System.out.println("My TestCase Node" + testCaseNode);

        // Create the request object node to include pathParam and queryParam
        ObjectNode requestNode = objectMapper.createObjectNode();

        for (JsonNode flowSection : flowsData) {
            String flowName = flowSection.get("testCase").get("name").asText();
            if (testCase.getTestCaseName().equals(flowName)) {
                JsonNode request = flowSection.get("testCase");

                // Set pathParam and queryParam within request node if they exist in flow data
                requestNode.set("pathParam", request.has("pathParam") ? mergeParams(request.get("pathParam"), combinedValuesNode) : objectMapper.createObjectNode());
                requestNode.set("queryParam", request.has("queryParam") ? mergeParams(request.get("queryParam"), combinedValuesNode) : objectMapper.createObjectNode());
                updatedTestCase.set("delay", request.has("delay") ? request.get("delay") : objectMapper.nullNode());

                break;
            }
        }

        // If the testCaseNode contains a body, replace placeholders and add it to the request
        if (testCaseNode.get("request").has("body")) {
            requestNode.set("body", replacePlaceholdersInBody(testCaseNode.get("request").get("body"), combinedValuesNode));
        }

        // Add the request object to updatedTestCase
        updatedTestCase.set("request", requestNode);

        // Set the response as usual
        updatedTestCase.set("response", testCaseNode.get("response"));

        JsonNode finalResult = mergeMissingFields(testCaseNode, updatedTestCase);
        System.out.println("Updated Test Case with Flow Data: " + finalResult);
        return updatedTestCase;
    }


    /**
     * Merge parameters with values from combinedValuesNode.
     */
    private JsonNode mergeParams(JsonNode paramsNode, JsonNode combinedValuesNode) {
        ObjectNode mergedParams = objectMapper.createObjectNode();
        paramsNode.fields().forEachRemaining(entry -> {
            String value = entry.getValue().asText();
            if (combinedValuesNode.has(value)) {
                mergedParams.set(entry.getKey(), combinedValuesNode.get(value));
            } else {
                mergedParams.set(entry.getKey(), entry.getValue());
            }
        });
        return mergedParams;
    }

    /**
     * Replace placeholders in the request body with values from combinedValuesNode.
     */
    private JsonNode replacePlaceholdersInBody(JsonNode bodyNode, JsonNode combinedValuesNode) {
        ObjectNode updatedBodyNode = bodyNode.deepCopy();
        updatedBodyNode.fields().forEachRemaining(entry -> {
            String placeholder = entry.getValue().asText();
            if (combinedValuesNode.has(placeholder)) {
                updatedBodyNode.set(entry.getKey(), combinedValuesNode.get(placeholder));
            }
        });
        return updatedBodyNode;
    }

    /**
     * Save the processed test case into the repository.
     */
    private void saveTestCases(JsonNode processedTestCase) {
        TestCase testCase = objectMapper.convertValue(processedTestCase, TestCase.class);
        testCaseRepository.save(testCase);
    }

    /**
     * Merges missing fields from `testCaseNode` into `updatedTestCase` recursively.
     */
    public JsonNode mergeMissingFields(JsonNode testCaseNode, ObjectNode updatedTestCase) {
        testCaseNode.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode sourceField = entry.getValue();

            if (updatedTestCase.has(fieldName)) {
                // If target already has the field, check if it's an object to merge recursively
                if (sourceField.isObject() && updatedTestCase.get(fieldName).isObject()) {
                    mergeMissingFields(sourceField, (ObjectNode) updatedTestCase.get(fieldName));
                }
            } else {
                // Otherwise, add the field from source to target
                updatedTestCase.set(fieldName, sourceField);
            }
        });
        return updatedTestCase;
    }
}
