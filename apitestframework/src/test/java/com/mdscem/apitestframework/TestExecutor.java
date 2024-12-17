package com.mdscem.apitestframework;

import com.mdscem.apitestframework.context.Flow;
import com.mdscem.apitestframework.context.FlowContext;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.flowprocessor.FlowProcessor;
import com.mdscem.apitestframework.frameworkImplementation.RestAssuredCoreFramework;
import com.mdscem.apitestframework.requestprocessor.CaptureContext;
import com.mdscem.apitestframework.requestprocessor.CoreFramework;
import com.mdscem.apitestframework.requestprocessor.frameworkconfig.FrameworkLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
class TestExecutor {
    private static final Logger logger = LogManager.getLogger(TestExecutor.class);

    @Autowired
    private FlowProcessor flowProcessor;
    @Autowired
    private FrameworkLoader frameworkLoader;
    private CaptureContext captureContext = CaptureContext.getInstance(); // Use Singleton

    private RestAssuredCoreFramework restAssuredCoreFramework = new RestAssuredCoreFramework();

    public void executeTests() {
        try {
            //flow processor process flows
            FlowContext flowContext = flowProcessor.flowProcess();

            // Check if the loaded framework is RestAssuredCoreFramework
            for (Map.Entry<String, Flow> flowEntry : flowContext.getFlowMap().entrySet()) {

                captureContext = new CaptureContext();

                Flow flow = flowEntry.getValue();
                List<TestCase> flowTestCaseList = flow.getTestCaseArrayList();

                // Pass the test cases to the RestAssuredCoreFramework
                restAssuredCoreFramework.testcaseInitializer(new ArrayList<>(flowTestCaseList));
//              captureContext.clearCaptures();

            }
        } catch (IOException e) {
            logger.error("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //call request processor
    @Test
    public void testExecutor() {
        executeTests();
    }
}
