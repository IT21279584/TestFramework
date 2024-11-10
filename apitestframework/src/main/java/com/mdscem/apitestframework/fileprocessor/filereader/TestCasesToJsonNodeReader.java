package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestCasesToJsonNodeReader {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    // Store the files separately for includes and flows
    private List<JsonNode> includeJsonNodeList = new ArrayList<>();
    private List<JsonNode> flowJsonNodeList = new ArrayList<>();

    // Existing method to load a single file as JsonNode
    public JsonNode loadFileAsJsonNode(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }

        String content = new String(Files.readAllBytes(path));
//        System.out.println("Reading Content from: " + filePath);

        // Parse the content based on file extension
        return parseContentByExtension(filePath, content);
    }

    private JsonNode parseContentByExtension(String filePath, String content) throws IOException {
        if (filePath.endsWith(".json")) {
            return jsonMapper.readTree(content);
        } else if (filePath.endsWith(".yaml") || filePath.endsWith(".yml")) {
            return yamlMapper.readTree(content);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filePath);
        }
    }

    // Load test case files from the test cases directory
    public List<String> loadTestCaseFilePathsFromDirectory(String directoryPath) throws Exception {
        List<String> filePaths = new ArrayList<>();
        Path directory = Paths.get(directoryPath);

        if (!Files.isDirectory(directory)) {
            throw new IOException("Directory not found: " + directoryPath);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.{json,yaml,yml}")) {
            for (Path file : stream) {
                filePaths.add(file.toAbsolutePath().toString());
            }
        }

        System.out.println("My File Paths: " + filePaths);
        return filePaths;
    }

    // Generalized method to load files from any directory and return them as JsonNode list
    private List<JsonNode> loadFilesFromDirectoryAsJsonNodes(String directoryPath) throws IOException {
        List<JsonNode> jsonNodeList = new ArrayList<>();
        Path directory = Paths.get(directoryPath);

        // Check if the directory exists
        if (!Files.isDirectory(directory)) {
            throw new IOException("Directory not found: " + directoryPath);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.{json,yaml,yml}")) {
            for (Path file : stream) {
                JsonNode jsonNode = loadFileAsJsonNode(file.toAbsolutePath().toString());
                jsonNodeList.add(jsonNode);
            }
        }
        return jsonNodeList;
    }

    // Method to load include files
    public List<JsonNode> loadIncludeFilesAsJsonNodes(String directoryPath) throws IOException {
        includeJsonNodeList = loadFilesFromDirectoryAsJsonNodes(directoryPath);
        System.out.println("Loaded Include Files: " + includeJsonNodeList + "\n");

        return includeJsonNodeList;
    }
}
