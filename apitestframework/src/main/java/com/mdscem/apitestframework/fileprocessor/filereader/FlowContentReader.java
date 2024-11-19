package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.fileprocessor.TestCaseProcessor;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.validator.SchemaValidation;
import com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static com.mdscem.apitestframework.fileprocessor.TestCaseProcessor.jsonNodeToTestCase;
import static com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer.*;

@Component
public class FlowContentReader {
    @Autowired
    private TestCasesReader testCasesReader;
    @Autowired
    private SchemaValidation schemaValidation;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper objectMapper = new ObjectMapper();


    // Get all flow YAML files from the directory
    public List<Path> getFlowFilesFromDirectory(Path flowPath) throws IOException {
        List<Path> flowFiles = new ArrayList<>();

        if (Files.isDirectory(flowPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(flowPath, "*.yaml")) {
                for (Path file : stream) {
                    flowFiles.add(file);
                }
            } catch (IOException e) {
                System.err.println("Error processing directory " + flowPath + ": " + e.getMessage());
                throw e;
            }
        } else {
            throw new IOException("Invalid path is not a directory.");
        }
        return flowFiles;
    }

    public List<JsonNode> getFlowContentAsJsonNodes(Path flowPath) throws IOException {
        List<JsonNode> flowContentsList = new ArrayList<>();
        JsonNode flowsNode = yamlMapper.readTree(flowPath.toFile());

        for (JsonNode singleFlow : flowsNode) {
            flowContentsList.add(singleFlow);
        }
        return flowContentsList;
    }

    public TestCase readNewTestCase(String testCaseName) throws IOException {

        // Load include files and combine them into one node
        List<JsonNode> includeNodes = testCasesReader.loadFilesFromDirectory();
        JsonNode combinedValuesNode = combineNodes(includeNodes);

        String testCaseFilePath = Constant.TEST_CASES_DIRECTORY + "/" + testCaseName + ".yaml";

        //Read the testcases
        JsonNode testCaseNode = testCasesReader.readFile(testCaseFilePath);

        // Call to method that replaces placeholders
        JsonNode replaceJsonNode = TestCaseReplacer.replacePlaceholdersInNode(testCaseNode, combinedValuesNode);

        //Validate TestCase against the testcase schema
        TestCase finalResult = schemaValidation.validateTestcase(replaceJsonNode);

        return finalResult;
    }


    /**
     * Process each TestCase with flow-specific data (pathParam, queryParam, delay, etc.).
     */
    public TestCase replaceTestCaseWithFlowData(TestCase testCase, JsonNode flowsData) {
        ObjectNode updatedTestCase = objectMapper.createObjectNode();
        JsonNode testCaseNode = convertToJsonNode(testCase);

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
