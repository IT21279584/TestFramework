package com.mdscem.apitestframework;

import com.mdscem.apitestframework.fileprocessor.filereader.FlowContentReader;
import com.mdscem.apitestframework.fileprocessor.flowprocessor.FlowProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.IOException;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApiTestMain implements CommandLineRunner {

    @Autowired
    private FlowProcessor flowProcessor;

    @Override
    public void run(String... args) {
    try {

        flowProcessor.flowProcess();

        } catch (IOException e) {
            System.err.println("Error occurred while loading files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
