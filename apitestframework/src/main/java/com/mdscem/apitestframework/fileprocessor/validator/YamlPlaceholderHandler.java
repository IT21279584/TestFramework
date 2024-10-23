package com.mdscem.apitestframework.fileprocessor.validator;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.util.Map;

public class YamlPlaceholderHandler implements PlaceholderHandler {

    @Override
    public Map<String, Object> loadValuesFromFile(String filePath) throws Exception {
        Yaml yaml = new Yaml(new Constructor(Map.class));
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            return yaml.load(inputStream);
        }
    }

    @Override
    public String replacePlaceholders(String content, Map<String, Object> valueMap) throws Exception {
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            String placeholder = "{{include " + entry.getKey() + "}}";
            String replacement = mapToString(entry.getValue());
            System.out.println("Replacing placeholder yaml: " + placeholder + " with: " + replacement); // Debug line
            content = content.replace(placeholder, replacement);
        }
        return content;
    }

    @Override
    public String mapToString(Object mapObject) throws Exception {
        if (mapObject instanceof Map) {
            Yaml yaml = new Yaml(getYamlDumperOptions());
            StringBuilder yamlOutput = new StringBuilder();
            Map<String, Object> map = (Map<String, Object>) mapObject;

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object valueObj = entry.getValue();
                String value;

                if (valueObj instanceof String) {
                    value = "\"" + valueObj.toString() + "\"";
                } else if (valueObj instanceof Map) {
                    value = mapToString(valueObj);
                } else {
                    value = valueObj.toString();
                }

                yamlOutput.append(key).append(": ").append(value).append("\n");
            }

            return yamlOutput.toString();
        }
        return mapObject.toString();
    }

    private DumperOptions getYamlDumperOptions() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        return options;
    }
}
