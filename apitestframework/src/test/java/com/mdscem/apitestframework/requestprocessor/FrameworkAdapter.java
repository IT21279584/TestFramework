package com.mdscem.apitestframework.requestprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

import static com.mdscem.apitestframework.constants.Constant.CORE_FRAMEWORK_PATH;
import static com.mdscem.apitestframework.constants.Constant.FRAMEWORK;

public class FrameworkAdapter {

    public static String loadFrameworkTypeFromConfig() throws IOException {
        // Load the JSON configuration file
        ObjectMapper objectMapper = new ObjectMapper();
        File configFile = new File(CORE_FRAMEWORK_PATH);

        if (!configFile.exists()) {
            throw new IOException("Configuration file not found: " + configFile.getAbsolutePath());
        }

        // Parse the JSON and extract the framework type
        JsonNode configNode = objectMapper.readTree(configFile);
        String frameworkType = configNode.get(FRAMEWORK).asText();

        return frameworkType;
    }
}
