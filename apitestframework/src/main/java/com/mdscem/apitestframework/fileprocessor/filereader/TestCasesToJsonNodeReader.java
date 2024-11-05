package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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


    // Existing method to load a single file as JsonNode
    public JsonNode loadFileAsJsonNode(String filePath) throws IOException {
        Path path = Paths.get(filePath);;

        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }

        String content = new String(Files.readAllBytes(path));
        System.out.println("Reading Content from: " + filePath);

        // Parse the content based on file extension
        JsonNode rootNode = parseContentByExtension(filePath, content);

        // Check if the root node contains multiple test cases
        if (rootNode.isArray()) {
            throw new IOException("Multiple test cases found in file: " + filePath);
        }

        return rootNode;
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

    public List<String> loadTestCaseFilePathsFromDirectory(String directoryPath) throws Exception {
        List<String> filePaths = new ArrayList<>();
        Path directory = Paths.get(directoryPath);

        // Check if the directory exists
        if (!Files.isDirectory(directory)) {
            throw new IOException("Directory not found: " + directoryPath);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.{json,yaml,yml}")) {
            for (Path file : stream) {
                filePaths.add(file.toAbsolutePath().toString());
            }
        }

        System.out.println("My File Paths " + filePaths);
        return filePaths;
    }

    //Load the Includes file directory files and read each file content and store into the JsonNode List
    public List<JsonNode> loadIncludeFilesAsJsonNodes(String directoryPath) throws IOException {
        List<JsonNode> jsonNodeList = new ArrayList<>();
        Path directory = Paths.get(directoryPath);

        // Check if the directory exists
        if (!Files.isDirectory(directory)) {
            throw new IOException("Directory not found: " + directoryPath);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.{json,yaml,yml}")) {
            for (Path file : stream) {

                //read each file content and add into the jsoNodeList
                JsonNode jsonNode = loadFileAsJsonNode(file.toAbsolutePath().toString());
                jsonNodeList.add(jsonNode);
            }
        }

        System.out.println("Loaded Include Files: " + jsonNodeList);
        return jsonNodeList;
    }

}
