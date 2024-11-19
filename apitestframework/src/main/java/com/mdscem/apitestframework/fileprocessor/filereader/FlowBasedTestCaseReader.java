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

import static com.mdscem.apitestframework.fileprocessor.TestCaseProcessor.convertToJsonNode;
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
    public List<TestCase> loadTestCasesByFlow() throws IOException {

        List<TestCase> testCases = new ArrayList<>();
        List<Path> flowFiles = getFlowFilesFromDirectory();

        for (Path flowFile : flowFiles) {
            List<TestCase> flowTestCases = processFlowFileAndRead(flowFile);
            testCases.addAll(flowTestCases);
        }

        return testCases;
    }

    // Get all flow YAML files from the directory
    private List<Path> getFlowFilesFromDirectory() throws IOException {
        Path flowPath = Paths.get(Constant.FLOWS_DIRECTORY);
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

    // Process individual flow file and return the list of test cases
    private List<TestCase> processFlowFileAndRead(Path flowFile) throws IOException {
        List<TestCase> processedTestCases = new ArrayList<>();
        JsonNode flowsNode = yamlMapper.readTree(flowFile.toFile());

        // Load include files and combine them into one node
        List<JsonNode> includeNodes = testCasesToJsonNodeReader.loadFilesFromDirectory();
        JsonNode combinedValuesNode = TestCaseProcessor.combineNodes(includeNodes);

        // Process each flow item in the YAML file
        for (JsonNode flowItem : flowsNode) {
            String testCaseName = flowItem.get("testCase").get("name").asText();
            String testCaseFilePath = Constant.TEST_CASES_DIRECTORY + "/" + testCaseName + ".yaml";

            //Read the testcases
            JsonNode testCaseNode = testCasesToJsonNodeReader.readFile(testCaseFilePath);

            // Call to method that replaces placeholders
            TestCase finalResults = TestCaseReplacer.replacePlaceholdersInTestCase(testCaseNode, combinedValuesNode);

            // Process the final test case with flow data
            TestCase processedTestCase = processTestCaseWithFlowData(finalResults, flowItem);
            processedTestCases.add(processedTestCase);
            testCaseProcessor.saveTestCases(processedTestCase);
        }

        return processedTestCases;
    }
}
