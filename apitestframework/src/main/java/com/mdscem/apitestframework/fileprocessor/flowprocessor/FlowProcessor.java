package com.mdscem.apitestframework.fileprocessor.flowprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.context.Flow;
import com.mdscem.apitestframework.context.FlowContext;
import com.mdscem.apitestframework.fileprocessor.filereader.FlowContentReader;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import com.mdscem.apitestframework.fileprocessor.validator.TestCaseReplacer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class FlowProcessor {

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

                // Print flowObject, flowObjectMap, and testCaseMap data
                printFlowObjectData(flow);
                printFlowObjectMap();
                printTestCaseMap();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Prints the flowObject data.
     *
     */
    private void printFlowObjectData(Flow flowObject) {
        try {
            String jsonString = objectMapper.writeValueAsString(flowObject);
            System.out.println("FlowObject data: " + jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error converting FlowObject to JSON: " + e.getMessage());
        }
    }

    /**
     * Prints the flowObjectMap data.
     */
    private void printFlowObjectMap() {
        try {
            String jsonString = objectMapper.writeValueAsString(testCaseContext.flowMap);
            System.out.println("FlowObjectMap data: " + jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error converting FlowObjectMap to JSON: " + e.getMessage());
        }
    }

    /**
     * Prints the testCaseMap data.
     */
    private void printTestCaseMap() {
        try {
            String jsonString = objectMapper.writeValueAsString(testCaseContext.testCaseMap);
            System.out.println("TestCaseMap data: " + jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error converting TestCaseMap to JSON: " + e.getMessage());
        }
    }
}
