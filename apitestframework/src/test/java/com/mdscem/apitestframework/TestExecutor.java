package com.mdscem.apitestframework;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mdscem.apitestframework.context.Flow;
import com.mdscem.apitestframework.context.FlowContext;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.flowprocessor.FlowProcessor;
import com.mdscem.apitestframework.frameworkImplementation.RestAssuredCoreFramework;
import com.mdscem.apitestframework.requestprocessor.CaptureContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.*;

import static com.mdscem.apitestframework.constants.Constant.REPORT_GENERATION_PATH;
import static com.mdscem.apitestframework.constants.Constant.REPORT_NAME;

@SpringBootTest
class TestExecutor {
    private static final Logger logger = LogManager.getLogger(TestExecutor.class);

    @Autowired
    private FlowProcessor flowProcessor;
    @Autowired
    private RestAssuredCoreFramework restAssuredCoreFramework;
    @Autowired
    private CaptureContext captureContext;
    private static ExtentReports extent;
    public static ExtentTest test;

    @BeforeAll
    public static void beforeAll() throws Exception {
        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_GENERATION_PATH);
        spark.config().setReportName(REPORT_NAME);
        extent = new ExtentReports();
        extent.attachReporter(spark);
    }

    @TestFactory
    public List<DynamicTest> testExecutor() {
        List<DynamicTest> dynamicTests = new ArrayList<>();

        try {
            FlowContext flowContext = flowProcessor.flowProcess();

            for (Map.Entry<String, Flow> flowEntry : flowContext.getFlowMap().entrySet()) {
                String flowName = flowEntry.getKey();
                Flow flow = flowEntry.getValue();

                logger.info("Starting execution for flow: " + flowName);

                for (TestCase testCase : flow.getTestCaseArrayList()) {
                    dynamicTests.add(DynamicTest.dynamicTest(
                            testCase.getTestCaseName(),
                            () -> executeTestCase(testCase, flowName)
                    ));
                }

                // create capture context
                dynamicTests.add(DynamicTest.dynamicTest(
                        "Flow: " + flowName + " | Created CaptureContext (Internal)",
                        () -> {
                            createNewCaptureContext(flowName);
                        }
                ));
            }
        } catch (IOException e) {
            logger.error("Unexpected error while processing flows: " + e.getMessage(), e);
        }

        return dynamicTests;
    }


    /**
     * Executes a single test case and logs it under the corresponding flow.
     *
     * @param testCase The test case to execute.
     * @param flowName The name of the flow the test case belongs to.
     */
    private void executeTestCase(TestCase testCase, String flowName) throws JsonProcessingException {
        try {
            logger.info("Executing test case: " + testCase.getTestCaseName() + " in flow: " + flowName);
            test = extent.createTest(testCase.getTestCaseName());
            test.log(Status.INFO, "Request Method: " + testCase.getRequest().getMethod());
            test.log(Status.INFO, "Request URL: " + testCase.getBaseUri() + testCase.getRequest().getPath());
            test.log(Status.INFO, "Captures: " + testCase.getCapture());
            test.assignCategory(testCase.getRequest().getMethod());

            // Initialize and execute the test case using RestAssuredCoreFramework
            restAssuredCoreFramework.testcaseInitializer(Arrays.asList(testCase));

            logger.info("Test case executed successfully: " + testCase.getTestCaseName());
            test.pass("Test passed successfully");
        } catch (AssertionError | Exception e) {
            logger.error("Test case execution failed: " + testCase.getTestCaseName(), e);
            test.fail("Test case failed: " + e.getMessage());
            throw e;
        }
    }

    //Create  capture context for each flow
    private void createNewCaptureContext(String flowName) {
        try {
            // Create a new capture map
            captureContext.setCaptureMap(new HashMap<String, Map<String, Object>>());
        } catch (Exception e) {
            logger.error("Failed to create capture context for flow: " + flowName, e);
            throw e;
        }
    }

    @AfterAll
    public static void afterAll() {
        extent.flush();
    }
}
