package com.mdscem.apitestframework;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mdscem.apitestframework.context.Flow;
import com.mdscem.apitestframework.context.FlowContext;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.flowprocessor.FlowProcessor;
import com.mdscem.apitestframework.frameworkImplementation.RestAssuredCoreFramework;
import com.mdscem.apitestframework.requestprocessor.CaptureContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

@SpringBootTest
class TestExecutor {
    private static final Logger logger = LogManager.getLogger(TestExecutor.class);

    @Autowired
    private FlowProcessor flowProcessor;
    @Autowired
    private RestAssuredCoreFramework restAssuredCoreFramework;

    @Autowired
    private CaptureContext captureContext;

    @TestFactory
    public Iterable<DynamicTest> testExecutor() {
        try {
            // Process flows and retrieve test cases.
            FlowContext flowContext = flowProcessor.flowProcess();

            // Create a list to store all dynamic tests
            List<DynamicTest> dynamicTests = new ArrayList<>();

            // Iterate through each flow and extract its test cases
            for (Map.Entry<String, Flow> flowEntry : flowContext.getFlowMap().entrySet()) {
                Flow flow = flowEntry.getValue();

                // Iterate through the test cases in the current flow
                for (TestCase testCase : flow.getTestCaseArrayList()) {
                    // Create a dynamic test for each test case
                    DynamicTest dynamicTest = DynamicTest.dynamicTest(testCase.getTestCaseName(), () -> {
                        executeTestCase(testCase); // Execute the test case
                    });

                    // Add the created dynamic test to the list
                    dynamicTests.add(dynamicTest);
                }
            }

            // Return the list of dynamic tests
            return dynamicTests;

        } catch (IOException e) {
            logger.error("Unexpected error while processing flows: " + e.getMessage(), e);
            e.printStackTrace();

            // Return an empty list in case of error
            return new ArrayList<>();
        }
    }

    /**
     * Executes a single test case.
     *
     * @param testCase The test case to execute.
     */
    private void executeTestCase(TestCase testCase) throws JsonProcessingException {
        try {
            logger.info("Executing test case: " + testCase.getTestCaseName());
            captureContext = new CaptureContext();

            // Call the restAssuredCoreFramework to initialize the test case
            restAssuredCoreFramework.testcaseInitializer(Arrays.asList(testCase));
            logger.info("Test case executed successfully: " + testCase.getTestCaseName());

        } catch (Exception e) {
            logger.error("Error executing test case: " + testCase.getTestCaseName(), e);
            throw e;
        }
    }
}
