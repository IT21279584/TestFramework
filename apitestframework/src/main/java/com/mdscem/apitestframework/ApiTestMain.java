package com.mdscem.apitestframework;


import com.mdscem.apitestframework.fileprocessor.flowprocessor.FlowProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApiTestMain implements CommandLineRunner {
    private static final Logger logger = LogManager.getLogger(ApiTestMain.class);
    @Autowired
    private FlowProcessor flowProcessor;
//
//    @Autowired
//    private  CaptureValidation captureValidation;
//
//    @Autowired
//    private CaptureReplacer captureReplacer;

    @Override
    public void run(String... args) {
        try {
            flowProcessor.flowProcess();


            // Simulate response after test case execution
//            String responseString = "{\"nic\": \"123456789V\", \"hometown\": \"Colombo\"}";
//
//            // Update captures with response data
//            captureReplacer.updateCapturesFromResponse(responseString);
//
//            // Print all captures to verify updates
//            captureValidation.printAllCaptures();
//
//            String path = "api/users?category={{use GetStudent.nic}}&hometown={{use UpdateCourse.hometown}}";
//            String resolvedPath = captureReplacer.replaceParameterPlaceholders(path);
//            System.out.println("Resolved Path: " + resolvedPath);



        } catch (Exception e) {
            logger.error("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
