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

}