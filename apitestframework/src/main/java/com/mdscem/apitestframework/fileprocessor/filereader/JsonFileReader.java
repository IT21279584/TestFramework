package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

public class JsonFileReader implements IFileReader {
    private static final Logger logger = Logger.getLogger(JsonFileReader.class);

    @Override
    public String readTestCases(String content) {
        try {
            ObjectMapper jsonMapper = new ObjectMapper();
            JsonNode jsonTree = jsonMapper.readTree(content);
            String formattedJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonTree);
            logger.info("Successfully parsed JSON content.");
            return formattedJson;
        } catch (Exception e) {
            logger.error("Error parsing JSON content", e);
            e.printStackTrace();
            return null;
        }
    }
}
