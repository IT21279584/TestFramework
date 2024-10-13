package com.mdscem.apitestframework.fileprocessor.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderReplacer {

    public String replacePlaceholders(String content, String valuesFilePath) throws Exception {

        // Detect whether the content to replace is JSON or YAML
        String fileType = detectFileType(content);

        Map<String, Object> valueMap;

        if (fileType.equals("json")) {
            valueMap = loadJsonValuesFromFile(valuesFilePath);
        } else if (fileType.equals("yaml")) {
            valueMap = loadYamlValuesFromFile(valuesFilePath);
        } else {
            throw new IllegalArgumentException("Unsupported file type.");
        }

        return replacePlaceholdersInString(content, valueMap, fileType);
    }

    // Method to detect content is in JSON or YAML format
    private String detectFileType(String content) {
        content = content.trim();
        if (content.startsWith("{") && content.endsWith("}")) {
            return "json";
        } else {
            return "yaml";
        }
    }

    // Method to load values from a YAML file
    private Map<String, Object> loadYamlValuesFromFile(String yamlFilePath) throws IOException {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = new FileInputStream(yamlFilePath)) {
            return yaml.load(inputStream);  // Load YAML content from the file into a map
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error reading YAML file: " + yamlFilePath, e);
        }
    }

    // Method to load values from a JSON file
    private Map<String, Object> loadJsonValuesFromFile(String jsonFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream inputStream = new FileInputStream(jsonFilePath)) {
            return objectMapper.readValue(inputStream, Map.class);  // Load JSON content from the file into a map
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error reading JSON file: " + jsonFilePath, e);
        }
    }

    // Main placeholder replacement method (works for both YAML and JSON strings)
    private String replacePlaceholdersInString(String content, Map<String, Object> valueMap, String fileType) throws Exception {
        String modifiedContent = content;

        try {
            // Iterate over the map and replace placeholders
            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                String placeholder = "{{include " + entry.getKey() + "}}";
                String replacement;

                if (entry.getValue() instanceof Map) {
                    // If the value is a nested map (e.g., auth_info_1), convert it to a string representation
                    ObjectMapper mapper = new ObjectMapper();
                    replacement = mapper.writeValueAsString(entry.getValue());  // Convert nested object to JSON string
                } else {
                    // Otherwise, treat it as a simple value
                    replacement = entry.getValue().toString();
                }

                // Replace the placeholder with its corresponding value
                modifiedContent = modifiedContent.replace(placeholder, replacement);
            }

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

        System.out.println("Modified content ========> " + modifiedContent);
        return modifiedContent;
    }
}
