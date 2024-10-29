package com.mdscem.apitestframework.fileprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.mdscem.apitestframework.context.TestCaseRepository;
import com.mdscem.apitestframework.fileprocessor.filereader.TestCasesToJsonNodeReader;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer;
import org.apache.log4j.BasicConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.util.List;

import static com.mdscem.apitestframework.constants.Constant.MULTIPLE_FILE_PATH;
import static com.mdscem.apitestframework.constants.Constant.VALUE_FILE_PATH;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "com.mdscem.apitestframework") // Adjust to your root package
public class ApiTestMain implements CommandLineRunner {

    @Autowired
    private TestCaseReplacer testCaseReplacer;

    @Autowired
    private TestCasesToJsonNodeReader testCasesToJsonNodeReader;

    @Autowired
    @Qualifier("testCaseRepositoryImpl")
    private TestCaseRepository testCaseRepository;

    public static void main(String[] args) {
        SpringApplication.run(ApiTestMain.class, args);
    }

    @Override
    public void run(String... args) {
        // Initialize basic log4j configuration
        BasicConfigurator.configure();

        try {
            // Load test case file paths from fileconfig.json
            List<String> testCaseFilePaths = testCasesToJsonNodeReader.loadTestCaseFilePaths(MULTIPLE_FILE_PATH);

            // Process each test case file individually
            for (String filePath : testCaseFilePaths) {
                // Load the test case and values for the current file
                JsonNode testCaseNode = testCasesToJsonNodeReader.loadFileAsJsonNode(filePath);
                JsonNode valuesNode = testCasesToJsonNodeReader.loadFileAsJsonNode(VALUE_FILE_PATH);

                // Replace placeholders in the current test case node using values node
                JsonNode finalResult = testCaseReplacer.replacePlaceholdersInNode(testCaseNode, valuesNode);

                // Debug: Print the finalResult for verification
                System.out.println("Final Result Node: \n" + finalResult.toPrettyString());

                // Check if testCaseId exists
                if (finalResult.has("testCaseId")) {
                    // Create a new TestCase entity and populate its fields
                    TestCase newTestCase = new TestCase();
                    newTestCase.setTestCaseId(finalResult.get("testCaseId").asText()); // Example field assignment

                    // Save the new test case entity to the repository
                    testCaseRepository.save(newTestCase);
                    System.out.println("Saved new test case with ID: " + newTestCase.getTestCaseId());
                } else {
                    System.err.println("testCaseId not found in the loaded JSON for file: " + filePath);
                }
            }

        } catch (IOException e) {
            System.err.println("Error occurred while loading files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
