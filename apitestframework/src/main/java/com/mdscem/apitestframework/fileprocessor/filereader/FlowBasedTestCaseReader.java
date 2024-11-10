package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class FlowBasedTestCaseReader {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final TestCasesToJsonNodeReader testCaseReader;

    public FlowBasedTestCaseReader(TestCasesToJsonNodeReader testCaseReader) {
        this.testCaseReader = testCaseReader;
    }

    // Load flows and retrieve ordered test cases as JsonNode list
    public List<JsonNode> loadTestCasesByFlow(String flowsFilePath, String testCaseDirectory) throws IOException {
        List<JsonNode> orderedTestCases = new ArrayList<>();

        // Load flows.yaml as JsonNode
        Path flowPath = Paths.get(flowsFilePath);
        JsonNode flowsNode = yamlMapper.readTree(flowPath.toFile());
        System.out.println("My flows data : " + flowsNode);

        // Iterate over each flow item and retrieve test cases by name
        for (JsonNode flowItem : flowsNode) {
            String testCaseName = flowItem.get("testCaseName").asText();
            String testCaseFilePath = testCaseDirectory + "/" + testCaseName + ".yaml";

            // Load each test case file based on the name in flows.yaml
            JsonNode testCaseNode = testCaseReader.loadFileAsJsonNode(testCaseFilePath);
            orderedTestCases.add(testCaseNode);
        }

        return orderedTestCases;
    }
}
