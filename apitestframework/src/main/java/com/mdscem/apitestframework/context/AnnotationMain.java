package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class AnnotationMain implements CommandLineRunner {

    @Autowired
    private TestCaseRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(AnnotationMain.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Create a new test case entity
        TestCase newTestCase = new TestCase();
        newTestCase.setTestCaseId("1");
        newTestCase.setBaseUri("https://reqres.in/api/users/1");

        // Save the new test case
        repository.save(newTestCase);

        // Find the test case by ID
        TestCase retrievedTestCase = repository.findById("1")
                .orElseThrow(() -> new IllegalArgumentException("Test case with ID 1 not found"));
        System.out.println("Found Test Case: " + retrievedTestCase.getTestCaseId());

        // Delete the test case by ID
        repository.deleteById("1");
        System.out.println("Test case with ID 1 deleted.");
    }
}
