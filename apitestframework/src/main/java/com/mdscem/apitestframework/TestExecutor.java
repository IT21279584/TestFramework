package com.mdscem.apitestframework;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.constants.DirectoryPaths;
import com.mdscem.apitestframework.context.Flow;
import com.mdscem.apitestframework.context.FlowContext;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.flowprocessor.FlowProcessor;
import com.mdscem.apitestframework.requestprocessor.CaptureContext;
import com.mdscem.apitestframework.requestprocessor.CaptureReplacer;
import com.mdscem.apitestframework.requestprocessor.CaptureProcessor;
import com.mdscem.apitestframework.requestprocessor.CoreFramework;
import com.mdscem.apitestframework.requestprocessor.frameworkconfig.FrameworkLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class TestExecutor {
    private static final Logger logger = LogManager.getLogger(TestExecutor.class);
    @Autowired
    private FlowProcessor flowProcessor;
    @Autowired
    private CaptureContext captureContext;
    @Autowired
    private FrameworkLoader frameworkLoader;
    @Autowired
    private CaptureProcessor captureValidation;
    @Autowired
    private CaptureReplacer captureReplacer;
    @Autowired
    private CoreFramework coreFramework;
    private ExtentReports extent;
    private ExtentTest test;

    private void initializeReports() {
        try {
            extent = new ExtentReports();
            ExtentSparkReporter spark = new ExtentSparkReporter(DirectoryPaths.REPORT_DIRECTORY);
            spark.config().setReportName(Constant.REPORT_NAME);
            extent.attachReporter(spark);

            logger.info("Extent report initialized.");
        } catch (Exception e) {
            logger.error("❌ Error initializing Report : " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize ExtentReports", e);
        }
    }

    private void executeTests() {
        logger.info("Starting test execution...");
        try {
            coreFramework = frameworkLoader.loadFrameworkFromConfig();
            FlowContext flowContext = flowProcessor.flowProcess();

            for (Map.Entry<String, Flow> flowEntry : flowContext.getFlowMap().entrySet()) {
                String flowName = flowEntry.getKey();
                Flow flow = flowEntry.getValue();

                // Create a new capture context for the flow
                createNewCaptureContext(flowName);

                for (TestCase testCase : flow.getTestCaseArrayList()) {
                    executeTestCase(testCase, flowName);
                }
            }
        } catch (IOException e) {
            logger.error("❌ Error during test execution: " + e.getMessage(), e);
            throw new RuntimeException("Failed to execute flow-processor", e);
        }
    }

    private void executeTestCase(TestCase testCase, String flowName) throws IOException {
        try {
            logger.info("Test Case : " + testCase.getTestCaseName());
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
        }
    }

    private void executeCoreFramework(TestCase testCase) throws Exception {
        captureValidation.processCaptures(testCase);
        TestCase replacedTestCase = captureReplacer.replaceParameterPlaceholders(testCase);
        String res = coreFramework.createFrameworkTypeTestFileAndExecute(replacedTestCase);
        captureReplacer.updateCapturesFromResponse(res, replacedTestCase);
    }

    private void createNewCaptureContext(String flowName) {
        try {
            captureContext.setCaptureMap(new HashMap<>());
            logger.info("Capture context created for flow: " + flowName);
        } catch (Exception e) {
            logger.error("❌ Error creating capture context for " + flowName, e);
            throw new RuntimeException("Failed to create capture context for " + flowName, e);
        }
    }

    private void finalizeReports() {
        if (extent != null) {
            extent.flush();
            logger.info("Test execution completed.");
            logger.info("Report generated at: " + DirectoryPaths.REPORT_DIRECTORY);
        } else {
            logger.error("Report is not initialized.");
        }
    }
    public void execute(){
        logger.info("...Initializing API Test Executor...");
        initializeReports();
        executeTests();
        finalizeReports();
    }
}