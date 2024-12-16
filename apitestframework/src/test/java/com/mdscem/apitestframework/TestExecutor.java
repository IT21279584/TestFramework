package com.mdscem.apitestframework;

import com.mdscem.apitestframework.context.Flow;
import com.mdscem.apitestframework.context.FlowContext;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.flowprocessor.FlowProcessor;
import com.mdscem.apitestframework.frameworkImplementation.RestAssuredCoreFramework;
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

    public void executeTests(){
            try {
                //flow processor process flows
                FlowContext flowContext1 =  flowProcessor.flowProcess();
                //selecting core framework
                CoreFramework coreFramework = frameworkLoader.loadFrameworkFromConfig();

                // Check if the loaded framework is RestAssuredCoreFramework
                if (coreFramework instanceof RestAssuredCoreFramework) {
                    RestAssuredCoreFramework restAssuredFramework = (RestAssuredCoreFramework) coreFramework;

                    for (Map.Entry<String, Flow> flowEntry : flowContext1.getFlowMap().entrySet()) {
                        Flow flow = flowEntry.getValue();
                        List<TestCase> flowTestCaseList = flow.getTestCaseArrayList();

                        // Pass the test cases to the RestAssuredCoreFramework
                        restAssuredFramework.testcaseInitializer(new ArrayList<>(flowTestCaseList));
                    }
                } else {
                    logger.error("Loaded framework is not compatible with RestAssuredCoreFramework.");
                }
            } catch (IOException e) {
                logger.error("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
    }

    //call request processor
    @Test
    public void testExecutor(){
        executeTests();
    }
}
