package com.mdscem.apitestframework.requestprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CaptureReplacer {

    @Autowired
    private CaptureContext captureContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    //update captureMap using the response data in testcase after the testcase execution
    public void updateCapturesFromResponse(String response) {
        try {
            // Parse the response string as JSON
            JsonNode responseJson = objectMapper.readTree(response);

            // Iterate over all test case captures
            captureContext.getCaptureMap().forEach((testCaseName, captures) -> {
                captures.forEach((key, value) -> {
                    if (responseJson.has(key)) {
                        // Extract the value from the JSON response
                        String newValue = responseJson.get(key).asText();
                        captures.put(key, newValue);
                        System.out.println("Updated capture for key: " + key + " with value: " + newValue);
                    } else {
                        System.err.println("Key '" + key + "' not found in the response for test case: " + testCaseName);
                    }
                });
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response or update captures", e);
        }
    }

    public String replaceParameterPlaceholders(String path) {
        try {
            // Extract placeholders in the format {{param TestCaseName.key}}
            Pattern pattern = Pattern.compile("\\{\\{use (\\w+)\\.(\\w+)}}");
            Matcher matcher = pattern.matcher(path);

            // Replace each placeholder with its corresponding value from captureMap
            while (matcher.find()) {
                String placeholder = matcher.group(); // e.g., "{{use GetStudent.userId}}"
                String testCaseName = matcher.group(1); // e.g., "GetStudent"
                String key = matcher.group(2); // e.g., "userId"

                // Check if the testCaseName and key exist in the captureMap
                if (captureContext.getCaptureMap().containsKey(testCaseName)) {
                    Map<String, Object> innerMap = captureContext.getCaptureMap().get(testCaseName);
                    if (innerMap.containsKey(key)) {
                        Object value = innerMap.get(key);
                        path = path.replace(placeholder, value.toString());
                    } else {
                        throw new IllegalArgumentException("Key '" + key + "' not found in testCase '" + testCaseName + "'.");
                    }
                } else {
                    throw new IllegalArgumentException("TestCase '" + testCaseName + "' not found in CaptureMap.");
                }
            }

            return path;

        } catch (Exception e) {
            throw new RuntimeException("Error while replacing placeholders in the path", e);
        }
    }

}
