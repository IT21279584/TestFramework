package com.mdscem.apitestframework.requestprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.constants.DirectoryPaths;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class FrameworkAdapter {

    public String loadFrameworkTypeFromConfig() throws IOException {
        // Load the JSON configuration file
        ObjectMapper objectMapper = new ObjectMapper();
        File configFile = new File(DirectoryPaths.CORE_FRAMEWORK_PATH);

        if (!configFile.exists()) {
            throw new IOException("Configuration file not found: " + configFile.getAbsolutePath());
        }
        // Parse the JSON and extract the framework type
        JsonNode configNode = objectMapper.readTree(configFile);
        String frameworkType = configNode.get(Constant.FRAMEWORK).asText();
        return frameworkType;
    }
}
