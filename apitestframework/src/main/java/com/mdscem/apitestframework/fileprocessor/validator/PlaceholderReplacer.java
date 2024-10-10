package com.mdscem.apitestframework.fileprocessor.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StrSubstitutor;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderReplacer {

    // Method to replace placeholders in a YAML string
    public String replacePlaceholdersInYamlString(String yamlContent, String valuesYamlContent) throws Exception {
        Map<String, Object> valueMap = loadYamlValuesFromString(valuesYamlContent);
        return replacePlaceholdersInString(yamlContent, valueMap, "yaml");
    }

    // Method to replace placeholders in a JSON string
    public String replacePlaceholdersInJsonString(String jsonContent, String valuesJsonContent) throws Exception {
        Map<String, Object> valueMap = loadJsonValuesFromString(valuesJsonContent);
        return replacePlaceholdersInString(jsonContent, valueMap, "json");
    }

    // Helper method to load values from a YAML string
    private Map<String, Object> loadYamlValuesFromString(String valuesYamlContent) {
        Yaml yaml = new Yaml();
        return yaml.load(valuesYamlContent);
    }

    // Helper method to load values from a JSON string
    private Map<String, Object> loadJsonValuesFromString(String valuesJsonContent) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(valuesJsonContent, Map.class);
    }

    // Main placeholder replacement method (works for both YAML and JSON strings)
    private String replacePlaceholdersInString(String content, Map<String, Object> valueMap, String fileType) throws Exception {
        String modifiedContent;

        try {
            // Replace placeholders using StrSubstitutor
            StrSubstitutor strSubstitutor = new StrSubstitutor(valueMap, "{{include ", "}}");
            modifiedContent = strSubstitutor.replace(content);

            // Check for any unresolved placeholders
            Pattern placeholderPattern = Pattern.compile("\\{\\{([^}]+)}}");
            Matcher matcher = placeholderPattern.matcher(modifiedContent);
            StringBuilder unresolvedPlaceholders = new StringBuilder();

            while (matcher.find()) {
                unresolvedPlaceholders.append(matcher.group()).append("\n");
            }

            if (unresolvedPlaceholders.length() > 0) {
                throw new IllegalArgumentException("Error: The following placeholders were not replaced:\n" + unresolvedPlaceholders);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error processing the " + fileType + " content", e);
        }

        System.out.println(modifiedContent);
        return modifiedContent;
    }
}
