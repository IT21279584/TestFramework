package com.mdscem.apitestframework.fileprocessor.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.fileprocessor.TestCaseProcessor;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.flowprocessor.FlowProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Component class for replacing placeholders in test cases with actual values.
 * It also supports flow-specific replacements for path parameters, query parameters, and delays.
 */
@Component
public class TestCaseReplacer {
    private static final Logger logger = LogManager.getLogger(FlowProcessor.class);
    @Autowired
    private TestCaseProcessor testCaseProcessor;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Replaces placeholders in a given JsonNode (testCaseNode) with values from another JsonNode (valuesNode).
     * Handles both single objects and arrays of objects.
     *
     * @param testCaseNode JsonNode representing the test case data.
     * @param valuesNode   JsonNode containing replacement values for placeholders.
     * @return Updated JsonNode with placeholders replaced.
     * @throws IOException if there is an issue during processing.
     */
    public JsonNode replacePlaceholder(JsonNode testCaseNode, JsonNode valuesNode) throws IOException {
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
    private JsonNode replacePlaceholders(JsonNode testCaseNode, JsonNode valuesNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = testCaseNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode valueNode = field.getValue();

            // Check for textual placeholders matching "{{include ...}}"
            if (valueNode.isTextual() && valueNode.asText().startsWith(Constant.INCLUDE_KEYWORD) && valueNode.asText().endsWith(Constant.END_DOUBLE_CURLY_BRACKET)) {
                // Extracts the key inside a placeholder like "{{includes key}}"
                // 1. valueNode.asText() -> "{{includes key}}"
                // 2. .substring(10, valueNode.asText().length() - 2)
                //    - 10 skips "{{includes " (length of the prefix)
                //    - length() - 2 removes "}}" (trims the suffix)
                // 3. .trim() ensures no extra spaces
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
    private void validateNoPlaceholdersRemaining(JsonNode node) {
        if (node.isTextual() && node.asText().matches(Constant.PLACEHOLDER_VALIDATOR)) {
            String errorMessage = "Unresolved placeholder found: " + node.asText();
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Replaces placeholders in a TestCase object with flow-specific data like path parameters, query parameters, and delays.
     *
     * @param testCase  TestCase object to process.
     * @param flowsData JsonNode containing flow-specific replacement data.
     * @return Updated TestCase object with flow-specific data.
     */
    public TestCase replaceTestCaseWithFlowData(TestCase testCase, JsonNode flowsData) {
        ObjectNode updatedTestCase = objectMapper.createObjectNode();
        JsonNode testCaseNode = testCaseProcessor.convertToJsonNode(testCase);

        // Create the request node to hold path and query parameters
        ObjectNode requestNode = objectMapper.createObjectNode();

        for (JsonNode flowSection : flowsData) {
            String flowName = flowSection.get(Constant.TESTCASE_NAME).asText();

            if (flowSection.has(Constant.TESTCASE_NAME) && testCase.getTestCaseName().equals(flowName)) {
                // Add path and query parameters
                requestNode.set(Constant.PATH_PARAM, flowSection.get(Constant.PATH_PARAM));
                requestNode.set(Constant.QUERY_PARAM, flowSection.get(Constant.QUERY_PARAM));
                // Add delay
                updatedTestCase.set(Constant.DELAY, flowSection.get(Constant.DELAY));
                JsonNode capture = flowSection.get(Constant.CAPTURE);

                // Check if capture exists and is not null
                if (capture != null && !capture.isNull()) {
                    if (capture.isArray()) {
                        // If capture is an array, convert to a map with null values
                        ObjectNode captureMap = objectMapper.createObjectNode();
                        for (JsonNode item : capture) {
                            captureMap.put(item.asText(), (JsonNode) null); // Add key with null value
                        }
                        updatedTestCase.set(Constant.CAPTURE, captureMap); // Set capture in updated test case

                    } else if (capture.isTextual()) {
                        // If capture is a string, set it directly with a null value
                        ObjectNode captureMap = objectMapper.createObjectNode();
                        captureMap.put(capture.asText(), (JsonNode) null); // Add key with null value
                        updatedTestCase.set(Constant.CAPTURE, captureMap);
                    }
                }

            }
        }

        // Add the request object to the updated test case
        updatedTestCase.set(Constant.REQUEST, requestNode);

        // Set the response data from the original test case
        updatedTestCase.set(Constant.RESPONSE, testCaseNode.get(Constant.RESPONSE));

        // Merge the updated fields back into the test case
        JsonNode finalResult = testCaseProcessor.mergeFlowNodeWithTestCaseNode(testCaseNode, updatedTestCase);
        TestCase finalTestCase = testCaseProcessor.jsonNodeToTestCase(finalResult);
        finalTestCase.getRequest().setPath(replaceParameterPlaceholders(finalTestCase));
        return finalTestCase;
    }

    public String replaceParameterPlaceholders(TestCase testCase) {
        try {
            String path = testCase.getRequest().getPath();
            // Extract placeholders for path and query parameters
            Matcher matcher = Pattern.compile(Constant.PARAM_PATTERN).matcher(path);

            // Access pathParams and queryParams from the testCase
            Map<String, String> pathParams = testCase.getRequest().getPathParam();
            Map<String, String> queryParams = testCase.getRequest().getQueryParam();

            // Replace each placeholder with its corresponding value from pathParams or queryParams
            while (matcher.find()) {
                String placeholder = matcher.group(); // e.g., "{{param userId}}"
                String key = matcher.group(1); // e.g., "userId"

                // Check if the key exists in pathParams first
                if (pathParams != null && pathParams.containsKey(key)) {
                    String value = pathParams.get(key);
                    path = path.replace(placeholder, value);
                }
                // If not found in pathParams, check in queryParams
                else if (queryParams != null && queryParams.containsKey(key)) {
                    String value = queryParams.get(key);
                    path = path.replace(placeholder, value);
                } else {
                    throw new IllegalArgumentException("Key '" + key + "' not found in pathParam or queryParam.");
                }
            }
            path = path.replace("'", "");
            return path;

        } catch (Exception e) {
            throw new RuntimeException("Error while replacing placeholders in the path", e);
        }
    }
}
