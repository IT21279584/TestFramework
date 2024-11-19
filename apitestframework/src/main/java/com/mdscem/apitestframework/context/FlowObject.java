package com.mdscem.apitestframework.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FlowObject {
    private List<JsonNode> flowContentList;
    private ArrayList<TestCase> testCaseArrayList;

    public void setFlowContentList(List<JsonNode> flowContentList) {
        this.flowContentList = flowContentList;
    }

    public ArrayList<TestCase> getTestCaseArrayList() {
        return testCaseArrayList;
    }

    public void setTestCaseArrayList(ArrayList<TestCase> testCaseArrayList) {
        this.testCaseArrayList = testCaseArrayList;
    }
}
