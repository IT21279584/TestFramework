package com.mdscem.apitestframework.fileprocessor.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.fileprocessor.TestCaseProcessor;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static com.mdscem.apitestframework.fileprocessor.TestCaseProcessor.jsonNodeToTestCase;

/**
 * Component class for replacing placeholders in test cases with actual values.
 * It also supports flow-specific replacements for path parameters, query parameters, and delays.
 */
@Component
public class TestCaseReplacer {

    @Autowired
    private TestCaseProcessor testCaseProcessor;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Replaces placeholders in a given JsonNode (testCaseNode) with values from another JsonNode (valuesNode).
     * Handles both single objects and arrays of objects.
     *
     * @param testCaseNode JsonNode representing the test case data.
     * @param valuesNode   JsonNode containing replacement values for placeholders.
     * @return Updated JsonNode with placeholders replaced.
     * @throws IOException if there is an issue during processing.
     */
    public static JsonNode replacePlaceholdersInNode(JsonNode testCaseNode, JsonNode valuesNode) throws IOException {
        if (testCaseNode.isArray()) {
            // Process each element in the array
            for (int i = 0; i < testCaseNode.size(); i++) {
                JsonNode element = testCaseNode.get(i);
                JsonNode modifiedElement = replacePlaceholders(element, valuesNode);
                ((ObjectNode) testCaseNode).set(String.valueOf(i), modifiedElement);
            }
        } else {
            // Replace placeholders directly for single objects
            testCaseNode = replacePlaceholders(testCaseNode, valuesNode);
        }

        return testCaseNode;
    }

    /**
     * Recursively replaces placeholders in a JsonNode with actual values from another JsonNode.
     * Handles nested objects, arrays, and textual placeholders.
     *
     * @param testCaseNode JsonNode to process and replace placeholders in.
     * @param valuesNode   JsonNode containing replacement values.
     * @return Updated JsonNode with placeholders replaced.
     */
    public static JsonNode replacePlaceholders(JsonNode testCaseNode, JsonNode valuesNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = testCaseNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode valueNode = field.getValue();

            // Check for textual placeholders matching "{{include ...}}"
            if (valueNode.isTextual() && valueNode.asText().startsWith("{{include ") && valueNode.asText().endsWith("}}")) {
                String placeholderKey = valueNode.asText().substring(10, valueNode.asText().length() - 2).trim();

                if (valuesNode.has(placeholderKey)) {
                    JsonNode replacementNode = valuesNode.get(placeholderKey);

                    // Replace with value or object as necessary
                    if (replacementNode.isValueNode()) {
                        ((ObjectNode) testCaseNode).put(field.getKey(), replacementNode.asText());
                    } else {
                        ((ObjectNode) testCaseNode).set(field.getKey(), replacementNode);
                    }
                } else {
                    throw new IllegalArgumentException("Missing replacement value for placeholder: " + placeholderKey);
                }
            } else if (valueNode.isObject()) {
                // Recursively process nested objects
                replacePlaceholders(valueNode, valuesNode);
            } else if (valueNode.isArray()) {
                // Process each element in arrays
                for (JsonNode arrayElement : valueNode) {
                    replacePlaceholders(arrayElement, valuesNode);
                }
            }
        }

        // Ensure no unresolved placeholders remain
        validateNoPlaceholdersRemaining(testCaseNode);

        return testCaseNode;
    }

    /**
     * Validates that no unresolved placeholders are left in the JsonNode.
     *
     * @param node JsonNode to validate.
     */
    private static void validateNoPlaceholdersRemaining(JsonNode node) {
        if (node.isTextual() && node.asText().matches("\\{\\{.*\\}\\}")) {
            throw new IllegalArgumentException("Unresolved placeholder found: " + node.asText());
        } else if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                validateNoPlaceholdersRemaining(fields.next().getValue());
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                validateNoPlaceholdersRemaining(arrayElement);
            }
        }
    }

    /**
     * Replaces placeholders in a TestCase object with flow-specific data like path parameters, query parameters, and delays.
     *
     * @param testCase   TestCase object to process.
     * @param flowsData  JsonNode containing flow-specific replacement data.
     * @return Updated TestCase object with flow-specific data.
     */
    public TestCase replaceTestCaseWithFlowData(TestCase testCase, JsonNode flowsData) {
        ObjectNode updatedTestCase = objectMapper.createObjectNode();
        JsonNode testCaseNode = testCaseProcessor.convertToJsonNode(testCase);

        // Create the request node to hold path and query parameters
        ObjectNode requestNode = objectMapper.createObjectNode();

        for (JsonNode flowSection : flowsData) {
            if (flowSection.has("name")) {
                String flowName = flowSection.get("name").asText();
                if (testCase.getTestCaseName().equals(flowName)) {
                    // Add path and query parameters if present
                    requestNode.set("pathParam", flowSection.get("pathParam"));
                    requestNode.set("queryParam", flowSection.get("queryParam"));

                    // Add delay if available
                    updatedTestCase.set("delay", flowSection.has("delay") ? flowSection.get("delay") : objectMapper.nullNode());
                    break;
                }
            }
        }

        // Add the request object to the updated test case
        updatedTestCase.set("request", requestNode);

        // Set the response data from the original test case
        updatedTestCase.set("response", testCaseNode.get("response"));

        // Merge the updated fields back into the test case
        JsonNode finalResult = testCaseProcessor.mergeMissingFields(testCaseNode, updatedTestCase);
        return jsonNodeToTestCase(finalResult);
    }
}
