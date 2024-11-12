package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class FlowBasedTestCaseReader {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final TestCasesToJsonNodeReader testCaseReader;

    public FlowBasedTestCaseReader(TestCasesToJsonNodeReader testCaseReader) {
        this.testCaseReader = testCaseReader;
    }

    // Load flows and retrieve ordered test cases as JsonNode list
    public List<JsonNode> loadTestCasesByFlow(String flowsDirectory , String testCaseDirectory) throws IOException {
        List<JsonNode> orderedTestCases = new ArrayList<>();
        Path flowPath = Paths.get(flowsDirectory);

        // Check if the path is a directory
        if (Files.isDirectory(flowPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(flowPath, "*.yaml")) {
                // Iterate through each YAML file in the directory
                for (Path file : stream) {
                    JsonNode flowsNode = yamlMapper.readTree(file.toFile());
                    System.out.println("My flows data: " + flowsNode);

                    // Process each flow in the YAML file
                    for (JsonNode flowItem : flowsNode) {
                        String testCaseName = flowItem.get("testCase").get("name").asText();
                        String testCaseFilePath = testCaseDirectory + "/" + testCaseName + ".yaml";
                        JsonNode testCaseNode = testCaseReader.loadFileAsJsonNode(testCaseFilePath);

                        orderedTestCases.add(testCaseNode);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error processing directory " + flowPath + ": " + e.getMessage());
                throw e;
            }
        } else {
            // If the given path is neither a directory nor a regular file, handle appropriately
            throw new IOException("Invalid path: " + flowsDirectory + " is not a directory.");
        }

        return orderedTestCases;
    }

    // Loads flow data from a single file (not directory)
    public List<JsonNode> getFlowData(String flowsDirectoryPath) throws IOException {
        List<JsonNode> flowDataList = new ArrayList<>();
        Path flowPath = Paths.get(flowsDirectoryPath);

        if (Files.isDirectory(flowPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(flowPath, "*.yaml")) {
                // Iterate through each YAML file in the directory
                for (Path file : stream) {
                    JsonNode flowNode = yamlMapper.readTree(file.toFile());
                    flowDataList.add(flowNode); // Add each flow data to the list
                }
            } catch (IOException e) {
                System.err.println("Error processing directory " + flowPath + ": " + e.getMessage());
                throw e; // Propagate the exception
            }
        } else {
            // If the given path is not a directory, throw an exception
            throw new IOException("Invalid path: " + flowsDirectoryPath + " is not a directory.");
        }

        return flowDataList;
    }
}
