package com.mdscem.apitestframework.requestprocessor.capturehandling;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


public class CaptureValidation {

    @Autowired
    private static CaptureContext captureContext;

    // get captures from TestCase and put into captureMap
    public static void processCaptures(TestCase testCase) {

        Map<String, Object> capture = testCase.getCapture();

        if (capture == null || capture.isEmpty()) {
            return;
        }

        // Add all captures to the internal map
        capture.forEach((key, value) -> {
            captureContext.getCaptureMap().put(key, value); // Store key-value pair
        });
    }


    // Print all captures
    public static void printAllCaptures() {
        captureContext.getCaptureMap().forEach((key, value) -> {
            System.out.println("Key: " + key + ", Value: " + value);
        });
    }
}