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
    @Autowired
    private TestCaseProcessor testCaseProcessor;
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
            throw new IOException("Invalid path. not a directory.");
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
        JsonNode combinedValuesNode = testCaseProcessor.combineNodes(includeNodes);

        String testCaseFilePath = Constant.TEST_CASES_DIRECTORY + "/" + testCaseName + ".yaml";

        //Read the testcases
        JsonNode testCaseNode = testCasesReader.readFile(testCaseFilePath);

        // Call to method that replaces placeholders
        JsonNode replaceJsonNode = TestCaseReplacer.replacePlaceholdersInNode(testCaseNode, combinedValuesNode);

        //Validate TestCase against the testcase schema
        TestCase finalResult = schemaValidation.validateTestcase(replaceJsonNode);

        return finalResult;
    }

}
