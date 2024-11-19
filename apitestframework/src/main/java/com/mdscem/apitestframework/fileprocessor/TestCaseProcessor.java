package com.mdscem.apitestframework.fileprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdscem.apitestframework.context.TestCaseRepository;
import com.mdscem.apitestframework.fileprocessor.filereader.FlowBasedTestCaseReader;
import com.mdscem.apitestframework.fileprocessor.filereader.TestCasesToJsonNodeReader;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer.JsonNodeToJavaObjConverter;
import static com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer.jsonNodeToTestCase;

@Component
public class TestCaseProcessor {

    @Autowired
    private TestCaseRepository testCaseRepository;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Process each TestCase with flow-specific data (pathParam, queryParam, delay, etc.).
     */
    public static TestCase processTestCaseWithFlowData(TestCase testCase, JsonNode flowsData) {
        ObjectNode updatedTestCase = objectMapper.createObjectNode();
        JsonNode testCaseNode = convertToJsonNode(testCase);
        System.out.println("My TestCase JsoNode : ");

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

        JsonNode finalResult = mergeMissingFields(testCaseNode, updatedTestCase);
        return jsonNodeToTestCase(finalResult);
    }

    /**
     * Save the processed test case into the repository.
     */
    public void saveTestCases(TestCase processedTestCase) {
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

    /**
     * Combine multiple nodes into a single node.
     */
    public static JsonNode combineNodes(List<JsonNode> node) {
        ObjectNode combinedValuesNode = objectMapper.createObjectNode();
        node.forEach(includeNode ->
                includeNode.fields().forEachRemaining(entry ->
                        combinedValuesNode.set(entry.getKey(), entry.getValue())
                )
        );
        return combinedValuesNode;
    }


    public static JsonNode convertToJsonNode(TestCase testCase) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.valueToTree(testCase);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to convert object to JsonNode", e);
        }
    }
}
