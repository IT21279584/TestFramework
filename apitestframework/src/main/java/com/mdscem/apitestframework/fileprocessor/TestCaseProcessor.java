package com.mdscem.apitestframework.fileprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestCaseProcessor {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    //JsonNode to TestCase object
    public static TestCase jsonNodeToTestCase(JsonNode jsonNode) {
        try {
            return objectMapper.treeToValue(jsonNode, TestCase.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert JsonNode to TestCase: " + e.getMessage(), e);
        }
    }

    /**
     * Merges missing fields from `testCaseNode` into `updatedTestCase` recursively in flow process.
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

    /**
     * Combine multiple nodes into a single node(include nodes).
     */
    public JsonNode combineNodes(List<JsonNode> node) {
        ObjectNode combinedValuesNode = objectMapper.createObjectNode();
        node.forEach(includeNode ->
                includeNode.fields().forEachRemaining(entry ->
                        combinedValuesNode.set(entry.getKey(), entry.getValue())
                )
        );
        return combinedValuesNode;
    }

    //Convert Testcase to the JsoNode
    public JsonNode convertToJsonNode(TestCase testCase) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.valueToTree(testCase);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to convert object to JsonNode", e);
        }
    }
}
