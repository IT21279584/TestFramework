package com.mdscem.apitestframework.fileprocessor.flowprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.context.Flow;
import com.mdscem.apitestframework.context.FlowContext;
import com.mdscem.apitestframework.fileprocessor.filereader.FlowContentReader;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class FlowProcessor {

    private static final Logger logger = LogManager.getLogger(FlowProcessor.class);
    @Autowired
    private FlowContentReader flowContentReader;
    @Autowired
    private FlowContext testCaseContext;
    @Autowired
    private Flow flow;
    @Autowired
    private TestCaseReplacer testCaseReplacer;

    private static final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper

    public void flowProcess() throws IOException {
        Path flowPathDir = Paths.get(Constant.FLOWS_DIRECTORY);
        // list of flow paths
        List<Path> flowPaths = flowContentReader.getFlowFilesFromDirectory(flowPathDir);

        for (Path flowPath : flowPaths) {
            String flowFileName;
            ArrayList<TestCase> flowContentTestCaseList = new ArrayList<>();
            try {
                flowFileName = String.valueOf(flowPath.getFileName());
                List<JsonNode> flowContentList = flowContentReader.getFlowContentAsJsonNodes(flowPath);

                for (JsonNode flowTestCase : flowContentList) {

                    String testCaseName = flowTestCase.get("testCase").get("name").asText();
                    if (testCaseContext.testCaseMap.containsKey(testCaseName)) {
                        TestCase testCase = testCaseContext.testCaseMap.get(testCaseName);
                        TestCase completeTestCase = testCaseReplacer.replaceTestCaseWithFlowData(testCase, flowTestCase);
                        flowContentTestCaseList.add(completeTestCase);
                    }
                    TestCase newTestCase = flowContentReader.readNewTestCase(testCaseName);
                    testCaseContext.testCaseMap.put(testCaseName, newTestCase);
                    TestCase completeTestCase = testCaseReplacer.replaceTestCaseWithFlowData(newTestCase, flowTestCase);
                    flowContentTestCaseList.add(completeTestCase);
                }

                flow.setFlowContentList(flowContentList);
                flow.setTestCaseArrayList(flowContentTestCaseList);
                testCaseContext.flowMap.put(flowFileName, flow);

                logger.info("FlowObject data: {}", objectMapper.writeValueAsString(flow));
                logger.info("FlowObjectMap data: {}", objectMapper.writeValueAsString(testCaseContext.flowMap));
                logger.info("TestCaseMap data: {}", objectMapper.writeValueAsString(testCaseContext.testCaseMap));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
