package com.mdscem.apitestframework.requestprocessor;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CaptureValidation {

    private final CaptureContext captureContext = CaptureContext.getInstance(); // Use Singleton
    // Process captures and store them with the test case name
    public void processCaptures(TestCase testCase) {
        try{
            String testCaseName = testCase.getTestCaseName(); // Assume this method exists in TestCase
            Map<String, Object> capture = testCase.getCapture();

            if (capture == null || capture.isEmpty()) {
                return;
            }
            System.out.println("Test case name: " + testCaseName);
            System.out.println("Capture map: " + capture);


            // Store the captures in the context map using the test case name
            captureContext.addCapturesForTestCase(testCaseName, capture);
            printAllCaptures();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // Print all captures for all test cases
    public void printAllCaptures() {
        if (captureContext == null) {
            System.err.println("Error: captureContext is null.");
            return;
        }

        captureContext.getCaptureMap().forEach((testCaseName, captures) -> {
            System.out.println("Test Case: " + testCaseName);
            captures.forEach((key, value) -> {
                System.out.println("  Key: " + key + ", Value: " + value);
            });
        });
    }
}
