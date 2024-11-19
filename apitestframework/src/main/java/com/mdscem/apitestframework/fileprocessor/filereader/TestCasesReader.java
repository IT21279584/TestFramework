package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mdscem.apitestframework.constants.Constant;
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
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());


    // Read the file
    public JsonNode readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        String content = new String(Files.readAllBytes(path));

        content = content.replaceAll("\\{\\{(.*?)\\}\\}", "\"{{$1}}\"");

        // Determine parser based on file extension and parse the content
        if (filePath.endsWith(".json")) {
            return jsonMapper.readTree(content);
        } else if (filePath.endsWith(".yaml") || filePath.endsWith(".yml")) {
            return yamlMapper.readTree(content);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filePath);
        }
    }

    //Load include files from directory and read and return them as JsonNode list
    public List<JsonNode> loadFilesFromDirectory() throws IOException {
        List<JsonNode> jsonNodeList = new ArrayList<>();
        Path directory = Paths.get(Constant.INCLUDES_DIRECTORY);

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
