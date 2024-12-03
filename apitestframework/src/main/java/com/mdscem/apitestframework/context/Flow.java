package com.mdscem.apitestframework.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.mdscem.apitestframework.fileprocessor.filereader.model.TestCase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class Flow implements Testable{

    //flow file content
    private List<JsonNode> flowContentList;

    //testcases are stored into array
    private ArrayList<TestCase> testCaseArrayList;
    private Iterator<TestCase> testCaseIterator;


    public List<JsonNode> getFlowContentList() {
        return flowContentList;
    }

    public void setFlowContentList(List<JsonNode> flowContentList) {
        this.flowContentList = flowContentList;
    }

    public ArrayList<TestCase> getTestCaseArrayList() {
        return testCaseArrayList;
    }

//    public void setTestCaseArrayList(ArrayList<TestCase> testCaseArrayList) {
//        this.testCaseArrayList = testCaseArrayList;
//    }


    public void setTestCaseArrayList(ArrayList<TestCase> testCaseArrayList) {
        this.testCaseArrayList = testCaseArrayList;
        // Initialize the iterator when the list is set
        this.testCaseIterator = testCaseArrayList.iterator();
    }

    /**
     * Returns the next TestCase in the list or null if no more elements.
     */
    public TestCase getNextTestCase() {
        if (testCaseIterator != null && testCaseIterator.hasNext()) {
            return testCaseIterator.next();
        }
        return null; // Return null if no more test cases
    }

    /**
     * Resets the iterator to the beginning of the list.
     */
    public void resetTestCaseIterator() {
        if (testCaseArrayList != null) {
            this.testCaseIterator = testCaseArrayList.iterator();
        }
    }
}
