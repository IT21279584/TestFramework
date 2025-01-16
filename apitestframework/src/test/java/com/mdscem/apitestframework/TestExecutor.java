package com.mdscem.apitestframework;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.mdscem.apitestframework.context.Flow;
import com.mdscem.apitestframework.context.FlowContext;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.flowprocessor.FlowProcessor;
import com.mdscem.apitestframework.requestprocessor.CaptureContext;
import com.mdscem.apitestframework.requestprocessor.CaptureReplacer;
import com.mdscem.apitestframework.requestprocessor.CaptureValidation;
import com.mdscem.apitestframework.requestprocessor.CoreFramework;
import com.mdscem.apitestframework.requestprocessor.frameworkconfig.FrameworkLoader;
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
    private CaptureContext captureContext;
    @Autowired
    private FrameworkLoader frameworkLoader;
    @Autowired
    private CaptureValidation captureValidation;
    @Autowired
    private CaptureReplacer captureReplacer;
    @Autowired
    CoreFramework coreFramework;

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
        // A list to hold dynamically generated test cases
        List<DynamicTest> dynamicTests = new ArrayList<>();

        try {
            // Retrieve the FlowContext which contains all flows to be executed
            FlowContext flowContext = flowProcessor.flowProcess();

            // Iterate through each flow defined in the FlowContext
            for (Map.Entry<String, Flow> flowEntry : flowContext.getFlowMap().entrySet()) {
                String flowName = flowEntry.getKey(); // Name of the flow
                Flow flow = flowEntry.getValue();    // The flow object containing test cases

                // Log the start of execution for the current flow
                logger.info("Starting execution for flow: " + flowName);

                // Iterate through all test cases associated with the current flow
                for (TestCase testCase : flow.getTestCaseArrayList()) {
                    /*
                     * Create a dynamic test for the current test case.
                     * Dynamic tests are useful for scenarios where the number or nature of tests
                     * isn't known beforehand, such as when tests are derived from external data
                     * (files, databases, or flows, in this case).
                     *
                     * dynamicTest() is a factory method that creates an instance of a dynamic test.
                     * It takes two arguments:
                     * 1. displayName: A descriptive name for the test (testCase.getTestCaseName()).
                     * 2. Executable: A lambda or method reference representing the test logic
                     *    (executeTestCase(testCase, flowName)).
                     */
                    dynamicTests.add(DynamicTest.dynamicTest(
                            testCase.getTestCaseName(),  // Descriptive name of the test case for reporting
                            () -> executeTestCase(testCase, flowName) // Actual test logic
                    ));
                }

                dynamicTests.add(DynamicTest.dynamicTest(
                        "Flow: " + flowName + " | Created CaptureContext (Internal)", // Display name
                        () -> {
                            createNewCaptureContext(flowName);  // Setup logic for the capture context
                        }
                ));
            }
        } catch (IOException e) {
            // Log and handle any unexpected errors that occur during flow processing
            logger.error("Unexpected error while processing flows: " + e.getMessage(), e);
        }

        // Return the list of dynamically created test cases to JUnit
        return dynamicTests;
    }

    /**
     * Executes a single test case and logs it under the corresponding flow.
     *
     * @param testCase The test case to execute.
     * @param flowName The name of the flow the test case belongs to.
     */
    private void executeTestCase(TestCase testCase, String flowName) throws IOException {
        try {
            logger.info("Executing test case: " + testCase.getTestCaseName() + " in flow: " + flowName);
            test = extent.createTest(testCase.getTestCaseName());
            test.log(Status.INFO, "Request Method: " + testCase.getRequest().getMethod());
            test.log(Status.INFO, "Request URL: " + testCase.getBaseUri() + testCase.getRequest().getPath());
            test.assignCategory(testCase.getRequest().getMethod());

            //execute core framework
            executeCoreFramework(testCase);

            logger.info("Test case executed successfully: " + testCase.getTestCaseName());
            test.pass("Test passed successfully");
        } catch (AssertionError | Exception e) {
            logger.error("Test case execution failed: " + testCase.getTestCaseName(), e);
            test.fail("Test case failed: " + e.getMessage());
            throw e;
        }
    }

    private void executeCoreFramework(TestCase testCase) throws IOException {
        coreFramework = frameworkLoader.loadFrameworkFromConfig();
        captureValidation.processCaptures(testCase);
        TestCase replacedTestCase = captureReplacer.replaceParameterPlaceholders(testCase);
        String res = coreFramework.createFrameworkTypeTestFileAndexecute(replacedTestCase);
        captureReplacer.updateCapturesFromResponse(res);
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
