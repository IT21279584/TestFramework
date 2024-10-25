package com.mdscem.apitestframework.fileprocessor;

import com.mdscem.apitestframework.fileprocessor.filereader.FileConfigLoader;
import com.mdscem.apitestframework.fileprocessor.validator.PlaceholderReplacer;
import org.apache.log4j.BasicConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApiTestMain implements CommandLineRunner {

    @Autowired
    PlaceholderReplacer placeholderReplacer;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ApiTestMain.class);
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize basic log4j configuration
        BasicConfigurator.configure();

        // Read the test cases file content
        String content = FileConfigLoader.readFile(FileConfigLoader.loadTestCasesFiles());
        System.out.println("Original content:");
        System.out.println(content);

        // Define the path to the values.yaml file
        String valuePath = "/home/kmedagoda/Documents/Kavinda Final/final TestFramework/Hansaka-test/TestFramework/apitestframework/src/main/resources/values.yaml";

        // Perform placeholder replacement
        String modifiedContent = placeholderReplacer.replacePlaceholders(content, valuePath);

        // Output the modified content
        System.out.println("Modified content after placeholder replacement:");
        System.out.println(modifiedContent);
    }
}
