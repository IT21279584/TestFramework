package com.mdscem.apitestframework.fileprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.context.TestCaseRepository;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestCaseProcessor {

    @Autowired
    private TestCaseRepository testCaseRepository;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Process each TestCase with flow-specific data (pathParam, queryParam, delay, etc.).
     */
    public JsonNode processTestCaseWithFlowData(TestCase testCase, JsonNode testCaseNode, JsonNode combinedValuesNode, JsonNode flowsData) {
        ObjectNode updatedTestCase = objectMapper.createObjectNode();

        updatedTestCase.set("testCaseName", testCaseNode.get("testCaseName"));
        updatedTestCase.set("baseUri", testCaseNode.get("baseUri"));
        updatedTestCase.set("auth", testCaseNode.get("auth"));

        // Create the request object node to include pathParam and queryParam
        ObjectNode requestNode = objectMapper.createObjectNode();

        for (JsonNode flowSection : flowsData) {
            if (flowSection.has("name")) {
                String flowName = flowSection.get("name").asText();
                if (testCase.getTestCaseName().equals(flowName)) {

                    // Check for pathParam and queryParam before setting
                    if (flowSection.has("pathParam")) {
                        requestNode.set("pathParam", mergeParams(flowSection.get("pathParam"), combinedValuesNode));
                    }
                    if (flowSection.has("queryParam")) {
                        requestNode.set("queryParam", mergeParams(flowSection.get("queryParam"), combinedValuesNode));
                    }

                    updatedTestCase.set("delay", flowSection.has("delay") ? flowSection.get("delay") : objectMapper.nullNode());
                    break;
                }
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
        return finalResult;
    }


    /**
     * Merge parameters with values from combinedValuesNode.
     */
    private static JsonNode mergeParams(JsonNode paramsNode, JsonNode combinedValuesNode) {
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
     * Replace testcase in the request body with values from combinedValuesNode.
     */
    private static JsonNode replacePlaceholdersInBody(JsonNode bodyNode, JsonNode combinedValuesNode) {
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
    public void saveTestCases(JsonNode processedTestCase) {
        TestCase testCase = objectMapper.convertValue(processedTestCase, TestCase.class);
        testCaseRepository.save(testCase);
    }

    /**
     * Merges missing fields from `testCaseNode` into `updatedTestCase` recursively.
     */
    public static JsonNode mergeMissingFields(JsonNode testCaseNode, ObjectNode updatedTestCase) {
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
