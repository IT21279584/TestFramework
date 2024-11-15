package com.mdscem.apitestframework.fileprocessor.flowprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.mdscem.apitestframework.constants.Constant;
import com.mdscem.apitestframework.context.FlowObject;
import com.mdscem.apitestframework.context.TestCaseContext;
import com.mdscem.apitestframework.fileprocessor.TestCaseProcessor;
import com.mdscem.apitestframework.fileprocessor.filereader.FlowContentReader;
import com.mdscem.apitestframework.fileprocessor.filereader.TestCasesReader;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
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
    private TestCasesReader testCasesReader;


    public void abc() throws IOException {
        Path flowPathDir = Paths.get(Constant.FLOWS_DIRECTORY);
        // Load include files and combine them into one node
        List<JsonNode> includeNodes = testCasesReader.loadFilesFromDirectory();
        // all include files content
        JsonNode combinedValuesNode = flowContentReader.combineNodes(includeNodes);
        // list of flow paths
        List<Path> flowPaths = flowContentReader.getFlowFilesFromDirectory(flowPathDir);

        for(Path flowPath : flowPaths){
            String flowFileName;
            List<JsonNode> flowContentList = new ArrayList<>();
            ArrayList<TestCase> flowContentTestCaseList = new ArrayList<>();
            try{
                flowFileName= String.valueOf(flowPath.getFileName());
                flowContentList = flowContentReader.getFlowContentAsJsonNodes(flowPath);
                for (JsonNode flowTestCase : flowContentList){
                    String testCaseName = flowTestCase.get("testCase").get("name").asText();
                    if (testCaseContext.testCaseMap.containsKey(testCaseName)){
                        TestCase testCase = testCaseContext.testCaseMap.get(testCaseName);
                        TestCase completeTestCase = flowContentReader.replaceTestCaseWithFlowData(testCase,flowTestCase);
                        flowContentTestCaseList.add(completeTestCase);
                    }
                    TestCase newTestCase = flowContentReader.readNewTestCase(testCaseName);
                    testCaseContext.testCaseMap.put(testCaseName,newTestCase);
                    TestCase completeTestCase = flowContentReader.replaceTestCaseWithFlowData(newTestCase,flowTestCase);
                    flowContentTestCaseList.add(completeTestCase);
                }
                flowObject.setFlowContentList(flowContentList);
                flowObject.setTestCaseArrayList(flowContentTestCaseList);
                testCaseContext.flowObjectMap.put(flowFileName,flowObject);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
