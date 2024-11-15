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
     * Recursively merges fields from the source JSON node (`testCaseNode`)
     * into the target JSON object node (`updatedTestCase`).
     * This method ensures that any fields present in the source but not in the
     * target are added to the target. If a field is an object in both the source
     * and target, the method performs a recursive merge to ensure nested structures
     * are handled.
     *
     * @param testCaseNode     the source JSON node containing fields to merge
     * @param TestCase  the target JSON object node to which missing fields are added
     * @return                 the updated JSON object node with fields merged
     */
    public JsonNode mergeFlowNodeWithTestCaseNode(JsonNode testCaseNode, ObjectNode TestCase) {
        testCaseNode.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode sourceField = entry.getValue();

            if (TestCase.has(fieldName)) {
                // If target already has the field, check if it's an object to merge recursively
                if (sourceField.isObject() && TestCase.get(fieldName).isObject()) {
                    mergeFlowNodeWithTestCaseNode(sourceField, (ObjectNode) TestCase.get(fieldName));
                }
            } else {
                // Otherwise, add the field from source to target
                TestCase.set(fieldName, sourceField);
            }
        });
        return TestCase;
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
