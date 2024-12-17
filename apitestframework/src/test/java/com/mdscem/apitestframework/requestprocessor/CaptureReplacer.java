package com.mdscem.apitestframework.requestprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CaptureReplacer {
    private static final Logger logger = LogManager.getLogger(CaptureReplacer.class);

    private final CaptureContext captureContext = CaptureContext.getInstance(); // Use Singleton
    private final ObjectMapper objectMapper = new ObjectMapper();

    //update captureMap using the response data in testcase after the testcase execution
    public void updateCapturesFromResponse(String response) {
        try {
            // Parse the response string as JSON
            JsonNode responseJson = objectMapper.readTree(response);

            // Iterate over all test case captures
            captureContext.getCaptureMap().forEach((testCaseName, captures) -> captures.forEach((key, value) -> {
                if (responseJson.has(key)) {
                    // Extract the value from the JSON response
                    String newValue = responseJson.get(key).asText();
                    captures.put(key, newValue);
                    logger.info("Updated capture for key: " + key + " with value: " + newValue);
                }
            }));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response or update captures", e);
        }
    }

    public TestCase replaceParameterPlaceholders(TestCase testCase) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String testCaseJson = objectMapper.writeValueAsString(testCase);

            // Regex pattern to find placeholders
            Pattern pattern = Pattern.compile("\\{\\{use (\\w+)\\.(\\w+)}}");
            Matcher matcher = pattern.matcher(testCaseJson);

            // Replace placeholders
            while (matcher.find()) {
                String placeholder = matcher.group(); // e.g., "{{use GetEmployee.name}}"
                String testCaseName = matcher.group(1); // e.g., "GetEmployee"
                String key = matcher.group(2); // e.g., "name"

                // Fetch value from capture context
                if (captureContext.getCaptureMap().containsKey(testCaseName)) {
                    Map<String, Object> innerMap = captureContext.getCaptureMap().get(testCaseName);
                    if (innerMap.containsKey(key)) {
                        Object value = innerMap.get(key);
                        testCaseJson = testCaseJson.replace(placeholder, value.toString());
                    }else {
                        throw new IllegalArgumentException("Key '" + key + "' not found in the response for test case: " + testCaseName);
                    }
                } else {
                    throw new IllegalArgumentException("TestCase '" + testCaseName + "' not found in CaptureMap.");
                }
            }

            // Deserialize updated JSON back into TestCase
            return objectMapper.readValue(testCaseJson, TestCase.class);

        } catch (Exception e) {
            throw new RuntimeException("Error while replacing placeholders in TestCase", e);
        }
    }



}
