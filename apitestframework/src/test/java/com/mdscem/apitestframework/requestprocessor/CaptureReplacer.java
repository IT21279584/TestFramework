package com.mdscem.apitestframework.requestprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.fileprocessor.filereader.model.Request;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CaptureReplacer {

    private final CaptureContext captureContext = CaptureContext.getInstance(); // Use Singleton
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

    public TestCase replaceParameterPlaceholders(TestCase testCase) {
        try {
            // Get the request from the test case
            Request request = testCase.getRequest();
            ObjectMapper objectMapper = new ObjectMapper();

            // Serialize the request object into a JSON string for processing
            String requestJson = objectMapper.writeValueAsString(request);

            // Replace placeholders in the serialized JSON string
            Pattern pattern = Pattern.compile("\\{\\{use (\\w+)\\.(\\w+)}}");
            Matcher matcher = pattern.matcher(requestJson);

            while (matcher.find()) {
                String placeholder = matcher.group(); // e.g., "{{use GetStudent.userId}}"
                String testCaseName = matcher.group(1); // e.g., "GetStudent"
                String key = matcher.group(2); // e.g., "userId"

                // Check captureContext for values
                if (captureContext.getCaptureMap().containsKey(testCaseName)) {
                    Map<String, Object> innerMap = captureContext.getCaptureMap().get(testCaseName);
                    if (innerMap.containsKey(key)) {
                        Object value = innerMap.get(key);
                        requestJson = requestJson.replace(placeholder, value.toString());
                    } else {
                        throw new IllegalArgumentException("Key '" + key + "' not found in testCase '" + testCaseName + "'.");
                    }
                } else {
                    throw new IllegalArgumentException("TestCase '" + testCaseName + "' not found in CaptureMap.");
                }
            }

            // Deserialize the updated JSON string back into the Request object
            Request updatedRequest = objectMapper.readValue(requestJson, Request.class);

            // Set the updated request in the test case
            testCase.setRequest(updatedRequest);
            return testCase;

        } catch (Exception e) {
            throw new RuntimeException("Error while replacing placeholders in the request", e);
        }
    }


}
