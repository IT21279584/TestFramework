package com.mdscem.apitestframework.fileprocessor.filereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.log4j.Logger;

public class YamlFileReader implements IFileReader {
    private static final Logger logger = Logger.getLogger(YamlFileReader.class);

    @Override
    public String readTestCases(String content) {
        try {
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
//            JsonNode yamlTree = yamlMapper.readTree(content);
            String formattedYaml = yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(content);
            logger.info("Successfully parsed YAML content.");
            return formattedYaml;
        } catch (Exception e) {
            logger.error("Error parsing YAML content", e);
            e.printStackTrace();
            return null;
        }
    }
}