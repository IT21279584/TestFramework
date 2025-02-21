package com.mdscem.apitestframework;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.mdscem.apitestframework.constants.Constant;
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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;



@SpringBootTest
public class TestExecutorOld {
    private static final Logger logger = LogManager.getLogger(TestExecutorOld.class);

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
    public static void beforeAll() {
        try {
            String baseDirectoryPath = System.getProperty("base.directory.path");
            System.out.println("Base Directory Path: " + baseDirectoryPath);
            System.out.println("Report Directory Path: " + Constant.REPORT_PATH);
            extent = new ExtentReports();
            ExtentSparkReporter spark = new ExtentSparkReporter(Constant.REPORT_PATH);
            spark.config().setReportName("API TestFramework Report");
            extent.attachReporter(spark);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize ExtentReports", e);
        }
    }

    @TestFactory
    public Stream<DynamicTest> testExecutor() {
        System.out.println("Starting JUnit test execution from test executor method...");
        List<DynamicTest> dynamicTests = new ArrayList<>();

        try {
            FlowContext flowContext = flowProcessor.flowProcess();
            for (Map.Entry<String, Flow> flowEntry : flowContext.getFlowMap().entrySet()) {
                String flowName = flowEntry.getKey();
                Flow flow = flowEntry.getValue();

                dynamicTests.add(DynamicTest.dynamicTest(
                        "Flow: " + flowName + " | Created CaptureContext (Internal)",
                        () -> createNewCaptureContext(flowName)
                ));

                for (TestCase testCase : flow.getTestCaseArrayList()) {
                    dynamicTests.add(DynamicTest.dynamicTest(
                            flowName + " -> " + testCase.getTestCaseName(),
                            () -> executeTestCase(testCase, flowName)
                    ));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute flow-processor", e);
        }

        return dynamicTests.stream();
    }

    /**
     * Executes a single test case and logs it under the corresponding flow.
     *
     * @param testCase The test case to execute.
     * @param flowName The name of the flow the test case belongs to.
     */
    private void executeTestCase(TestCase testCase, String flowName) throws IOException {
        try {
            test = extent.createTest(flowName + " -> " + testCase.getTestCaseName());
            test.log(Status.INFO, "Request Method: " + testCase.getRequest().getMethod());
            test.log(Status.INFO, "Request URL: " + testCase.getBaseUri() + testCase.getRequest().getPath());
            test.assignCategory(testCase.getRequest().getMethod());

            // Execute the core framework
            executeCoreFramework(testCase);

            // Log the test case status
            test.pass("Test passed successfully");
            logger.info("✅ Test Passed: " + flowName + " -> " + testCase.getTestCaseName());
        } catch (AssertionError | Exception e) {
            test.fail("Test case failed: " + e.getMessage());
            logger.error("❌ Test Failed: " + flowName + " -> " + testCase.getTestCaseName() + " | Error: " + e.getMessage());
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
            throw e;
        }
    }

    @AfterAll
    public static void afterAll() {
        if (extent != null) {
            extent.flush();
        } else {
            System.err.println("ExtentReports not initialized.");
        }
    }

}
