package com.mdscem.apitestframework.requestprocessor;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CaptureValidation {
    private static final Logger logger = LogManager.getLogger(CaptureContext.class);

    private final CaptureContext captureContext = CaptureContext.getInstance(); // Use Singleton
    // Process captures and store them with the test case name
    public void processCaptures(TestCase testCase) {
        try{
            String testCaseName = testCase.getTestCaseName(); // Assume this method exists in TestCase
            Map<String, Object> capture = testCase.getCapture();

            if (capture == null || capture.isEmpty()) {
                return;
            }
            logger.info("Test case name: " + testCaseName);
            logger.info("Capture map: " + capture);


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
            logger.error("Error: captureContext is null.");
            return;
        }

        captureContext.getCaptureMap().forEach((testCaseName, captures) -> {
            logger.info("Test Case: " + testCaseName);
            captures.forEach((key, value) -> {
                logger.info("  Key: " + key + ", Value: " + value);
            });
        });
    }
}
