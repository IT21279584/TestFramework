package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.StringReader;

public class YamlFileReader implements IFileReader {

    @Override
    public JsonNode readTestCases(String content) {
        try {
            // Initialize ObjectMapper with YAMLFactory
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

            // Convert the YAML content to JsonNode
            String yamlContent = content.toString();
            JsonNode jsonNode = yamlMapper.readTree(new StringReader(yamlContent));

            return jsonNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
