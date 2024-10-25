package com.mdscem.apitestframework.fileprocessor.validator_old;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class PlaceholderReplacer {
    @Autowired
    private PlaceholderHandler jsonPlaceholderHandler;

    @Autowired
    private PlaceholderHandler yamlPlaceholderHandler;

    public String replacePlaceholders(String content, String valuesFilePath) throws Exception {
        String fileType = detectFileType(content);
        Map<String, Object> valueMap;

        if (fileType.equals("json")) {
            valueMap = jsonPlaceholderHandler.loadValuesFromFile(valuesFilePath);
            System.out.println("Loaded JSON values from file: " + valueMap); // Debug line
            return jsonPlaceholderHandler.replacePlaceholders(content, valueMap);
        } else if (fileType.equals("yaml")) {
            valueMap = yamlPlaceholderHandler.loadValuesFromFile(valuesFilePath);
            System.out.println("Loaded YAML values from file: " + valueMap); // Debug line
            return yamlPlaceholderHandler.replacePlaceholders(content, valueMap);
        } else {
            throw new IllegalArgumentException("Unsupported file type.");
        }
    }

    private String detectFileType(String content) {
        content = content.trim();
        if (content.startsWith("{") && content.endsWith("}")) {
            return "json";
        } else {
            return "yaml";
        }
    }
}
