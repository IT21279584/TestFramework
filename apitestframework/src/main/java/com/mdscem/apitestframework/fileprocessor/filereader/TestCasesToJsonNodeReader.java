package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;


public class TestCasesToJsonNodeReader {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    public JsonNode loadFileAsJsonNode(String filePath) throws IOException {
        File file = new File(filePath);

        if (filePath.endsWith(".json")) {
            return jsonMapper.readTree(file);
        } else if (filePath.endsWith(".yaml") || filePath.endsWith(".yml")) {
            return yamlMapper.readTree(file);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filePath);
        }
    }
}
