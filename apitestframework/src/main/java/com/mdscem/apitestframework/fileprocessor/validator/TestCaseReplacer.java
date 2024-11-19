package com.mdscem.apitestframework.fileprocessor.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.fileprocessor.TestCaseProcessor;
import com.mdscem.apitestframework.fileprocessor.filereader.FlowContentReader;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static com.mdscem.apitestframework.fileprocessor.TestCaseProcessor.jsonNodeToTestCase;

@Component
public class TestCaseReplacer {

    @Autowired
    private TestCaseProcessor testCaseProcessor;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    //check the reader return and array of jsonNode or one jsonNode
    public static JsonNode replacePlaceholdersInNode(JsonNode testCaseNode, JsonNode valuesNode) throws IOException {
        // Check if the input node is an array or a single object
        if (testCaseNode.isArray()) {
            // If the node is an array, process each element of the array and replace placeholders
            for (int i = 0; i < testCaseNode.size(); i++) {
                JsonNode element = testCaseNode.get(i);
                JsonNode modifiedElement = replacePlaceholders(element, valuesNode);
                ((ObjectNode) testCaseNode).set(String.valueOf(i), modifiedElement);  // Directly modify the array node
            }
        } else {
            // If it's a single object, replace the placeholders directly
            testCaseNode = replacePlaceholders(testCaseNode, valuesNode);
        }

        // Return the processed JsonNode
        return testCaseNode;
    }

    //replace place holders
    public static JsonNode replacePlaceholders(JsonNode testCaseNode, JsonNode valuesNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = testCaseNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode valueNode = field.getValue();

            if (valueNode.isTextual() && valueNode.asText().startsWith("{{include ") && valueNode.asText().endsWith("}}")) {
                String placeholderKey = valueNode.asText().substring(10, valueNode.asText().length() - 2).trim();

                if (valuesNode.has(placeholderKey)) {
                    JsonNode replacementNode = valuesNode.get(placeholderKey);

                    if (replacementNode.isValueNode()) {
                        ((ObjectNode) testCaseNode).put(field.getKey(), replacementNode.asText());
                    } else {
                        ((ObjectNode) testCaseNode).set(field.getKey(), replacementNode);
                    }
                }
            } else if (valueNode.isObject()) {
                replacePlaceholders(valueNode, valuesNode);
            } else if (valueNode.isArray()) {
                for (JsonNode arrayElement : valueNode) {
                    replacePlaceholders(arrayElement, valuesNode);
                }
            }
        }
        return testCaseNode;
    }

    /**
     * Process each TestCase with flow-specific data (pathParam, queryParam, delay, etc.).
     */
    public TestCase replaceTestCaseWithFlowData(TestCase testCase, JsonNode flowsData) {
        ObjectNode updatedTestCase = objectMapper.createObjectNode();
        JsonNode testCaseNode = testCaseProcessor.convertToJsonNode(testCase);

        // Create the request object node to include pathParam and queryParam
        ObjectNode requestNode = objectMapper.createObjectNode();

        for (JsonNode flowSection : flowsData) {
            if (flowSection.has("name")) {
                String flowName = flowSection.get("name").asText();
                if (testCase.getTestCaseName().equals(flowName)) {

                    // Check for pathParam and queryParam before setting
                    if (flowSection.has("pathParam")) {
                        requestNode.set("pathParam", flowSection.get("pathParam"));
                    }
                    if (flowSection.has("queryParam")) {
                        requestNode.set("queryParam", flowSection.get("queryParam"));
                    }

                    updatedTestCase.set("delay", flowSection.has("delay") ? flowSection.get("delay") : objectMapper.nullNode());
                    break;
                }
            }

        }

        // Add the request object to updatedTestCase
        updatedTestCase.set("request", requestNode);

        // Set the response as usual
        updatedTestCase.set("response", testCaseNode.get("response"));

        JsonNode finalResult = testCaseProcessor.mergeMissingFields(testCaseNode, updatedTestCase);
        return jsonNodeToTestCase(finalResult);
    }
}