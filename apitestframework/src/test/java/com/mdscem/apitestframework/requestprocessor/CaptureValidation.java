package com.mdscem.apitestframework.requestprocessor;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CaptureValidation {

    @Autowired
    private CaptureContext captureContext;

    // Process captures and store them with the test case name
    public void processCaptures(TestCase testCase) {
        String testCaseName = testCase.getTestCaseName(); // Assume this method exists in TestCase
        Map<String, Object> capture = testCase.getCapture();

        if (capture == null || capture.isEmpty()) {
            return;
        }

        // Store the captures in the context map using the test case name
        captureContext.addCapturesForTestCase(testCaseName, capture);
        printAllCaptures();
    }

    // Print all captures for all test cases
    public void printAllCaptures() {
        captureContext.getCaptureMap().forEach((testCaseName, captures) -> {
            System.out.println("Test Case: " + testCaseName);
            captures.forEach((key, value) -> {
                System.out.println("  Key: " + key + ", Value: " + value);
            });
        });
    }
}
