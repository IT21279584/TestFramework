package com.mdscem.apitestframework;

import com.mdscem.apitestframework.fileprocessor.flowprocessor.CaptureValidation;
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

    @Override
    public void run(String... args) {
        try {
            flowProcessor.flowProcess();
            CaptureValidation.printAllCaptures();
        } catch (Exception e) {
            logger.error("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
