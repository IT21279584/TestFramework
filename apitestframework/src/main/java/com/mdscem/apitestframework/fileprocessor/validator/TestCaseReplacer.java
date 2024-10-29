package com.mdscem.apitestframework.fileprocessor.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

public class TestCaseReplacer {

    public JsonNode replacePlaceholdersInNode(JsonNode testCaseNode, JsonNode valuesNode) {
        if (testCaseNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) testCaseNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode modifiedElement = replacePlaceholders(arrayNode.get(i), valuesNode);
                arrayNode.set(i, modifiedElement);
            }
            return arrayNode;
        } else {
            return replacePlaceholders(testCaseNode, valuesNode);
        }
    }

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
