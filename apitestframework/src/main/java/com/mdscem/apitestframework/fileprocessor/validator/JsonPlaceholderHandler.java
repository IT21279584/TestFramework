package com.mdscem.apitestframework.fileprocessor.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class JsonPlaceholderHandler implements PlaceholderHandler {

    @Override
    public Map<String, Object> loadValuesFromFile(String filePath) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = new FileInputStream(filePath)) {
            return objectMapper.readValue(inputStream, Map.class);
        }
    }

    @Override
    public String replacePlaceholders(String content, Map<String, Object> valueMap) throws Exception {
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            String placeholder = "{{include " + entry.getKey() + "}}";
            String replacement = mapToString(entry.getValue());
            System.out.println("Replacing placeholder: " + placeholder + " with: " + replacement); // Debug line
            content = content.replace(placeholder, replacement);
        }
       return content;
    }

    @Override
    public String mapToString(Object mapObject) throws Exception {
        if (mapObject instanceof Map) {
            ObjectMapper jsonMapper = new ObjectMapper();
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapObject);
        }
        return mapObject.toString();
    }
}
