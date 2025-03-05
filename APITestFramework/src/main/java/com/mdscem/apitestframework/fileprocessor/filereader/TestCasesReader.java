package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.constants.DirectoryPaths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class TestCasesReader {
    @Autowired
    private ObjectMapper jsonMapper;
    @Autowired
    @Qualifier("yamlMapper")
    private ObjectMapper yamlMapper;

    // Read the file
    public JsonNode readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        // Read the file content
        String content = new String(Files.readAllBytes(path));

        // Escape placeholders for YAML compatibility
        // Ensure placeholders like {{param userId}} are wrapped correctly in single quotes
        content = content.replaceAll("\\{\\{", "'{{")
                .replaceAll("\\}\\}", "}}'");

        // Determine the parser based on file extension
        if (filePath.endsWith(Constant.JSON_EXTENTION)) {
            return jsonMapper.readTree(content);
        } else if (filePath.endsWith(Constant.YAML_EXTENTION) || filePath.endsWith(Constant.YML_EXTENTION)) {
            return yamlMapper.readTree(content);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filePath);
        }
    }

    //Load include files from directory and read and return them as JsonNode list
    public List<JsonNode> loadFilesFromDirectory() throws IOException {
        List<JsonNode> jsonNodeList = new ArrayList<>();
        Path directory = Paths.get(DirectoryPaths.INCLUDES_DIRECTORY);

        // Check if the directory exists
        if (!Files.isDirectory(directory)) {
            throw new IOException("Directory not found: " + directory);
        }

        // Read each file in the directory
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                    jsonNodeList.add(readFile(file.toString()));
                }
        }
        return jsonNodeList;
    }
}