package com.mdscem.apitestframework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.IOException;
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApiTestMain implements CommandLineRunner {
    private static final Logger logger = LogManager.getLogger(ApiTestMain.class);
    @Autowired
    private TestExecutor executor;

    @Override
    public void run(String... args) throws IOException {
        logger.info("Initializing API Test Executor...");
        executor.initializeReports();
        executor.executeTests();
        executor.finalizeReports();
    }
}
