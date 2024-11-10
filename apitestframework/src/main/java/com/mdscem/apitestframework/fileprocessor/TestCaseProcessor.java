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
    public void processTestCasesFromFlows(String flowsFilePath, String testCaseDirectory, List<JsonNode> includeNodes) throws IOException {
        List<JsonNode> orderedTestCases = flowBasedTestCaseReader.loadTestCasesByFlow(flowsFilePath, testCaseDirectory);
        JsonNode combinedValuesNode = combineIncludeNodes(includeNodes);

        for (JsonNode testCaseNode : orderedTestCases) {
            TestCase[] finalResults = testCaseReplacer.replacePlaceholdersInNode(testCaseNode, combinedValuesNode);

            for (TestCase testCase : finalResults) {
                JsonNode flowData = flowBasedTestCaseReader.getFlowData(flowsFilePath);
                JsonNode processedTestCase = processTestCaseWithFlowData(testCase, testCaseNode, combinedValuesNode, flowData);
                saveTestCases(processedTestCase);
            }
        }
    }

    /**
     * Combine multiple include nodes into a single node.
     */
    private JsonNode combineIncludeNodes(List<JsonNode> includeNodes) {
        ObjectNode combinedValuesNode = objectMapper.createObjectNode();
        includeNodes.forEach(includeNode ->
                includeNode.fields().forEachRemaining(entry ->
                        combinedValuesNode.set(entry.getKey(), entry.getValue())
                )
        );
        return combinedValuesNode;
    }

    /**
     * Process each TestCase with flow-specific data (pathParam, queryParam, delay, etc.).
     */
    /**
     * Process each TestCase with flow-specific data (pathParam, queryParam, delay, etc.).
     */
    private JsonNode processTestCaseWithFlowData(TestCase testCase, JsonNode testCaseNode, JsonNode combinedValuesNode, JsonNode flowsData) {
        ObjectNode updatedTestCase = objectMapper.createObjectNode();
        updatedTestCase.set("testCaseName", testCaseNode.get("testCaseName"));
        updatedTestCase.set("baseUri", testCaseNode.get("baseUri"));
        updatedTestCase.set("auth", testCaseNode.get("auth"));

        // Create the request object node to include pathParam and queryParam
        ObjectNode requestNode = objectMapper.createObjectNode();

        for (JsonNode flowSection : flowsData) {
            String flowName = flowSection.get("testCaseName").asText();
            if (testCase.getTestCaseName().equals(flowName)) {
                JsonNode request = flowSection.get("request");

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


        System.out.println("Updated Test Case with Flow Data: " + updatedTestCase);
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
}
