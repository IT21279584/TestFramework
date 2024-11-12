package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.fileprocessor.TestCaseProcessor;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static com.mdscem.apitestframework.fileprocessor.TestCaseProcessor.processTestCaseWithFlowData;

@Component
public class FlowBasedTestCaseReader {

    @Autowired
    private  TestCaseReplacer testCaseReplacer;
    @Autowired
    private  TestCasesToJsonNodeReader testCasesToJsonNodeReader;
    @Autowired
    private  TestCaseProcessor testCaseProcessor;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    // Main method to load test cases based on flows
    public List<JsonNode> loadTestCasesByFlow(JsonNode combinedValuesNode) throws IOException {

        List<JsonNode> testCases = new ArrayList<>();
        List<Path> flowFiles = getFlowFilesFromDirectory(Constant.FLOWS_DIRECTORY);

        for (Path flowFile : flowFiles) {
            List<JsonNode> flowTestCases = processFlowFileAndRead(flowFile, combinedValuesNode);
            testCases.addAll(flowTestCases);
        }

        return testCases;
    }

    // Get all flow YAML files from the directory
    private List<Path> getFlowFilesFromDirectory(String flowsDirectory) throws IOException {
        Path flowPath = Paths.get(flowsDirectory);
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
            throw new IOException("Invalid path: " + flowsDirectory + " is not a directory.");
        }

        return flowFiles;
    }

    // Process individual flow file and return the list of test cases
    private List<JsonNode> processFlowFileAndRead(Path flowFile, JsonNode combinedValuesNode) throws IOException {
        List<JsonNode> processedTestCases = new ArrayList<>();
        JsonNode flowsNode = yamlMapper.readTree(flowFile.toFile());

        // Process each flow item in the YAML file
        for (JsonNode flowItem : flowsNode) {
            String testCaseName = flowItem.get("testCase").get("name").asText();
            String testCaseFilePath = Constant.TEST_CASES_DIRECTORY + "/" + testCaseName + ".yaml";

            //Read the testcases
            JsonNode testCaseNode = testCasesToJsonNodeReader.readFile(testCaseFilePath);

            // Call to method that replaces placeholders
            TestCase finalResults = TestCaseReplacer.replacePlaceholdersInTestCase(testCaseNode, combinedValuesNode);

            // Process the final test case with flow data
            JsonNode processedTestCase = processTestCaseWithFlowData(finalResults, testCaseNode, combinedValuesNode, flowItem);

            processedTestCases.add(processedTestCase);
            testCaseProcessor.saveTestCases(processedTestCase);
        }

        return processedTestCases;
    }
}
