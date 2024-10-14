package com.mdscem.apitestframework.context;

import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;

import java.util.Optional;

public class AnnotationMain {

    public static void main(String[] args) {
        // Create the repository using the processor
        TestCaseRepository repository = RepositoryProcessor.createRepository(TestCaseRepository.class);

        // Create a new test case entity
        TestCase newTestCase = new TestCase();
        newTestCase.setTestCaseId("1");
//        newTestCase.setTestCaseId("Save User");
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
