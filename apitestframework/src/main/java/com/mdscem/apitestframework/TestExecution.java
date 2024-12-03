package com.mdscem.apitestframework;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.flowprocessor.CaptureReplacer;
import com.mdscem.apitestframework.fileprocessor.flowprocessor.CaptureValidation;

import java.util.HashMap;
import java.util.Map;

public class TestExecution {

    public static void main(String[] args) {
        // Example TestCase with capture keys
        TestCase testCase = new TestCase();
        Map<String, Object> capture = new HashMap<>();
        capture.put("nic", null);
        capture.put("hometown", null);
        testCase.setCapture(capture);


        // Process initial captures
        CaptureValidation.processCaptures(testCase);
        CaptureValidation.printAllCaptures();

        // Simulate response after test case execution
        String responseString = "{\"nic\": \"123456789V\", \"hometown\": \"Colombo\"}";

        // Update captures with response data
        CaptureReplacer.updateCapturesFromResponse(responseString);

        // Print all captures to verify updates
        CaptureValidation.printAllCaptures();
    }
}
