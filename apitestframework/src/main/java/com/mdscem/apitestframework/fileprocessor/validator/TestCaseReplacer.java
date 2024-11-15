package com.mdscem.apitestframework.fileprocessor.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class TestCaseReplacer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static TestCase JsonNodeToJavaObjConverter(JsonNode jsonArrayNode) {
        // Check if JsonNode is an array
        if (jsonArrayNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonArrayNode;

            // Convert ArrayNode to TestCase array
            TestCase[] testCaseArray = objectMapper.convertValue(arrayNode, TestCase[].class);

            // Return the first TestCase object in the array
            if (testCaseArray.length > 0) {
                return testCaseArray[0];
            } else {
                throw new IllegalArgumentException("JSON array is empty.");
            }
        } else {
            throw new IllegalArgumentException("Expected a JSON array.");
        }
    }

    //remove this
    public static TestCase jsonNodeToTestCase(JsonNode jsonNode) {
        try {
            return objectMapper.treeToValue(jsonNode, TestCase.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert JsonNode to TestCase: " + e.getMessage(), e);
        }
    }



    //check the reader return and array of jsonNode or one jsonNode
    public static JsonNode replacePlaceholdersInNode(JsonNode testCaseNode, JsonNode valuesNode) throws IOException {
        // Ensure testCaseNode is an array
        ArrayNode arrayNode;

        if (testCaseNode.isArray()) {
            arrayNode = (ArrayNode) testCaseNode;
        } else {
            // Wrap testCaseNode in an array if it's not already an array
            arrayNode = objectMapper.createArrayNode().add(testCaseNode);
        }

        // Replace placeholders in each element
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode modifiedElement = replacePlaceholders(arrayNode.get(i), valuesNode);

            //Validate testcases against the schema
//            JsonNode validateNode = SchemaValidation.validateFile(modifiedElement);
            arrayNode.set(i, modifiedElement);
        }

        // Convert modified ArrayNode to TestCase array
        return arrayNode;
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

    //handle placeholder replacement
//    public static TestCase replacePlaceholdersInTestCase(JsonNode testCaseNode, JsonNode combinedValuesNode) throws IOException {
//        return replacePlaceholdersInNode(testCaseNode, combinedValuesNode);
//    }

    //handle placeholder replacement
    public static TestCase validateTestcase(JsonNode testCaseNode) throws IOException {
        //Validate testcases against the schema
        JsonNode validateNode = SchemaValidation.validateFile(testCaseNode);

        return jsonNodeToTestCase(testCaseNode);
    }

}