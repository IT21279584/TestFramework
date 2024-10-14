package com.mdscem.apitestframework.fileprocessor.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderReplacer {

    public String replacePlaceholders(String content, String valuesFilePath) throws Exception {
        // Detect the file type from content
        String fileType = detectFileType(content);

        Map<String, Object> valueMap;

        // Load values based on the detected file type (YAML/JSON)
        if (fileType.equals("yaml")) {
            valueMap = loadYamlValuesFromFile(valuesFilePath);
        } else if (fileType.equals("json")) {
            valueMap = loadJsonValuesFromFile(valuesFilePath);
        } else {
            throw new IllegalArgumentException("Unsupported file type.");
        }

        // Replace placeholders in the content
        return replacePlaceholdersInString(content, valueMap, fileType);
    }

    // Method to detect whether content is in JSON or YAML format
    private String detectFileType(String content) {
        content = content.trim();
        if (content.startsWith("{") && content.endsWith("}")) {
            return "json";
        } else if (content.startsWith("---") || content.contains(":")) {
            return "yaml";
        } else {
            throw new IllegalArgumentException("Unsupported content format.");
        }
    }

    // Method to load values from a YAML file
    private Map<String, Object> loadYamlValuesFromFile(String yamlFilePath) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(yamlFilePath)) {
            return yaml.load(inputStream);  // Load YAML content into a map
        } catch (IOException e) {
            throw new IOException("Error reading YAML file: " + yamlFilePath, e);
        }
    }

    // Method to load values from a JSON file
    private Map<String, Object> loadJsonValuesFromFile(String jsonFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = new FileInputStream(jsonFilePath)) {
            return objectMapper.readValue(inputStream, Map.class);  // Load JSON content into a map
        } catch (IOException e) {
            throw new IOException("Error reading JSON file: " + jsonFilePath, e);
        }
    }

    // Placeholder replacement method for both YAML and JSON
    private String replacePlaceholdersInString(String content, Map<String, Object> valueMap, String fileType) throws Exception {
        String modifiedContent = content;

        try {
            // Iterate over the map and replace placeholders
            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                String placeholder = "{{include " + entry.getKey() + "}}";
                Object replacement = entry.getValue();

                // Check if it's a Map (for auth)
                if (replacement instanceof Map) {
                    // Directly serialize the Map to JSON without wrapping it in a string
                    replacement = new ObjectMapper().writeValueAsString(replacement);
                }

                // Avoid wrapping the baseUri in additional quotes
                modifiedContent = modifiedContent.replace(placeholder, replacement.toString());
            }

            // Check for unresolved placeholders
            checkForUnresolvedPlaceholders(modifiedContent);
        } catch (Exception e) {
            throw new Exception("Error processing " + fileType + " content", e);
        }

        return modifiedContent;
    }



    // Convert nested maps to formatted strings based on file type (YAML or JSON)
    private String convertMapToFormattedString(Object value, String fileType) throws IOException {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;

            ObjectMapper objectMapper = new ObjectMapper();
            // Use ObjectMapper to convert the map back to a JSON string
            return objectMapper.writeValueAsString(map);
        }
        return value.toString();  // Return the string representation for non-map values
    }

    // Method to check for unresolved placeholders
    private void checkForUnresolvedPlaceholders(String modifiedContent) {
        Pattern placeholderPattern = Pattern.compile("\\{\\{([^}]+)}}");
        Matcher matcher = placeholderPattern.matcher(modifiedContent);
        StringBuilder unresolvedPlaceholders = new StringBuilder();

        while (matcher.find()) {
            unresolvedPlaceholders.append(matcher.group()).append("\n");
        }

        if (unresolvedPlaceholders.length() > 0) {
            throw new IllegalArgumentException("Error: The following placeholders were not replaced:\n" + unresolvedPlaceholders);
        }
    }
}
