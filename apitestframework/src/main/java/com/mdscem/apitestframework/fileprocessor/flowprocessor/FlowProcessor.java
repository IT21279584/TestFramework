package com.mdscem.apitestframework.fileprocessor.flowprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.context.FlowObject;
import com.mdscem.apitestframework.context.TestCaseContext;
import com.mdscem.apitestframework.fileprocessor.filereader.FlowContentReader;
import com.mdscem.apitestframework.fileprocessor.filereader.TestCasesReader;
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
    private TestCaseContext testCaseContext;
    @Autowired
    private FlowObject flowObject;
    @Autowired
    private TestCaseReplacer testCaseReplacer;

    private static final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper

    public void flowProcess() throws IOException {
        Path flowPathDir = Paths.get(Constant.FLOWS_DIRECTORY);
        // list of flow paths
        List<Path> flowPaths = flowContentReader.getFlowFilesFromDirectory(flowPathDir);

        for (Path flowPath : flowPaths) {
            String flowFileName;
            List<JsonNode> flowContentList = new ArrayList<>();
            ArrayList<TestCase> flowContentTestCaseList = new ArrayList<>();
            try {
                flowFileName = String.valueOf(flowPath.getFileName());
                flowContentList = flowContentReader.getFlowContentAsJsonNodes(flowPath);

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

                flowObject.setFlowContentList(flowContentList);
                flowObject.setTestCaseArrayList(flowContentTestCaseList);
                testCaseContext.flowObjectMap.put(flowFileName, flowObject);



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Print flowObject, flowObjectMap, and testCaseMap data
        printFlowObjectData(flowObject);
        printFlowObjectMap();
        printTestCaseMap();
    }

    /**
     * Prints the flowObject data.
     *
     * @param flowObject The FlowObject to be printed.
     */
    private void printFlowObjectData(FlowObject flowObject) {
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
            String jsonString = objectMapper.writeValueAsString(testCaseContext.flowObjectMap);
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
