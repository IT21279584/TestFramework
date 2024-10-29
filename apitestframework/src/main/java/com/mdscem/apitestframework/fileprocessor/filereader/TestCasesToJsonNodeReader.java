package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestCasesToJsonNodeReader {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Existing method to load a single file as JsonNode
    public JsonNode loadFileAsJsonNode(String filePath) throws IOException {
        Path path = Paths.get(filePath);;

        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }

        String content = new String(Files.readAllBytes(path)); // For Java 8 compatibility
        System.out.println("Reading Content from: " + filePath);

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
    public List<String> loadTestCaseFilePaths(String configFilePath) throws IOException {
        // Read the file content as a JSON array
        String content = new String(Files.readAllBytes(Paths.get(configFilePath)));
        // Convert the content into a List of Strings
        return objectMapper.readValue(content, List.class);
    }

}
