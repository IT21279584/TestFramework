package com.mdscem.apitestframework.fileprocessor.flowprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

public class CaptureReplacer {

    @Autowired
    private static CaptureContext captureContext;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    //update captureMap using the response data in testcase after the testcase execution
    public static void updateCapturesFromResponse(String response) {
        try {
            // Parse the response string as JSON
            JsonNode responseJson = objectMapper.readTree(response);

            // Update captureMap with actual values
            captureContext.getCaptureMap().forEach((key, value) -> {
                if (responseJson.has(key)) {
                    // Extract the value from the JSON response
                    captureContext.getCaptureMap().put(key, responseJson.get(key).asText());
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to parse response string or update captures: " + e.getMessage());
        }
    }
}
