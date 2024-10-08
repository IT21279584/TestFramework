package com.mdscem.apitestframework.fileprocessor.yamlprocessor;

import org.apache.commons.text.StrSubstitutor;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlPlaceholderReplacer {

    public String replaceYamlPlaceholders(String yamlFilePath, String valuesYamlFilePath) throws Exception {
        Map<String, Object> valueMap = null;

        // Load values from the values YAML file
        try (InputStream input = new FileInputStream(valuesYamlFilePath)) {
            Yaml yaml = new Yaml();
            valueMap = yaml.load(input);  // Load YAML values into a map
        } catch (IOException exception) {
            exception.printStackTrace();
            throw new IOException("Error reading values file: " + valuesYamlFilePath, exception);
        }

        String modifiedYamlContent;

        try {
            // Read the original YAML file content
            String yamlContent = new String(Files.readAllBytes(Paths.get(yamlFilePath)));

            // Replace placeholders using StrSubstitutor
            StrSubstitutor strSubstitutor = new StrSubstitutor(valueMap, "{{include ", "}}");
            modifiedYamlContent = strSubstitutor.replace(yamlContent);

            // Check for any unresolved placeholders
            Pattern placeholderPattern = Pattern.compile("\\{\\{([^}]+)}}");
            Matcher matcher = placeholderPattern.matcher(modifiedYamlContent);
            StringBuilder unresolvedPlaceholders = new StringBuilder();

            while (matcher.find()) {
                unresolvedPlaceholders.append(matcher.group()).append("\n");
            }

            if (unresolvedPlaceholders.length() > 0) {
                throw new IllegalArgumentException("Error: The following placeholders were not replaced:\n" + unresolvedPlaceholders);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error processing the YAML file: " + yamlFilePath, e);
        }

        System.out.println(modifiedYamlContent);
        return modifiedYamlContent;
    }
}
