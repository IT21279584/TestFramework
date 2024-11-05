package com.mdscem.apitestframework.fileprocessor.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestCaseReplacer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TestCase[] JsonNodeToJavaObjConverter(JsonNode jsonArrayNode){
        // Check if JsonNode is an array
        if (jsonArrayNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonArrayNode;

            // Convert ArrayNode to TestCase array
            List<TestCase> testCaseList = Arrays.asList(objectMapper.convertValue(arrayNode, TestCase[].class));

            // Convert List to Array
            return testCaseList.toArray(new TestCase[0]);
        } else {
            throw new IllegalArgumentException("Expected a JSON array.");
        }
    }

    //check the reader return and array of jsonNode or one jsonNode
    public TestCase[] replacePlaceholdersInNode(JsonNode testCaseNode, JsonNode valuesNode) throws IOException {
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
            JsonNode validateNode = SchemaValidation.validateFile(modifiedElement);
            arrayNode.set(i, validateNode);
        }

        // Convert modified ArrayNode to TestCase array
        return JsonNodeToJavaObjConverter(arrayNode);
    }



    //replace place holders
    public JsonNode replacePlaceholders(JsonNode testCaseNode, JsonNode valuesNode) {
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
}